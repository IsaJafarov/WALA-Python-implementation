package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.cast.python.global.SystemPath;
import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.viz.DotUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestMultiRel extends TestPythonCallGraphShape {

    @Before
    public void beforeTest() {
        SystemPath.getInstance().unlockAppPath();
    }

    @Test
    public void testFromRelImportModule() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/from_import.py",
                "modules/pkg1/subpkg1/moduleA.py",
                "modules/pkg1/subpkg1/__init__.py",
                "modules/pkg1/subpkg1/from_dot_import_module.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "from_dot_import_module", "func_module_a"));
    }

    @Test
    public void testRelModuleImportFunc() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/pkg1/subpkg1/moduleA.py",
                "modules/pkg1/subpkg1/__init__.py",
                "modules/pkg1/subpkg1/from_dot_module_import_func.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        TestUtil.dumpCG(builder, cg);
        Assert.assertTrue(TestUtil.hasEdge(cg,  "from_dot_module_import_func", "func_module_a"));
    }

    @Test
    public void testDoubleDotImportModule() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/multi2.py",
                "modules/pkg1/moduleD.py",
                "modules/pkg1/__init__.py",
                "modules/pkg1/subpkg1/__init__.py",
                "modules/pkg1/subpkg1/from_double_dot_import_module.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "from_double_dot_import_module", "func_module_d"));
    }

    @Test
    public void testRelG() throws WalaException, IllegalArgumentException, CancelException, IOException {

        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/multi2.py",
                "modules/pkg1/subpkg1/moduleA.py",
                "modules/pkg1/subpkg1/__init__.py",
                "modules/pkg1/subpkg2/import_double_dot_module_import_func.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "import_double_dot_module_import_func", "func_module_a"));
    }

    @Test
    public void testRelH() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/multi2.py",
                "modules/pkg1/subpkg1/moduleA.py",
                "modules/pkg1/subpkg1/__init__.py",
                "modules/pkg1/subpkg2/import_double_dot_pkg_import_func.py"
                );
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "import_double_dot_pkg_import_func", "func_module_a"));
    }

}
