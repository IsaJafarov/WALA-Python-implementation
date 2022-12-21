/******************************************************************************
 * Copyright (c) 2018 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/
package com.ibm.wala.cast.python.ir;

import static com.ibm.wala.cast.python.ir.PythonLanguage.Python;

import java.nio.file.Path;
import java.util.*;

import com.ibm.wala.cast.ir.ssa.AssignInstruction;
import com.ibm.wala.cast.ir.ssa.AstGlobalRead;
import com.ibm.wala.cast.ir.ssa.AstInstructionFactory;
import com.ibm.wala.cast.ir.translator.ArrayOpHandler;
import com.ibm.wala.cast.ir.translator.AstTranslator;
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import com.ibm.wala.cast.loader.DynamicCallSiteReference;
import com.ibm.wala.cast.python.global.ImportType;
import com.ibm.wala.cast.python.global.SystemPath;
import com.ibm.wala.cast.python.global.XmlSummaries;
import com.ibm.wala.cast.python.ipa.summaries.BuiltinFunctions;
import com.ibm.wala.cast.python.loader.DynamicAnnotatableEntity;
import com.ibm.wala.cast.python.loader.PythonLoader;
import com.ibm.wala.cast.python.parser.PythonCodeEntity;
import com.ibm.wala.cast.python.ssa.PythonInvokeInstruction;
import com.ibm.wala.cast.python.types.PythonTypes;
import com.ibm.wala.cast.python.util.PathUtil;
import com.ibm.wala.cast.tree.CAstEntity;
import com.ibm.wala.cast.tree.CAstNode;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cast.tree.CAstSymbol;
import com.ibm.wala.cast.tree.CAstType;
import com.ibm.wala.cast.tree.impl.CAstControlFlowRecorder;
import com.ibm.wala.cast.tree.impl.CAstOperator;
import com.ibm.wala.cast.tree.impl.CAstSymbolImpl;
import com.ibm.wala.cast.tree.visit.CAstVisitor;
import com.ibm.wala.cast.util.CAstPrinter;
import com.ibm.wala.cfg.AbstractCFG;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.shrikeBT.IBinaryOpInstruction;
import com.ibm.wala.shrikeBT.IBinaryOpInstruction.IOperator;
import com.ibm.wala.shrikeBT.IInvokeInstruction.Dispatch;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.strings.Atom;

public class PythonCAstToIRTranslator extends AstTranslator {

    private final Map<CAstType, TypeName> walaTypeNames = HashMapFactory.make();
    private final Set<Pair<Scope, String>> globalDeclSet = new HashSet<>();
    private final HashMap<String, Set<Integer>> name2starValues = new HashMap<>();
    private static boolean signleFileAnalysis = true;

    public PythonCAstToIRTranslator(IClassLoader loader, Map<Object, CAstEntity> namedEntityResolver,
                                    ArrayOpHandler arrayOpHandler) {
        super(loader, namedEntityResolver, arrayOpHandler);
    }

    public PythonCAstToIRTranslator(IClassLoader loader, Map<Object, CAstEntity> namedEntityResolver) {
        super(loader, namedEntityResolver);
    }

    public PythonCAstToIRTranslator(IClassLoader loader) {
        super(loader);
    }

    public static boolean isSingleFileAnalysis() {
        return signleFileAnalysis;
    }

    public static void setSingleFileAnalysis(boolean singleFile) {
        PythonCAstToIRTranslator.signleFileAnalysis = singleFile;
    }

    @Override
    protected boolean liftDeclarationsForLexicalScoping() {
        return true;
    }

    @Override
    protected boolean hasImplicitGlobals() {
        return true;
    }

    @Override
    protected boolean useDefaultInitValues() {
        return true;
    }

    @Override
    protected boolean treatGlobalsAsLexicallyScoped() {
        return false;
    }

    @Override
    protected TypeReference defaultCatchType() {
        return PythonTypes.Exception;
    }

    @Override
    protected TypeReference makeType(CAstType type) {
        return TypeReference.findOrCreate(PythonTypes.pythonLoader, TypeName.string2TypeName(type.getName()));
    }

    @Override
    protected boolean defineType(CAstEntity type, WalkContext wc) {
        CAstType cls = type.getType();
        scriptScope(wc.currentScope()).declare(new CAstSymbolImpl(cls.getName(), cls));

        String typeNameStr = composeEntityName(wc, type);
        TypeName typeName = TypeName.findOrCreate("L" + typeNameStr);
        walaTypeNames.put(cls, typeName);

        ((PythonLoader) loader)
                .defineType(
                        typeName,
                        cls.getSupertypes().isEmpty() ?
                                PythonTypes.object.getName() :
                                walaTypeNames.get(cls.getSupertypes().iterator().next()), type.getPosition());

        return true;
    }

    @Override
    protected IOperator translateBinaryOpcode(CAstNode op) {
        // FIXME @Anemone, operator "in" or "not in" can't be simply replaced with "add".
        if (CAstOperator.OP_IN == op || CAstOperator.OP_NOT_IN == op || CAstOperator.OP_POW == op) {
            return IBinaryOpInstruction.Operator.ADD;
        } else {
            return super.translateBinaryOpcode(op);
        }
    }

    private Scope scriptScope(Scope s) {
        if (s.getEntity().getKind() == CAstEntity.SCRIPT_ENTITY) {
            return s;
        } else {
            return scriptScope(s.getParent());
        }
    }

    @Override
    protected void declareFunction(CAstEntity N, WalkContext context) {
        for (String s : N.getArgumentNames()) {
            context.currentScope().declare(new CAstSymbolImpl(s, Any));
        }

        String fnName = composeEntityName(context, N);
        if (N.getType() instanceof CAstType.Method) {
            ((PythonLoader) loader).defineMethodType("L" + fnName, N.getPosition(), N, walaTypeNames.get(((CAstType.Method) N.getType()).getDeclaringType()), context);
        } else {
            ((PythonLoader) loader).defineFunctionType("L" + fnName, N.getPosition(), N, context);
        }
    }

    @Override
    protected void defineFunction(CAstEntity N, WalkContext definingContext,
                                  AbstractCFG<SSAInstruction, ? extends IBasicBlock<SSAInstruction>> cfg, SymbolTable symtab,
                                  boolean hasCatchBlock, Map<IBasicBlock<SSAInstruction>, TypeReference[]> catchTypes, boolean hasMonitorOp,
                                  AstLexicalInformation lexicalInfo, DebuggingInformation debugInfo) {
        String fnName = composeEntityName(definingContext, N);

        ((PythonLoader) loader).defineCodeBodyCode("L" + fnName, cfg, symtab, hasCatchBlock, catchTypes, hasMonitorOp, lexicalInfo,
                debugInfo, N.getArgumentDefaults().length);
    }

    @Override
    protected void defineField(CAstEntity topEntity, WalkContext context, CAstEntity fieldEntity) {
        ((PythonLoader) loader).defineField(walaTypeNames.get(topEntity.getType()), fieldEntity);
    }

	/*
	@Override
	protected String composeEntityName(WalkContext parent, CAstEntity f) {
		if (f.getType() instanceof CAstType.Method) {
			return ((CAstType.Method)f.getType()).getDeclaringType().getName() + "/" + f.getName();
		} else {
			return f.getName();
		}
	}
*/

    @Override
    protected String composeEntityName(WalkContext parent, CAstEntity f) {
        if (f.getKind() == CAstEntity.SCRIPT_ENTITY)
            return f.getName();
        else {
            String name;
            //if (f.getType() instanceof CAstType.Method) {
            //	name = ((CAstType.Method)f.getType()).getDeclaringType().getName() + "/" + f.getName();
            //} else {
            name = f.getName();
            //}

            return parent.getName() + "/" + name;
        }
    }

    @Override
    protected void doPrologue(WalkContext context) {
        if (context.currentScope().getEntity().getKind() == CAstEntity.SCRIPT_ENTITY) {
            doGlobalWrite(context, context.currentScope().getEntity().getName(), PythonTypes.Root, 1);
        }

        super.doPrologue(context);
    }

    @Override
    protected void doThrow(WalkContext context, int exception) {
        context.cfg().addInstruction(Python.instructionFactory().ThrowInstruction(context.cfg().getCurrentInstruction(), exception));
    }

    @Override
    public void doArrayRead(WalkContext context, int result, int arrayValue, CAstNode arrayRef, int[] dimValues) {
        if (dimValues.length == 1) {
            int currentInstruction = context.cfg().getCurrentInstruction();
            context.cfg().addInstruction(((AstInstructionFactory) insts).PropertyRead(currentInstruction, result, arrayValue, dimValues[0]));
            context.cfg().noteOperands(currentInstruction, context.getSourceMap().getPosition(arrayRef));
        }
    }

    @Override
    public void doArrayWrite(WalkContext context, int arrayValue, CAstNode arrayRef, int[] dimValues, int rval) {
        assert dimValues.length == 1;
        context.cfg().addInstruction(((AstInstructionFactory) insts).PropertyWrite(context.cfg().getCurrentInstruction(), arrayValue, dimValues[0], rval));
    }

    @Override
    protected void leaveObjectRef(CAstNode n, WalkContext c, CAstVisitor<WalkContext> visitor) {
        WalkContext context = c;
        int result = c.getValue(n);
        CAstNode elt = n.getChild(1);
        doFieldRead(context, result, c.getValue(n.getChild(0)), elt, n);
    }

    @Override
    protected void doFieldRead(WalkContext context, int result, int receiver, CAstNode elt, CAstNode parent) {
        int currentInstruction = context.cfg().getCurrentInstruction();
        if (elt.getKind() == CAstNode.CONSTANT && elt.getValue() instanceof String) {
            if (elt.getValue().toString().equals("*")) {
                // from a import *
                Set<Integer> starValues = name2starValues.getOrDefault(context.getName(), new HashSet<>());
                starValues.add(receiver);
                name2starValues.put(context.getName(), starValues);
            } else {
                FieldReference f = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom((String) elt.getValue()), PythonTypes.Root);
                context.cfg().addInstruction(Python.instructionFactory().GetInstruction(currentInstruction, result, receiver, f));
            }
        } else {
            visit(elt, context, this);
            assert context.getValue(elt) != -1;
            context.cfg().addInstruction(((AstInstructionFactory) insts).PropertyRead(currentInstruction, result, receiver, context.getValue(elt)));
        }
        context.cfg().noteOperands(currentInstruction, context.getSourceMap().getPosition(parent.getChild(0)), context.getSourceMap().getPosition(elt));
    }

    @Override
    protected void doFieldWrite(WalkContext context, int receiver, CAstNode elt, CAstNode parent, int rval) {
        if (elt.getKind() == CAstNode.CONSTANT && elt.getValue() instanceof String) {
            FieldReference f = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom((String) elt.getValue()), PythonTypes.Root);
            context.cfg().addInstruction(Python.instructionFactory().PutInstruction(context.cfg().getCurrentInstruction(), receiver, rval, f));
        } else {
            visit(elt, context, this);
            assert context.getValue(elt) != -1;
            context.cfg().addInstruction(((AstInstructionFactory) insts).PropertyWrite(context.cfg().getCurrentInstruction(), receiver, context.getValue(elt), rval));
        }
    }

    @Override
    protected void leaveClassStmt(CAstNode n, WalkContext context, CAstVisitor<WalkContext> visitor) {
        super.leaveClassStmt(n, context, visitor);
        CAstEntity fn = (CAstEntity) n.getChild(0).getValue();

//        declareFunction(fn, context);
//        int result = context.currentScope().allocateTempValue();
//        int ex = context.currentScope().allocateTempValue();
//        String fnName = composeEntityName(context, fn);
//        doGlobalWrite(context, fnName, PythonTypes.Root, result);
//        FieldReference fnField = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom(fn.getName()), PythonTypes.Root);
//        context.cfg().addInstruction(Python.instructionFactory().PutInstruction(context.cfg().getCurrentInstruction(), 1, result+1, fnField));
    }

    @Override
    protected void doMaterializeFunction(CAstNode node, WalkContext context, int result, int exception, CAstEntity fn) {

        String fnName = composeEntityName(context, fn);
        IClass cls = loader.lookupClass(TypeName.findOrCreate("L" + fnName));
        TypeReference type = cls.getReference();
        int idx = context.cfg().getCurrentInstruction();
        context.cfg().addInstruction(Python.instructionFactory().NewInstruction(idx, result, NewSiteReference.make(idx, type)));

        if (fn instanceof DynamicAnnotatableEntity) {
            if (((DynamicAnnotatableEntity) fn).dynamicAnnotations().iterator().hasNext()) {
                ((DynamicAnnotatableEntity) fn).dynamicAnnotations().forEach(a -> {
                    visit(a, context, this);
                    int pos = context.cfg().getCurrentInstruction();
                    CallSiteReference site = new DynamicCallSiteReference(PythonTypes.CodeBody, pos);
                    context.cfg().addInstruction(
                            new PythonInvokeInstruction(
                                    context.cfg().getCurrentInstruction(),
                                    result,
                                    context.currentScope().allocateTempValue(),
                                    site,
                                    new int[]{context.getValue(a), result},
                                    new Pair[0]));
                });
            }
        }

        doGlobalWrite(context, fnName, PythonTypes.Root, result);
        FieldReference fnField = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom(fn.getName()), PythonTypes.Root);
        context.cfg().addInstruction(Python.instructionFactory().PutInstruction(context.cfg().getCurrentInstruction(), 1, result, fnField));
    }


    @Override
    protected void leaveFunctionEntity(CAstEntity n, WalkContext context, WalkContext codeContext,
                                       CAstVisitor<WalkContext> visitor) {
        super.leaveFunctionEntity(n, context, codeContext, visitor);

        String fnName = composeEntityName(context, n) + "_defaults";
        if (n.getArgumentDefaults() != null) {
            int first = n.getArgumentCount() - n.getArgumentDefaults().length;
            for (int i = first; i < n.getArgumentCount(); i++) {
                CAstNode dflt = n.getArgumentDefaults()[i - first];
                WalkContext cc = context.codeContext();
                visitor.visit(dflt, cc, visitor);
                doGlobalWrite(cc, "L" + fnName + "_" + i, PythonTypes.Root, cc.getValue(dflt));
            }
        }
    }

    @Override
    protected void leaveVar(CAstNode n, WalkContext c, CAstVisitor<WalkContext> visitor) {
        WalkContext context = c;
        String nm = (String) n.getChild(0).getValue();
        assert nm != null : "cannot find var for " + CAstPrinter.print(n, context.getSourceMap());
        Symbol s = context.currentScope().lookup(nm);
        assert s != null : "cannot find symbol for " + nm + " at " + CAstPrinter.print(n, context.getSourceMap());
        assert s.type() != null : "no type for " + nm + " at " + CAstPrinter.print(n, context.getSourceMap());
        TypeReference type = makeType(s.type());
        if (context.currentScope().isGlobal(s) || isGlobal(context, nm)) {
            int globalVal = doGlobalRead(n, context, nm, type);
            Set<Integer> starValues = name2starValues.getOrDefault(context.getName(), new HashSet<>());
            // TODO 这里修复 from x import *
            if (!starValues.isEmpty()) {
                int phiVal = context.currentScope().allocateTempValue();
                PreBasicBlock prevBB = context.cfg().getCurrentBlock();
                context.cfg().addInstruction(Python.instructionFactory().AssignInstruction(context.cfg().getCurrentInstruction(), phiVal, globalVal));
                List<PreBasicBlock> possibleBBs = new LinkedList<>();
                for (Integer eachValue : starValues) {
                    PreBasicBlock nextBB = context.cfg().newBlock(true);
                    int fieldVal = context.currentScope().allocateTempValue();
                    FieldReference f = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom(nm), PythonTypes.Root);
                    context.cfg().addInstruction(
                            Python.instructionFactory().GetInstruction(context.cfg().getCurrentInstruction(), fieldVal, eachValue, f));
                    context.cfg().addInstruction(
                            Python.instructionFactory().AssignInstruction(context.cfg().getCurrentInstruction(), phiVal, fieldVal));
                    possibleBBs.add(nextBB);
                }
                PreBasicBlock nextBB = context.cfg().newBlock(true);
                for (PreBasicBlock possibleBB : possibleBBs) {
                    context.cfg().addEdge(prevBB, possibleBB);
                    context.cfg().addEdge(possibleBB, nextBB);
                }
                context.cfg().addEdge(prevBB, nextBB);
                c.setValue(n, phiVal);
                FieldReference fnField = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom(nm), PythonTypes.Root);
                context.cfg().addInstruction(Python.instructionFactory().PutInstruction(context.cfg().getCurrentInstruction(), 1, phiVal, fnField));
            } else {
                c.setValue(n, globalVal);
            }
        } else if (context.currentScope().isLexicallyScoped(s)) {
            c.setValue(n, doLexicallyScopedRead(n, context, nm, type));
        } else {
            // `import a as b` 时走的是这里
            c.setValue(n, doLocalRead(context, nm, type));
        }
    }

    @Override
    protected void leaveVarAssign(CAstNode n, CAstNode v, CAstNode a, WalkContext c, CAstVisitor<WalkContext> visitor) {
        super.leaveVarAssign(n, v, a, c, visitor);
        WalkContext context = c;
        int rval = c.getValue(v);
        String nm = (String) n.getChild(0).getValue();
        // script的var要put
        if (context.getName().endsWith(".py")) {
            FieldReference fnField = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom(nm), PythonTypes.Root);
            context.cfg().addInstruction(Python.instructionFactory().PutInstruction(context.cfg().getCurrentInstruction(), 1, rval, fnField));
        }
    }

    @Override
    protected void assignValue(CAstNode n, WalkContext context, Symbol ls, String nm, int rval) {
        if (context.currentScope().isGlobal(ls) || isGlobal(context, nm))
            doGlobalWrite(context, nm, makeType(ls.type()), rval);
        else if (context.currentScope().isLexicallyScoped(ls)) {
            doLexicallyScopedWrite(context, nm, makeType(ls.type()), rval);
        } else {
            assert rval != -1 : CAstPrinter.print(n, context.top().getSourceMap());
            doLocalWrite(context, nm, makeType(ls.type()), rval);
        }
    }

    @Override
    protected void leaveVarAssignOp(CAstNode n, CAstNode v, CAstNode a, boolean pre, WalkContext c, CAstVisitor<WalkContext> visitor) {
        WalkContext context = c;
        String nm = (String) n.getChild(0).getValue();
        Symbol ls = context.currentScope().lookup(nm);
        TypeReference type = makeType(ls.type());
        int temp;

        if (context.currentScope().isGlobal(ls) || isGlobal(context, nm))
            temp = doGlobalRead(n, context, nm, type);
        else if (context.currentScope().isLexicallyScoped(ls)) {
            temp = doLexicallyScopedRead(n, context, nm, type);
        } else {
            temp = doLocalRead(context, nm, type);
        }

        if (!pre) {
            int ret = context.currentScope().allocateTempValue();
            int currentInstruction = context.cfg().getCurrentInstruction();
            context.cfg().addInstruction(new AssignInstruction(currentInstruction, ret, temp));
            context.cfg().noteOperands(currentInstruction, context.getSourceMap().getPosition(n.getChild(0)));
            c.setValue(n, ret);
        }

        int rval = processAssignOp(v, a, temp, c);

        if (pre) {
            c.setValue(n, rval);
        }

        if (context.currentScope().isGlobal(ls) || isGlobal(context, nm)) {
            doGlobalWrite(context, nm, type, rval);
        } else if (context.currentScope().isLexicallyScoped(ls)) {
            doLexicallyScopedWrite(context, nm, type, rval);
        } else {
            doLocalWrite(context, nm, type, rval);
        }
    }

    // after visit class
    @Override
    protected void leaveTypeEntity(CAstEntity n, WalkContext context, WalkContext typeContext, CAstVisitor<WalkContext> visitor) {
        super.leaveTypeEntity(n, context, typeContext, visitor);

        WalkContext code = context.codeContext();

        int v = code.currentScope().allocateTempValue();

        int idx = code.cfg().getCurrentInstruction();
        String fnName = composeEntityName(context, n);
        IClass cls = loader.lookupClass(TypeName.findOrCreate("L" + fnName));
        TypeReference type = cls.getReference();
        code.cfg().addInstruction(Python.instructionFactory().NewInstruction(idx, v, NewSiteReference.make(idx, type)));

        doLocalWrite(code, n.getType().getName(), type, v);
        doGlobalWrite(code, fnName, PythonTypes.Root, v);
        // script的class要put
        if (context.getName().endsWith(".py")) {
            FieldReference fnField = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom(n.getType().getName()), PythonTypes.Root);
            context.cfg().addInstruction(Python.instructionFactory().PutInstruction(context.cfg().getCurrentInstruction(), 1, v, fnField));
        }

        if (!this.entity2ExposedNames.containsKey(context.top())) {
            this.entity2ExposedNames.put(context.top(), HashSetFactory.make());
        }
        this.entity2ExposedNames.get(context.top()).add(fnName);

        for (CAstEntity field : n.getAllScopedEntities().get(null)) {
            FieldReference fr = FieldReference.findOrCreate(type, Atom.findOrCreateUnicodeAtom(field.getName()), PythonTypes.Root);
            int val;
            if (field.getKind() == CAstEntity.FIELD_ENTITY) {
                this.visit(field.getAST(), code, this);
                val = code.getValue(field.getAST());
            } else if (field.getKind() == CAstEntity.TYPE_ENTITY) {
                String className = composeEntityName(typeContext, field);
                val = doGlobalRead(null, code, className, PythonTypes.Root);
            } else {
                assert (field.getKind() == CAstEntity.FUNCTION_ENTITY);
                val = code.currentScope().allocateTempValue();
                String methodName = composeEntityName(typeContext, field);
                IClass methodCls = loader.lookupClass(TypeName.findOrCreate("L" + methodName));
                TypeReference methodType = methodCls.getReference();
                int codeIdx = code.cfg().getCurrentInstruction();
                code.cfg().addInstruction(Python.instructionFactory().NewInstruction(codeIdx, val, NewSiteReference.make(codeIdx, methodType)));
            }
            code.cfg().addInstruction(Python.instructionFactory().PutInstruction(code.cfg().getCurrentInstruction(), v, val, fr));
        }
    }

    @Override
    protected void leaveFileEntity(
            CAstEntity n,
            WalkContext context,
            WalkContext fileContext,
            CAstVisitor<WalkContext> visitor) {
        super.leaveFileEntity(n, context, fileContext, visitor);
    }

    @Override
    protected void leaveScriptEntity(CAstEntity n, WalkContext context, WalkContext codeContext, CAstVisitor<WalkContext> visitor) {
        super.leaveScriptEntity(n, context, codeContext, visitor);
    }


    @Override
    protected void doNewObject(WalkContext context, CAstNode newNode, int result, Object type, int[] arguments) {
        context.cfg().addInstruction(
                insts.NewInstruction(context.cfg().getCurrentInstruction(),
                        result,
                        NewSiteReference.make(
                                context.cfg().getCurrentInstruction(),
                                TypeReference.findOrCreate(
                                        PythonTypes.pythonLoader,
                                        "L" + type))));
    }

    @Override
    protected void leaveCall(CAstNode n, WalkContext c, CAstVisitor<WalkContext> visitor) {
        WalkContext context = c;
        int result = c.getValue(n);
        int exp = context.currentScope().allocateTempValue();
        int fun = c.getValue(n.getChild(0));
        CAstNode functionName = n.getChild(1);
        int[] args = new int[n.getChildCount() - 2];
        for (int i = 0; i < args.length; i++) {
            CAstNode s = n.getChild(i + 2);
            args[i] = c.getValue(s);
//            if (s.getKind()==CAstNode.PRIMITIVE){
//                args[i] = c.getValue(s.getChild(0));
//            }else {
//                args[i] = c.getValue(s);
//            }
        }
        doCall(context, n, result, exp, functionName, fun, args);
    }

    /**
     * @param context
     * @param call
     * @param result
     * @param exception
     * @param name
     * @param receiver
     * @param arguments
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doCall(WalkContext context, CAstNode call, int result, int exception, CAstNode name, int receiver,
                          int[] arguments) {
        List<Position> pospos = new ArrayList<Position>();
        List<Position> keypos = new ArrayList<Position>();
        List<Integer> posp = new ArrayList<Integer>();
        List<Pair<String, Integer>> keyp = new ArrayList<Pair<String, Integer>>();
        posp.add(receiver);
        pospos.add(context.getSourceMap().getPosition(call.getChild(0)));
        int argsVal = 0;
        int kwargsVal = 0;
        for (int i = 2; i < call.getChildCount(); i++) {
            CAstNode cl = call.getChild(i);
            if (cl.getKind() == CAstNode.ARRAY_LITERAL) {
                // kwargs
                if (cl.getChild(0).getValue() == null) {
                    // **kwargs
                    kwargsVal = context.getValue(cl.getChild(1));
                } else {
                    // kwargs
                    keyp.add(Pair.make(String.valueOf(cl.getChild(0).getValue()), context.getValue(cl.getChild(1))));
                }
                keypos.add(context.getSourceMap().getPosition(cl));
            } else {
                if (cl.getKind() == CAstNode.PRIMITIVE) {
                    // *args
                    argsVal = context.getValue(cl);
//                    posp.add(argsVal);
                } else {
                    // args
                    posp.add(context.getValue(cl));
                }
                pospos.add(context.getSourceMap().getPosition(cl));
            }
        }

        int params[] = new int[arguments.length + 1];
        params[0] = receiver;
        System.arraycopy(arguments, 0, params, 1, arguments.length);

        int[] hack = new int[posp.size()];
        for (int i = 0; i < hack.length; i++) {
            hack[i] = posp.get(i);
        }

        int pos = context.cfg().getCurrentInstruction();
        CallSiteReference site = new DynamicCallSiteReference(PythonTypes.CodeBody, pos);

        context.cfg().addInstruction(new PythonInvokeInstruction(pos, result, exception, site, hack, keyp.toArray(new Pair[keyp.size()]), argsVal, kwargsVal));

        pospos.addAll(keypos);
        context.cfg().noteOperands(pos, pospos.toArray(new Position[pospos.size()]));
        context.cfg().addPreNode(call, context.getUnwindState());

        // this new block is for the normal termination case
        context.cfg().newBlock(true);

        // exceptional case: flow to target given in CAst, or if null, the exit node
        ((CAstControlFlowRecorder) context.getControlFlow()).map(call, call);

        if (context.getControlFlow().getTarget(call, null) != null)
            context.cfg().addPreEdge(call, context.getControlFlow().getTarget(call, null), true);
        else context.cfg().addPreEdgeToExit(call, true);
    }

    public static final CAstType Any = new CAstType() {

        @Override
        public String getName() {
            return "Any";
        }

        @Override
        public Collection<CAstType> getSupertypes() {
            return Collections.emptySet();
        }
    };

    @Override
    protected CAstType topType() {
        return Any;
    }

    public final CAstType Exception = new CAstType() {

        @Override
        public String getName() {
            return "Exception";
        }

        @Override
        public Collection<CAstType> getSupertypes() {
            return Collections.singleton(topType());
        }
    };

    @Override
    protected CAstType exceptionType() {
        return Any;
    }

    /**
     * 处理import语句
     *
     * @param resultVal
     * @param context
     * @param primitiveCall
     */
    @Override
    protected void doPrimitive(int resultVal, WalkContext context, CAstNode primitiveCall) {

        if (primitiveCall.getChildCount() == 2) {
            String nameToken = (String) primitiveCall.getChild(1).getValue();
            if (ImportType.BUILTIN.equals(primitiveCall.getChild(0).getValue())) {
                int instNo = context.cfg().getCurrentInstruction();
                TypeReference importType = TypeReference.findOrCreate(PythonTypes.pythonLoader, "L" + nameToken);
                MethodReference call = MethodReference.findOrCreate(importType, "import", "()L" + primitiveCall.getChild(1).getValue());
                context.cfg().addInstruction(Python.instructionFactory().InvokeInstruction(instNo, resultVal, new int[0], context.currentScope().allocateTempValue(), CallSiteReference.make(instNo, call, Dispatch.STATIC), null));
            } else if (ImportType.INIT.equals(primitiveCall.getChild(0).getValue())) {
                // TODO: 调用lib时要以libpath拼接
                Path importedPath = SystemPath.getInstance().getImportModule(context.file(), "." + nameToken);
                FieldReference global = makeGlobalRef(
                        "script " + PathUtil.getUriString(importedPath) + ".py");
                context.cfg().addInstruction(new AstGlobalRead(context.cfg().getCurrentInstruction(), resultVal, global));
            } else {
                // TODO: 若contextfile在app，而import为lib时？
                if (BuiltinFunctions.builtins().contains(nameToken) || XmlSummaries.getInstance().contains(nameToken)) {
                    // in BIF XML
                    int instNo = context.cfg().getCurrentInstruction();
                    TypeReference importType = TypeReference.findOrCreate(PythonTypes.pythonLoader, "L" + nameToken);
                    MethodReference call = MethodReference.findOrCreate(importType, "import", "()L" + primitiveCall.getChild(1).getValue());
                    context.cfg().addInstruction(Python.instructionFactory().InvokeInstruction(instNo, resultVal, new int[0], context.currentScope().allocateTempValue(), CallSiteReference.make(instNo, call, Dispatch.STATIC), null));
                } else {
                    // in .py file
                    Path importedPath = SystemPath.getInstance().getImportModule(context.file(), nameToken);
                    FieldReference global = makeGlobalRef(
                            "script " + PathUtil.getUriString(importedPath) + ".py");
                    context.cfg().addInstruction(new AstGlobalRead(context.cfg().getCurrentInstruction(), resultVal, global));
                }
            }
        } else if (primitiveCall.getChildCount() == 1) {

            int instNo = context.cfg().getCurrentInstruction();
            context.setValue(primitiveCall, resultVal);
            int v = context.getValue(primitiveCall.getChild(0));
            context.cfg().addInstruction(new AssignInstruction(instNo, resultVal, v));
        }
    }

    @Override
    protected void leaveDeclStmt(CAstNode n, WalkContext context, CAstVisitor<WalkContext> visitor) {
        Queue<CAstNode> workList = new LinkedList<>();
        workList.add(n);
        CAstNode importCAst = null;
        while (!workList.isEmpty()) {
            CAstNode curr = workList.poll();
            if (curr.getKind() == CAstNode.PRIMITIVE && !curr.getChild(0).getValue().equals(ImportType.BUILTIN)) {
                importCAst = curr;
                workList.clear();
                break;
            }
            workList.addAll(curr.getChildren());
        }
        if (n.getChild(1).getKind() == CAstNode.OBJECT_REF
                && n.getChild(1).getChild(0).getKind() == CAstNode.VAR
                && n.getChild(1).getChild(0).getChild(0).getValue() != null
                && n.getChild(1).getChild(0).getChild(0).getValue().toString().startsWith("importTree")) {
            // from x import y as declToken
            String declToken = n.getChild(0).getValue().toString();
            int declVal = context.getValue(n.getChild(1));
            if (!declToken.equals("*")) {
                FieldReference fnField = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom(declToken), PythonTypes.Root);
                context.cfg().addInstruction(Python.instructionFactory().PutInstruction(context.cfg().getCurrentInstruction(), 1, declVal, fnField));
            }
        } else if (importCAst != null) {
            // import x as declToken
            // FIXME 当import pkg1.moduleD时，不将moduleD加入
            String declToken = n.getChild(0).getValue().toString();
            if (!declToken.startsWith("importTree")) {
                FieldReference fnField = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom(declToken), PythonTypes.Root);
                int declVal = context.getValue(n.getChild(1));
                context.cfg().addInstruction(Python.instructionFactory().PutInstruction(context.cfg().getCurrentInstruction(), 1, declVal, fnField));
                // 当import xxx as b的时候，不declare
                String declareField = n.getChild(1).getChild(1).getValue().toString();
                if (declareField.equals(declToken) && (!XmlSummaries.getInstance().contains(declToken))) {
                    CAstSymbol pkgSymbol = new CAstSymbolImpl(importCAst.getChild(1).getValue().toString(), PythonCAstToIRTranslator.Any);
                    String nm = pkgSymbol.name();
                    if (!context.currentScope().contains(nm)) {
                        context.currentScope().declare(pkgSymbol, context.getValue(importCAst));
                    }
                }
            }
        }

        super.leaveDeclStmt(n, context, visitor);
    }

    @Override
    protected boolean visitVarAssign(CAstNode n, CAstNode v, CAstNode a, WalkContext c,
                                     CAstVisitor<WalkContext> visitor) {
        String name = n.getChild(0).getValue().toString();
        if (!c.currentScope().contains(name)) {
            c.currentScope().declare(new CAstSymbolImpl(name, PythonCAstToIRTranslator.Any));
        }

        return false;
    }

    @Override
    protected boolean visitVarAssignOp(CAstNode n, CAstNode v, CAstNode a, boolean pre, WalkContext c,
                                       CAstVisitor<WalkContext> visitor) {
        return visitVarAssign(n, v, a, c, visitor);
    }


    @Override
    protected void leaveArrayLiteralAssign(CAstNode n, CAstNode v, CAstNode a, WalkContext c,
                                           CAstVisitor<WalkContext> visitor) {
        int rval = c.getValue(v);
        for (int i = 1; i < n.getChildCount(); i++) {
            CAstNode var = n.getChild(i);
            if (var.getKind() == CAstNode.VAR) {
                String name = (String) var.getChild(0).getValue();
                c.currentScope().declare(new CAstSymbolImpl(name, topType()));
                Symbol ls = c.currentScope().lookup(name);

                int rvi = c.currentScope().allocateTempValue();
                int idx = c.currentScope().getConstantValue(i - 1);
                c.cfg().addInstruction(Python.instructionFactory().PropertyRead(c.cfg().getCurrentInstruction(), rvi, rval, idx));

                c.setValue(n, rvi);
                assignValue(n, c, ls, name, rvi);
            }
        }
    }

    @Override
    protected void leaveObjectLiteralAssign(CAstNode n, CAstNode v, CAstNode a, WalkContext c,
                                            CAstVisitor<WalkContext> visitor) {
        int rval = c.getValue(v);
        handleObjectLiteralAssign(n, n, rval, c, visitor);
    }

    private void handleObjectLiteralAssign(CAstNode n, CAstNode lvalAst, int rval, WalkContext c,
                                           CAstVisitor<WalkContext> visitor) {
        for (int i = 1; i < lvalAst.getChildCount(); i += 2) {
            int idx = c.getValue(lvalAst.getChild(i));
            if (idx == -1) {
                visitor.visit(lvalAst.getChild(i), c, visitor);
                idx = c.getValue(lvalAst.getChild(i));
            }
            CAstNode var = lvalAst.getChild(i + 1);
            if (var.getKind() == CAstNode.VAR) {
                String name = (String) var.getChild(0).getValue();
                c.currentScope().declare(new CAstSymbolImpl(name, topType()));
                Symbol ls = c.currentScope().lookup(name);

                int rvi = c.currentScope().allocateTempValue();
                c.cfg().addInstruction(Python.instructionFactory().PropertyRead(c.cfg().getCurrentInstruction(), rvi, rval, idx));

                // c.setValue(n, rvi);
                assignValue(n, c, ls, name, rvi);
            } else if (var.getKind() == CAstNode.OBJECT_LITERAL) {
                int rvi = c.currentScope().allocateTempValue();
                c.cfg().addInstruction(Python.instructionFactory().PropertyRead(c.cfg().getCurrentInstruction(), rvi, rval, idx));
                handleObjectLiteralAssign(n, var, rvi, c, visitor);
            }
        }
    }

    boolean isGlobal(WalkContext context, String varName) {
        if (signleFileAnalysis)
            return false;
        else {
            if (context.currentScope().getEntity().getKind() == CAstEntity.SCRIPT_ENTITY)
                return true;
            else {
                Pair<Scope, String> pair = Pair.make(context.currentScope(), varName);
                if (globalDeclSet.contains(pair))
                    return true;
                else
                    return false;
            }
        }
    }

    void addGlobal(Scope scope, String varName) {
        Pair<Scope, String> pair = Pair.make(scope, varName);
        globalDeclSet.add(pair);
    }

    @Override
    protected boolean doVisit(CAstNode n, WalkContext context, CAstVisitor<WalkContext> visitor) {
        if (n.getKind() == CAstNode.COMPREHENSION_EXPR) {
            int[] args = new int[n.getChild(2).getChildCount() + 2];

            visitor.visit(n.getChild(0), context, visitor);
            int obj = context.getValue(n.getChild(0));

            visitor.visit(n.getChild(1), context, visitor);
            int lambda = context.getValue(n.getChild(1));

            args[0] = lambda;
            args[1] = obj;
            for (int i = 0; i < args.length - 2; i++) {
                visitor.visit(n.getChild(2).getChild(i), context, visitor);
                args[i + 2] = context.getValue(n.getChild(2).getChild(i));
            }

            int pos = context.cfg().getCurrentInstruction();
            CallSiteReference site = new DynamicCallSiteReference(PythonTypes.CodeBody, pos);
            int result = context.currentScope().allocateTempValue();
            int exception = context.currentScope().allocateTempValue();
            context.cfg().addInstruction(new PythonInvokeInstruction(pos, result, exception, site, args, new Pair[0]));

            context.setValue(n, result);
            return true;

        } else if (n.getKind() == CAstNode.GLOBAL_DECL) {
            int numOfChildren = n.getChildCount();
            for (int i = 0; i < numOfChildren; i++) {
                String val = (String) n.getChild(i).getChild(0).getValue();
                System.out.println("Hey " + val);
                addGlobal(context.currentScope(), val);
            }
            return true;

        } else {
            return super.doVisit(n, context, visitor);
        }
    }


    @Override
    protected boolean doVisitAssignNodes(CAstNode n, WalkContext context, CAstNode v, CAstNode a,
                                         CAstVisitor<WalkContext> visitor) {
        return super.doVisitAssignNodes(n, context, v, a, visitor);
    }


    @Override
    protected Position[] getParameterPositions(CAstEntity e) {
        Position[] ps = new Position[Math.max(1, e.getArgumentCount())];
        for (int i = 1; i < e.getArgumentCount(); i++) {
            ps[i] = e.getPosition(i);
        }
        return ps;
    }

}
