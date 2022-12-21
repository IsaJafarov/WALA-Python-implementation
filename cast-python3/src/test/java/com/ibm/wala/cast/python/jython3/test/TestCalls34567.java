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

public class TestCalls34567 extends TestPythonCallGraphShape {

    @Test
    public void testCalls31() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call34567/calls31.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "call", "id"));
    }

    @Test
    public void testCalls32() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call34567/calls32.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "calls32", "nothing"));
    }

    @Test
    public void testCalls4() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call34567/calls4.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "foo", "id"));
        Assert.assertTrue(TestUtil.hasEdge(CG,  "calls4", "bad"));
    }

    @Test
    public void testCalls5() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call34567/calls5.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "calls5", "bad"));
    }

    @Test
    public void testCalls6() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call34567/calls6.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "calls6", "bad"));
    }

    @Test
    public void testCalls7() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call34567/calls7.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "calls7", "bad"));
        Assert.assertTrue(TestUtil.hasEdge(CG,  "calls7", "id"));
    }
}
