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
package com.ibm.wala.cast.python.ipa.callgraph;

import java.util.*;

import com.ibm.wala.cast.ipa.callgraph.AstSSAPropagationCallGraphBuilder;
import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.ir.ssa.AstLexicalAccess;
import com.ibm.wala.cast.ir.ssa.AstLexicalRead;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.loader.CAstAbstractModuleLoader;
import com.ibm.wala.cast.loader.DynamicCallSiteReference;
import com.ibm.wala.cast.python.ipa.summaries.PythonInstanceMethodTrampoline;
import com.ibm.wala.cast.python.ipa.summaries.PythonSummarizedFunction;
import com.ibm.wala.cast.python.ipa.summaries.PythonSummary;
import com.ibm.wala.cast.python.ir.PythonLanguage;
import com.ibm.wala.cast.python.loader.PythonLoader;
import com.ibm.wala.cast.python.parser.PythonSourcePosition;
import com.ibm.wala.cast.python.ssa.PythonInvokeInstruction;
import com.ibm.wala.cast.python.ssa.PythonPropertyRead;
import com.ibm.wala.cast.python.types.PythonTypes;
import com.ibm.wala.cast.python.util.CounterSystem;
import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.ConstantValue;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.strings.Atom;

/**
 * 这里在每个函数前都套了层wrapper， 但是这样做会降低指针分析效果
 */
public class PythonTrampolineTargetSelector implements MethodTargetSelector {
    private final MethodTargetSelector base;

    class CalleeKey {
        private CGNode cgNode;
        private IClass receiver;

        public CalleeKey(CGNode cgNode, IClass receiver) {
            this.cgNode = cgNode;
            this.receiver = receiver;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CalleeKey calleeKey = (CalleeKey) o;
            return Objects.equals(cgNode, calleeKey.cgNode) &&
                    Objects.equals(receiver, calleeKey.receiver);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cgNode, receiver);
        }
    }

    public PythonTrampolineTargetSelector(MethodTargetSelector base) {
        this.base = base;
    }

    private final Map<Pair<IClass, Integer>, IMethod> codeBodies = HashMapFactory.make();
    private final Map<CalleeKey, IMethod> wrapperBodies = HashMapFactory.make();

    @SuppressWarnings("unchecked")
    @Override
    public IMethod getCalleeTarget(CGNode caller, CallSiteReference site, IClass receiver) {
        if (receiver != null) {
            IClassHierarchy cha = receiver.getClassHierarchy();
            int[] hashCodes = new int[] { caller.hashCode(), caller.getContext().hashCode(),site.hashCode(),receiver.hashCode() };
            String hashCode = Integer.toHexString(hashCodes.hashCode());
            PythonInvokeInstruction call = (PythonInvokeInstruction) caller.getIR().getCalls(site)[0];
            int realParaNum = call.getNumberOfConstParameters();
            if (receiver.getAnnotations().contains(Annotation.make(PythonTypes.classMethod))
                    && !caller.getMethod().getName().toString().startsWith("cls_trampoline")
            ) {
                int defParaNum = ((PythonLoader.DynamicMethodBody) receiver).getCodeBody().getNumberOfParameters();
                Atom defFuncName = ((PythonLoader.DynamicMethodBody) receiver).getCodeBody().getReference().getDeclaringClass().getName().getClassName();
                Atom trampolineName = Atom.findOrCreateUnicodeAtom("cls_trampoline_" + defFuncName + "_" + hashCode + "(" + call.getNumberOfTotalParameters() + ")");

                CalleeKey key = new CalleeKey(caller, receiver);
                if (!wrapperBodies.containsKey(key)) {
                    Map<Integer, Atom> names = HashMapFactory.make();
                    MethodReference tr = MethodReference.findOrCreate(receiver.getReference(),
                            trampolineName,
                            AstMethodReference.fnDesc);
                    PythonSummary x = new PythonSummary(tr, call.getNumberOfTotalParameters());
                    int iindex = 0;
                    int v = call.getNumberOfTotalParameters();
                    String nameAtEntityName = ((PythonLoader.DynamicMethodBody) receiver).getContainer().getName().toString();
                    int scriptIdx = nameAtEntityName.lastIndexOf(".py/");
                    String entityName = nameAtEntityName.substring(0, scriptIdx + 3);
                    String name = nameAtEntityName.substring(scriptIdx + 4);

                    Set<String> globalNames = new HashSet<>();
                    if (!caller.getMethod().getName().toString().startsWith("self_trampoline")) {
                        AstMethod.LexicalInformation LI = ((AstIRFactory.AstIR) caller.getIR()).lexicalInfo();
                        Pair<String, String>[] exposedNames = LI.getExposedNames();
                        for (int i = 0; i < exposedNames.length; i++) {
                            globalNames.add(exposedNames[i].fst);
                        }
                    }
                    if (name.contains("/")) {
                        AstLexicalAccess.Access global;
                        String[] possibleGlobals = name.split("/");
                        int i = 0;
                        for (; i < possibleGlobals.length; i++) {
                            if (globalNames.contains(possibleGlobals[i])) {
                                AstLexicalAccess.Access A = new AstLexicalAccess.Access(possibleGlobals[i], entityName, PythonTypes.Dynamic, ++v);
                                AstLexicalRead astLexicalRead = new AstLexicalRead(iindex++, A);
                                x.addStatement(astLexicalRead);
                                break;
                            }
                        }
                        if (i == possibleGlobals.length) {
                            System.err.println("Not found a global called " + name + "@" + entityName);
                        }
                        i++;
                        for (; i < possibleGlobals.length; i++) {
                            x.addStatement(PythonLanguage.Python.instructionFactory()
                                    .GetInstruction(iindex++, ++v, v - 1, FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom(possibleGlobals[i]), PythonTypes.Root)));
                        }
                    } else {
                        // A.static_func() || a.static_func()
                        if (caller.getMethod().getName().toString().startsWith("self_trampoline") || globalNames.contains(name)) {
                            AstLexicalAccess.Access A = new AstLexicalAccess.Access(name, entityName, PythonTypes.Dynamic, ++v);
                            x.addStatement(new AstLexicalRead(iindex++, A));
                        } else {
                            System.err.println("Not found a global called " + name + "@" + entityName);
                        }
                    }


                    int i = 0;
                    int[] params;
                    if (caller.getMethod().getName().toString().startsWith("self_trampoline")) {
                        params = new int[Math.max(2, call.getNumberOfPositionalParameters())];
                    } else {
                        params = new int[Math.max(2, call.getNumberOfPositionalParameters() + 1)];
                    }
                    params[i++] = 1;
                    params[i++] = v;

                    if (caller.getMethod().getName().toString().startsWith("self_trampoline")) {
                        for (int j = 2; j < call.getNumberOfPositionalParameters(); j++) {
                            params[i++] = j + 1;
                        }
                    } else {
                        for (int j = 1; j < call.getNumberOfPositionalParameters(); j++) {
                            params[i++] = j + 1;
                        }
                    }

                    int ki = 0, ji = call.getNumberOfPositionalParameters() + 1;
                    Pair<String, Integer>[] keys = new Pair[0];
                    if (call.getKeywords() != null) {
                        keys = new Pair[call.getKeywords().size()];
                        for (String k : call.getKeywords()) {
                            names.put(ji, Atom.findOrCreateUnicodeAtom(k));
                            keys[ki++] = Pair.make(k, ji++);
                        }
                    }

                    int result = ++v;
                    int except = ++v;
                    CallSiteReference ref = new DynamicCallSiteReference(call.getCallSite().getDeclaredTarget(), 2);
                    x.addStatement(new PythonInvokeInstruction(iindex++, result, except, ref, params, keys));
                    x.addStatement(new SSAReturnInstruction(iindex, result, false));
                    x.setValueNames(names);
                    wrapperBodies.put(key, new PythonSummarizedFunction(tr, x, receiver));
                }
                return wrapperBodies.get(key);
            } else if (receiver.getAnnotations().contains(Annotation.make(PythonTypes.staticMethod))
                    && !caller.getMethod().getName().toString().startsWith("static_trampoline")
            ) {
                int defParaNum = ((PythonLoader.DynamicMethodBody) receiver).getCodeBody().getNumberOfParameters();
                Atom defFuncName = ((PythonLoader.DynamicMethodBody) receiver).getCodeBody().getReference().getDeclaringClass().getName().getClassName();
                Atom trampolineName = Atom.findOrCreateUnicodeAtom("static_trampoline_" + defFuncName +"_"+hashCode+ "(" + call.getNumberOfTotalParameters() + ")");

                CalleeKey key = new CalleeKey(caller, receiver);
                if (!wrapperBodies.containsKey(key)) {
                    Map<Integer, Atom> names = HashMapFactory.make();
                    MethodReference tr = MethodReference.findOrCreate(receiver.getReference(),
                            trampolineName,
                            AstMethodReference.fnDesc);
                    PythonSummary x = new PythonSummary(tr, call.getNumberOfTotalParameters());
                    int iindex = 0;
                    int v = call.getNumberOfTotalParameters();
                    // 如果是内部类调用则要用getInstruction
                    int i = 0;

                    int[] params;
                    if (caller.getMethod().getName().toString().startsWith("self_trampoline")) {
                        params = new int[Math.max(2, call.getNumberOfPositionalParameters() - 1)];
                    } else {
                        params = new int[Math.max(2, call.getNumberOfPositionalParameters())];
                    }
                    params[i++] = 1;

                    if (caller.getMethod().getName().toString().startsWith("self_trampoline")) {
                        for (int j = 2; j < call.getNumberOfPositionalParameters(); j++) {
                            params[i++] = j + 1;
                        }
                    } else {
                        for (int j = 1; j < call.getNumberOfPositionalParameters(); j++) {
                            params[i++] = j + 1;
                        }
                    }
                    int ki = 0, ji = call.getNumberOfPositionalParameters() + 1;
                    Pair<String, Integer>[] keys = new Pair[0];
                    if (call.getKeywords() != null) {
                        keys = new Pair[call.getKeywords().size()];
                        for (String k : call.getKeywords()) {
                            names.put(ji, Atom.findOrCreateUnicodeAtom(k));
                            keys[ki++] = Pair.make(k, ji++);
                        }
                    }

                    int result = ++v;
                    int except = ++v;
                    CallSiteReference ref = new DynamicCallSiteReference(call.getCallSite().getDeclaredTarget(), 2);
                    x.addStatement(new PythonInvokeInstruction(iindex++, result, except, ref, params, keys));
                    x.addStatement(new SSAReturnInstruction(iindex, result, false));
                    x.setValueNames(names);
                    wrapperBodies.put(key, new PythonSummarizedFunction(tr, x, receiver));
                }
                return wrapperBodies.get(key);
            } else if (cha.isSubclassOf(receiver, cha.lookupClass(PythonTypes.trampoline))
            ) {
                // self
                Pair<IClass, Integer> key = Pair.make(receiver, call.getNumberOfTotalParameters());
                if (!codeBodies.containsKey(key)) {

//                    int defParaNum = ((PythonLoader.DynamicMethodBody) receiver).getCodeBody().getNumberOfParameters();
                    Atom defFuncName = receiver.getName().getClassName();
                    Atom trampolineName = Atom.findOrCreateUnicodeAtom("self_trampoline_" + defFuncName +"_"+hashCode+ "(" + call.getNumberOfTotalParameters() + ")");

                    Map<Integer, Atom> names = HashMapFactory.make();
                    MethodReference tr = MethodReference.findOrCreate(receiver.getReference(),
                            trampolineName,
                            AstMethodReference.fnDesc);
                    PythonSummary x = new PythonSummary(tr, call.getNumberOfTotalParameters());
                    IClass filter = ((PythonInstanceMethodTrampoline) receiver).getRealClass();
                    int v = call.getNumberOfTotalParameters() + 1;
                    x.addStatement(PythonLanguage.Python.instructionFactory().GetInstruction(0, v, 1, FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom("$function"), PythonTypes.Root)));
                    int v0 = v + 1;
                    x.addStatement(PythonLanguage.Python.instructionFactory().CheckCastInstruction(1, v0, v, filter.getReference(), true));
                    int v1 = v + 2;
                    x.addStatement(PythonLanguage.Python.instructionFactory().GetInstruction(1, v1, 1, FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom("$self"), PythonTypes.Root)));

                    int i = 0;
                    int[] params = new int[Math.max(2, call.getNumberOfPositionalParameters() + 1)];
                    params[i++] = v0;
                    params[i++] = v1;
                    for (int j = 1; j < call.getNumberOfPositionalParameters(); j++) {
                        params[i++] = j + 1;
                    }

                    int ki = 0, ji = call.getNumberOfPositionalParameters() + 1;
                    Pair<String, Integer>[] keys = new Pair[0];
                    if (call.getKeywords() != null) {
                        keys = new Pair[call.getKeywords().size()];
                        for (String k : call.getKeywords()) {
                            names.put(ji, Atom.findOrCreateUnicodeAtom(k));
                            keys[ki++] = Pair.make(k, ji++);
                        }
                    }

                    int result = v1 + 1;
                    int except = v1 + 2;
                    CallSiteReference ref = new DynamicCallSiteReference(call.getCallSite().getDeclaredTarget(), 2);
                    x.addStatement(new PythonInvokeInstruction(2, result, except, ref, params, keys));

                    x.addStatement(new SSAReturnInstruction(3, result, false));

                    x.setValueNames(names);

                    codeBodies.put(key, new PythonSummarizedFunction(tr, x, receiver));
                }

                return codeBodies.get(key);
            } else if (receiver instanceof CAstAbstractModuleLoader.DynamicCodeBody
                    && ((CAstAbstractModuleLoader.DynamicCodeBody) receiver).getCodeBody().getNumberOfParameters() > realParaNum
                    && !caller.getMethod().getName().toString().startsWith("args_trampoline")
            ) {

                CalleeKey key = new CalleeKey(caller, receiver);
                if (!wrapperBodies.containsKey(key)){
                    AstMethod targetCodeBody = ((CAstAbstractModuleLoader.DynamicCodeBody) receiver).getCodeBody();
                    int defParaNum = targetCodeBody.getNumberOfParameters();
                    Atom defFuncName = targetCodeBody.getReference().getDeclaringClass().getName().getClassName();
                    Atom trampolineName = Atom.findOrCreateUnicodeAtom("args_trampoline_" + defFuncName + "_"
                            + hashCode + "(" + realParaNum + ")");
                    MethodReference tr = MethodReference.findOrCreate(receiver.getReference(),
                            trampolineName,
                            AstMethodReference.fnDesc);
                    PythonSummary summaryFunc = new PythonSummary(tr, call.getNumberOfConstParameters());

                    int currFreeVal = call.getNumberOfTotalParameters() + 1;
                    int iindex = 1;

                    // 取args
                    List<Integer> params = new LinkedList<>();
                    for (int i = 1; i <= call.getNumberOfPositionalParameters(); i++) {
                        params.add(i);
                    }

                    int currParam = call.getNumberOfPositionalParameters();
                    // 取kwargs
                    Map<Integer, Atom> names = HashMapFactory.make();
                    Set<String> constKeys = new HashSet<>();
                    List<Pair<String, Integer>> keys = new LinkedList<>();
                    if (call.getKeywords() != null) {
                        for (String k : call.getKeywords()) {
                            currParam++;
                            constKeys.add(k);
                            names.put(currParam, Atom.findOrCreateUnicodeAtom(k));
                            keys.add(Pair.make(k, currParam));
                        }
                    }


                    // 若Call中有*args，取v_a=args
                    if (call.getArgsVal() > 0) {
                        currParam++;
                        // 生成v_{real+1}=v_a.ref(0),v_{real+2}=v_a.ref(1), ..., v_{real+m}=v_a.ref(def-len(kw))
                        for (int i = 0; i < defParaNum - call.getNumberOfConstParameters(); i++) {
                            int refVal = currFreeVal++;
                            int idxVal = currFreeVal++;
                            int idx = i;
                            summaryFunc.addConstant(refVal, new ConstantValue(idx));
                            summaryFunc.addStatement(
                                    new PythonPropertyRead(iindex++, idxVal, currParam, refVal));
                            params.add(idxVal);
                        }
                    }


                    if (call.getKwargsVal() > 0) {
                        currParam++;
                        for (int i = 1; i < defParaNum; i++) {
                            CAstSourcePositionMap.Position position = ((CAstAbstractModuleLoader.DynamicCodeBody) receiver).getCodeBody().debugInfo().getParameterPosition(i);
                            if (position instanceof PythonSourcePosition) {
                                String argKey = ((PythonSourcePosition) position).getString();
                                if (!constKeys.contains(argKey)) {
                                    names.put(currFreeVal, Atom.findOrCreateUnicodeAtom(argKey));
                                    keys.add(Pair.make(argKey, currFreeVal));
                                    FieldReference field = FieldReference.findOrCreate(PythonTypes.Root, Atom.findOrCreateUnicodeAtom(argKey), PythonTypes.Root);
                                    summaryFunc.addStatement(PythonLanguage.Python.instructionFactory()
                                            .GetInstruction(iindex++, currFreeVal++, currParam, field));

                                }
                            }
                        }
                    }

                    int result = currFreeVal++;
                    int except = currFreeVal++;

                    CallSiteReference ref = new DynamicCallSiteReference(call.getCallSite().getDeclaredTarget(), 2);
//                Pair<String, Integer>[] keys = new Pair[0];
                    summaryFunc.addStatement(new PythonInvokeInstruction(iindex++, result, except, ref,
                            params.stream().mapToInt(Integer::valueOf).toArray(),
                            keys.toArray(new Pair[0])));
                    summaryFunc.addStatement(new SSAReturnInstruction(iindex++, result, false));
                    summaryFunc.setValueNames(names);

                    wrapperBodies.put(key, new PythonSummarizedFunction(tr, summaryFunc, receiver));
                }
                return wrapperBodies.get(key);
            }
        }

        return base.getCalleeTarget(caller, site, receiver);
    }

}
