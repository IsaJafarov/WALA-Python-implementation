package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.cast.python.module.PyLibURLModule;
import com.ibm.wala.cast.python.module.PyScriptModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.viz.DotUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TestLib  extends TestPythonCallGraphShape {
    @Test
    public void testLib1() throws IOException, CancelException, WalaException {

        Collection<Module> src = new LinkedList<>();
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("lib/from_import.py")));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/multi2.py")).getFile())));
        PythonAnalysisEngine<Void> analysisEngine = new PythonAnalysisEngine<Void>() {
            @Override
            public Void performAnalysis(PropagationCallGraphBuilder builder) throws CancelException {
                assert false;
                return null;
            }
        };
        analysisEngine.setModuleFiles(src);
        SSAPropagationCallGraphBuilder builder = (SSAPropagationCallGraphBuilder) analysisEngine.defaultCallGraphBuilder();
        CallGraph callGraph = builder.makeCallGraph(builder.getOptions());


        Assert.assertTrue(TestUtil.hasEdge(callGraph,  "silly", "inner"));
    }

    @Test
    public void testSubPkg1() throws WalaException, IllegalArgumentException, CancelException, IOException {
        Collection<Module> src = new LinkedList<>();
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("lib/import_subpkg1.py")));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/pkg1/__init__.py")).getFile())));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/pkg1/subpkg1/__init__.py")).getFile())));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/pkg1/subpkg1/moduleA.py")).getFile())));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/pkg1/subpkg2/__init__.py")).getFile())));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/pkg1/subpkg2/import_double_dot_module_import_func.py")).getFile())));
        PythonAnalysisEngine<Void> analysisEngine = new PythonAnalysisEngine<Void>() {
            @Override
            public Void performAnalysis(PropagationCallGraphBuilder builder) throws CancelException {
                assert false;
                return null;
            }
        };
        analysisEngine.setModuleFiles(src);
        SSAPropagationCallGraphBuilder builder = (SSAPropagationCallGraphBuilder) analysisEngine.defaultCallGraphBuilder();
        CallGraph callGraph = builder.makeCallGraph(builder.getOptions());

        Assert.assertTrue(TestUtil.hasEdge(callGraph,  "import_subpkg1", "func_module_a"));
    }

    @Test
    public void testSubPkg2() throws WalaException, IllegalArgumentException, CancelException, IOException {
        Collection<Module> src = new LinkedList<>();
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("lib/import_subpkg2.py")));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/pkg1/__init__.py")).getFile())));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/pkg1/subpkg1/__init__.py")).getFile())));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/pkg1/subpkg1/moduleA.py")).getFile())));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/pkg1/subpkg2/__init__.py")).getFile())));
        src.add(new PyLibURLModule(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("modules/pkg1/subpkg2/import_double_dot_module_import_func.py")).getFile())));
        PythonAnalysisEngine<Void> analysisEngine = new PythonAnalysisEngine<Void>() {
            @Override
            public Void performAnalysis(PropagationCallGraphBuilder builder) throws CancelException {
                assert false;
                return null;
            }
        };
        analysisEngine.setModuleFiles(src);
        SSAPropagationCallGraphBuilder builder = (SSAPropagationCallGraphBuilder) analysisEngine.defaultCallGraphBuilder();
        CallGraph callGraph = builder.makeCallGraph(builder.getOptions());

        Assert.assertTrue(TestUtil.hasEdge(callGraph,  "import_subpkg2", "func_module_a"));
    }
}
