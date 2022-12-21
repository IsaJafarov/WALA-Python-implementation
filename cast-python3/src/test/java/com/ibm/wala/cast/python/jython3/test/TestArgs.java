package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestArgs extends TestPythonCallGraphShape {

    @Test
    public void testArgs2() throws WalaException, IOException, CancelException {
        PythonAnalysisEngine<?> engine = makeEngine("args/args2.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "func2", "func3"));
    }

    @Test
    public void testArgs3() throws WalaException, IOException, CancelException {
        PythonAnalysisEngine<?> engine = makeEngine("args/args3.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "func2", "func3"));
    }

    @Test
    public void testKwargs2() throws WalaException, IOException, CancelException {
        PythonAnalysisEngine<?> engine = makeEngine("args/kwargs2.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "func2", "func3"));
    }

    @Test
    public void testKwargs3() throws WalaException, IOException, CancelException {
        PythonAnalysisEngine<?> engine = makeEngine("args/kwargs3.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "func3", "func4"));
    }

    @Test
    public void testKwargs4() throws WalaException, IOException, CancelException {
        PythonAnalysisEngine<?> engine = makeEngine("args/kwargs4.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "func3", "func4"));
    }

    @Test
    public void testKwargs5() throws WalaException, IOException, CancelException {
        PythonAnalysisEngine<?> engine = makeEngine("args/kwargs5.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "func4", "func5"));
    }

    @Test
    public void testArgsKwargs3() throws WalaException, IOException, CancelException {
        // thread
        PythonAnalysisEngine<?> engine = makeEngine("args/arg_and_kwargs3.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "func2", "func3"));
    }

    @Test
    public void testArgsKwargs2() throws WalaException, IOException, CancelException {
        PythonAnalysisEngine<?> engine = makeEngine("args/arg_and_kwargs2.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        TestUtil.dumpCG(builder, cg);
        Assert.assertTrue(TestUtil.hasEdge(cg, "func5", "func6"));
    }


    @Test
    public void testArgsKwargs0() throws WalaException, IOException, CancelException {
        PythonAnalysisEngine<?> engine = makeEngine("args/arg_and_kwargs0.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        TestUtil.hasEdge(cg, "func1","func3");
        TestUtil.hasEdge(cg, "func2","func3");
    }
}
