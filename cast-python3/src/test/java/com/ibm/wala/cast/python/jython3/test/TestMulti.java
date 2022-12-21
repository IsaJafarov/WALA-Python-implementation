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

public class TestMulti extends TestPythonCallGraphShape {

    @Before
    public void beforeTest() {
        SystemPath.getInstance().unlockAppPath();
    }

    protected static final Object[][] assertionsMulti1 = new Object[][]{
            new Object[]{ROOT, new String[]{"script from_import.py", "script multi2.py"}},
            new Object[]{"script from_import.py", new String[]{"script multi2.py/silly"}},
            new Object[]{"script multi2.py/silly", new String[]{"script multi2.py/silly/inner"}},
    };

    @Test
    public void testMulti1() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("modules/multi2.py", "modules/from_import.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "silly", "inner"));
    }

    @Test
    public void testMulti2() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine( "modules/multi2.py", "modules/from_import_as.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "silly", "inner"));
    }

    /**
     * 测试传递性
     * @throws WalaException
     * @throws IllegalArgumentException
     * @throws CancelException
     * @throws IOException
     */
    @Test
    public void testImportImportModule() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("modules/import_import_a/module1.py", "modules/import_import_a/module2.py", "modules/import_import_a/module3.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "module3", "funcO"));
        Assert.assertTrue(TestUtil.hasEdge(cg,  "module2", "funcO"));
    }


//    @Test
//    public void testMulti4() throws WalaException, IllegalArgumentException, CancelException, IOException {
//        PythonAnalysisEngine<?> engine = makeEngine("multi4.py", "multi7.py");
//        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
//        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
//        CAstCallGraphUtil.AVOID_DUMP = false;
//        CAstCallGraphUtil.dumpCG((SSAContextInterpreter) builder.getContextInterpreter(), builder.getPointerAnalysis(), CG);
//        DotUtil.dotify(CG, null, PDFTypeHierarchy.DOT_FILE, "callgraph.pdf", "dot");
//    }
}
