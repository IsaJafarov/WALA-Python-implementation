package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.cast.python.global.SystemPath;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestMultiPkg extends TestPythonCallGraphShape {

    @Before
    public void beforeTest() {
        SystemPath.getInstance().unlockAppPath();
    }

    // test pkg
    @Test
    public void testFromPkgDotModuleImport() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("modules/from_pkg_dot_module_import.py",
                "modules/pkg1/__init__.py",
                "modules/pkg1/moduleI.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "from_pkg_dot_module_import", "silly"));
        Assert.assertTrue(TestUtil.hasEdge(cg,  "silly", "inner"));
    }



    @Test
    public void testImportPkgDotModule() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("modules/pkg1/__init__.py", "modules/pkg1/moduleD.py", "modules/import_pkg_dot_module.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "import_pkg_dot_module", "func_module_d"));
    }

    @Test
    public void testImportPkgDotModuleAs() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("modules/pkg1/__init__.py", "modules/pkg1/moduleD.py", "modules/import_pkg_dot_module_as.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "import_pkg_dot_module_as", "func_module_d"));
    }


    @Test
    public void testFromPkgImportFunc() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("modules/pkg1/__init__.py", "modules/pkg1/moduleD.py", "modules/from_pkg_import_func.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "from_pkg_import_func", "func_module_d"));
    }

    @Test
    public void testFromInitImportFunc() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("modules/pkg1/__init__.py", "modules/pkg1/moduleD.py", "modules/pkg1/subpkg1/__init__.py", "modules/pkg1/subpkg1/moduleA.py", "modules/from_init_import_func.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "from_init_import_func", "func_module_d"));
    }

    @Test
    public void testImportPkg1() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/pkg1/__init__.py",
                "modules/pkg1/moduleD.py",
                "modules/import_pkg1.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "import_pkg1", "func_module_d"));
    }

    @Test
    public void testImportPkg2() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/pkg1/__init__.py",
                "modules/pkg1/moduleD.py",
                "modules/import_pkg2.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "import_pkg2", "func_module_d"));
    }

    @Test
    public void testFromPkgImportModule() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/pkg1/__init__.py",
                "modules/pkg1/moduleD.py",
                "modules/from_pkg_import_module.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "from_pkg_import_module", "func_module_d"));
    }

    @Test
    public void testSubPkg1() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/pkg1/__init__.py",
                "modules/pkg1/subpkg1/__init__.py",
                "modules/pkg1/subpkg1/moduleA.py",
                "modules/pkg1/subpkg2/__init__.py",
                "modules/pkg1/subpkg2/import_double_dot_module_import_func.py",
                "modules/import_subpkg1.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "import_subpkg1", "func_module_a"));
    }

    @Test
    public void testSubPkg2() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/pkg1/__init__.py",
                "modules/pkg1/subpkg1/__init__.py",
                "modules/pkg1/subpkg1/moduleA.py",
                "modules/pkg1/subpkg2/__init__.py",
                "modules/pkg1/subpkg2/import_double_dot_module_import_func.py",
                "modules/import_subpkg2.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "import_subpkg2", "func_module_a"));
    }

    @Test
    public void testSubPkg3() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/pkg1/__init__.py",
                "modules/pkg1/subpkg1/__init__.py",
                "modules/pkg1/subpkg1/moduleA.py",
                "modules/pkg1/subpkg2/__init__.py",
                "modules/pkg1/subpkg2/import_double_dot_module_import_func.py",
                "modules/import_subpkg3.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "import_subpkg3", "func_module_a"));
    }
}
