package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.viz.DotUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestCalls1 extends TestPythonCallGraphShape {

    @Test
    public void testCalls1() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call1/calls1.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "calls1", "foo"));
        Assert.assertTrue(TestUtil.hasEdge(CG,  "foo", "id"));
        Assert.assertTrue(TestUtil.hasEdge(CG,  "self_trampoline_foo", "foo"));
    }


    @Test
    public void testCalls2() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call1/calls2.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "foo", "id"));
    }

    @Test
    public void testCalls3() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call1/calls3.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "foo", "call"));
    }

    @Test
    public void testCalls4() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call1/calls4.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "foo", "call"));
    }

    @Test
    public void testCalls5() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call1/calls5.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG, "call", "id"));
    }

    @Test
    public void testCalls6() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call1/calls6.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG, "foo", "id"));
    }

    @Test
    public void testCalls7() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("call1/calls7.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(CG,  "calls7", "nothing"));
    }

}
