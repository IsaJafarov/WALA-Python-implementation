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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestMultiStar extends TestPythonCallGraphShape {

    @Before
    public void beforeTest() {
        SystemPath.getInstance().unlockAppPath();
    }

    @Test
    public void testStarN3() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "caseStar/moduleN.py",
                "caseStar/moduleN3.py",
                "caseStar/moduleO.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Object[][] assertions = new Object[][]{
                new Object[]{ROOT, new String[]{"moduleN.py", "moduleN3.py", "moduleO.py"}},
                new Object[]{ "moduleN3.py",
                        new String[]{"funcN", "funcO"}},
        };

        verifyGraphAssertions(CG, assertions);
    }

    @Test
    public void testStarN4() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "caseStar/moduleN.py",
                "caseStar/moduleN3.py",
                "caseStar/moduleN4.py",
                "caseStar/moduleO.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Object[][] assertions = new Object[][]{
                new Object[]{ROOT, new String[]{"moduleN.py", "moduleN3.py", "moduleN4.py", "moduleO.py"}},
                new Object[]{
                        "moduleN3.py",
                        new String[]{"funcN", "funcO"}},
                new Object[]{ "moduleN4.py",
                        new String[]{"funcN", "funcO"}}
        };

        verifyGraphAssertions(CG, assertions);
    }

    @Test
    public void testStarN2() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "caseStar/moduleN2.py",
                "caseStar/moduleO.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Object[][] assertions = new Object[][]{
                new Object[]{ROOT, new String[]{"moduleN2.py", "moduleO.py"}},
                new Object[]{
                        "moduleN2.py",
                        new String[]{"funcO"}}
        };

        verifyGraphAssertions(CG, assertions);
    }

    @Test
    public void testStar2() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine(
                "modules/pkg1/__init__.py",
                "modules/pkg1/moduleD.py",
                "caseStar2/moduleN.py",
                "ana1.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Object[][] assertions = new Object[][]{
                new Object[]{ROOT, new String[]{"moduleN.py", "moduleD.py", "ana1.py"}},
                new Object[]{
                        "ana1.py",
                        new String[]{"Banana"}},
                new Object[]{
                        "Banana",
                        new String[]{"MAGIC_EQ"}}
        };

        verifyGraphAssertions(CG, assertions);
    }

}
