package com.ibm.wala.cast.python.client;

import java.io.IOException;
import java.util.*;

import com.ibm.wala.cast.ipa.callgraph.AstCFAPointerKeys;
import com.ibm.wala.cast.ipa.callgraph.AstContextInsensitiveSSAContextInterpreter;
import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.python.global.XmlSummaries;
import com.ibm.wala.cast.python.ipa.callgraph.PythonConstructorTargetSelector;
import com.ibm.wala.cast.python.ipa.callgraph.PythonSSAPropagationCallGraphBuilder;
import com.ibm.wala.cast.python.ipa.callgraph.PythonScopeMappingInstanceKeys;
import com.ibm.wala.cast.python.ipa.callgraph.PythonTrampolineTargetSelector;
import com.ibm.wala.cast.python.ipa.summaries.BuiltinFunctions;
import com.ibm.wala.cast.python.ipa.summaries.PythonComprehensionTrampolines;
import com.ibm.wala.cast.python.ipa.summaries.PythonSuper;
import com.ibm.wala.cast.python.loader.PythonAnalysisScope;
import com.ibm.wala.cast.python.loader.PythonLoaderFactory;
import com.ibm.wala.cast.python.loader.PythonSyntheticClass;
import com.ibm.wala.cast.python.module.PyLibURLModule;
import com.ibm.wala.cast.python.module.PyScriptModule;
import com.ibm.wala.cast.python.types.PythonTypes;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.cast.util.Util;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.ModuleEntry;
import com.ibm.wala.client.AbstractAnalysisEngine;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyClassTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ContextInsensitiveSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.cha.SeqClassHierarchyFactory;
import com.ibm.wala.ipa.summaries.BypassClassTargetSelector;
import com.ibm.wala.ipa.summaries.BypassMethodTargetSelector;
import com.ibm.wala.ipa.summaries.BypassSyntheticClassLoader;
import com.ibm.wala.ipa.summaries.XMLMethodSummaryReader;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SSAOptions.DefaultValues;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.WalaRuntimeException;

public abstract class PythonAnalysisEngine<T>
        extends AbstractAnalysisEngine<InstanceKey, PythonSSAPropagationCallGraphBuilder, T> {

    private static Class<? extends PythonLoaderFactory> loaders;

    public static void setLoaderFactory(Class<? extends PythonLoaderFactory> lf) {
        loaders = lf;
    }

    private final PythonLoaderFactory loader;

    private final IRFactory<IMethod> irs = AstIRFactory.makeDefaultFactory();

    private final List<String> syntheticXMLs = new LinkedList<>();

    public PythonAnalysisEngine(String[] xmls) {
        super();
        PythonLoaderFactory f;
        try {
            f = loaders.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            f = null;
            assert false : e.getMessage();
        } catch (NullPointerException e) {
            f = null;
            assert false : "PythonLoaderFactory is null";
        }
        loader = f;
        syntheticXMLs.addAll(Arrays.asList(xmls));
    }

    public PythonAnalysisEngine() {
        super();
        PythonLoaderFactory f;
        try {
            f = loaders.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            f = null;
            assert false : e.getMessage();
        } catch (NullPointerException e) {
            f = null;
            assert false : "PythonLoaderFactory is null";
        }
        loader = f;
        syntheticXMLs.add("flask.xml");
        syntheticXMLs.add("pandas.xml");
        syntheticXMLs.add("functools.xml");
    }

    @Override
    public void setModuleFiles(Collection<? extends Module> moduleFiles) {
        this.moduleFiles=new LinkedList<>();
        List<Module> moduleList=new LinkedList<>();
        for (Module o : moduleFiles) {
            if (o instanceof PyLibURLModule){
                moduleList.add(o);
            }
        }
        for (Module o : moduleFiles) {
            if (o instanceof PyScriptModule){
                moduleList.add(o);
            }
        }
        this.moduleFiles=moduleList;
    }

    @Override
    public void buildAnalysisScope() throws IOException {
        scope = new PythonAnalysisScope();

        for (Module o : moduleFiles) {
            scope.addToScope(PythonTypes.pythonLoader, o);
        }
    }

    @Override
    public IClassHierarchy buildClassHierarchy() {
        // 添加函数摘要, 如: <subprocess/function/call>

        for (String xmlFile:syntheticXMLs){
            XMLMethodSummaryReader xml = new XMLMethodSummaryReader(
                    getClass().getClassLoader().getResourceAsStream(xmlFile), scope);
            for (TypeReference t : xml.getAllocatableClasses()) {
                XmlSummaries.getInstance().add(t.getName().toString());
            }
        }
        try {
            IClassHierarchy cha = SeqClassHierarchyFactory.make(scope, loader);
            Util.checkForFrontEndErrors(cha);
            setClassHierarchy(cha);
            return cha;
        } catch (ClassHierarchyException e) {
            e.printStackTrace(System.err);
            assert false : e;
            return null;
        } catch (WalaException e) {
            throw new WalaRuntimeException(e.getMessage());
        }
    }

    protected void addSummaryBypassLogic(AnalysisOptions options, String summary) {
        IClassHierarchy cha = getClassHierarchy();
        XMLMethodSummaryReader xml = new XMLMethodSummaryReader(getClass().getClassLoader().getResourceAsStream(summary), scope);
        for (TypeReference t : xml.getAllocatableClasses()) {
            BypassSyntheticClassLoader ldr = (BypassSyntheticClassLoader) cha.getLoader(scope.getSyntheticLoader());
            ldr.registerClass(t.getName(), new PythonSyntheticClass(t, cha));
            XmlSummaries.getInstance().add(t.getName().toString());
        }

        MethodTargetSelector targetSelector = options.getMethodTargetSelector();
        targetSelector = new BypassMethodTargetSelector(targetSelector, xml.getSummaries(), xml.getIgnoredPackages(), cha);
        options.setSelector(targetSelector);

        ClassTargetSelector cs =
                new BypassClassTargetSelector(options.getClassTargetSelector(),
                        xml.getAllocatableClasses(),
                        cha,
                        cha.getLoader(scope.getSyntheticLoader()));
        options.setSelector(cs);
    }

    protected void addBypassLogic(IClassHierarchy cha, AnalysisOptions options) {
        options.setSelector(
                new PythonTrampolineTargetSelector(
                        new PythonConstructorTargetSelector(
                                new PythonComprehensionTrampolines(
                                        options.getMethodTargetSelector()))));

        BuiltinFunctions builtins = new BuiltinFunctions(cha);
        options.setSelector(
                builtins.builtinClassTargetSelector(
                        options.getClassTargetSelector()));

        // 添加函数摘要, 如: <subprocess/function/call>
        for (String xml:syntheticXMLs){
            addSummaryBypassLogic(options, xml);
        }
    }

    // FIXME path/scriptname
    private String scriptName(Module m) {
        String path = ((ModuleEntry) m).getName();
        return "Lscript " + path;
    }

    @Override
    protected Iterable<Entrypoint> makeDefaultEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
//        Set<Entrypoint> result = HashSetFactory.make();
        List<Entrypoint> result = new LinkedList();
        for (Module m : moduleFiles) {
            IClass entry = cha.lookupClass(TypeReference.findOrCreate(PythonTypes.pythonLoader, TypeName.findOrCreate(scriptName(m))));
            assert entry != null : "bad root name " + scriptName(m) + ":\n" + cha;
            MethodReference er = MethodReference.findOrCreate(entry.getReference(), AstMethodReference.fnSelector);
            result.add(new DefaultEntrypoint(er, cha));
        }
        return result;
    }


    @Override
    protected PythonSSAPropagationCallGraphBuilder getCallGraphBuilder(IClassHierarchy cha, AnalysisOptions options, IAnalysisCacheView cache2) {
        IAnalysisCacheView cache = new AnalysisCacheImpl(irs, options.getSSAOptions());

        options.setSelector(new ClassHierarchyClassTargetSelector(cha));
        options.setSelector(new ClassHierarchyMethodTargetSelector(cha));

        addBypassLogic(cha, options);

        options.setUseConstantSpecificKeys(true);

        SSAOptions ssaOptions = options.getSSAOptions();
        ssaOptions.setDefaultValues(new DefaultValues() {
            @Override
            public int getDefaultValue(SymbolTable symtab, int valueNumber) {
                return symtab.getNullConstant();
            }
        });
        options.setSSAOptions(ssaOptions);

        PythonSSAPropagationCallGraphBuilder builder =
                makeBuilder(cha, options, cache);

        AstContextInsensitiveSSAContextInterpreter interpreter = new AstContextInsensitiveSSAContextInterpreter(options, cache);
        builder.setContextInterpreter(interpreter);

        builder.setContextSelector(new nCFAContextSelector(1, new ContextInsensitiveSelector()));

        builder.setInstanceKeys(new PythonScopeMappingInstanceKeys(builder, new ZeroXInstanceKeys(options, cha, interpreter, ZeroXInstanceKeys.ALLOCATIONS)));

        new PythonSuper(cha).handleSuperCalls(builder, options);

        return builder;
    }

    protected PythonSSAPropagationCallGraphBuilder makeBuilder(IClassHierarchy cha, AnalysisOptions options,
                                                               IAnalysisCacheView cache) {
        return new PythonSSAPropagationCallGraphBuilder(cha, options, cache, new AstCFAPointerKeys());
    }

    public abstract T performAnalysis(PropagationCallGraphBuilder builder) throws CancelException;

}