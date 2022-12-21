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

public class TestContextSens extends TestPythonCallGraphShape {

    @Test
    public void testlib() throws WalaException, IOException, CancelException {
        PythonAnalysisEngine<?> engine = makeEngine("contentsensitive/id.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        TestUtil.dumpCG(builder, cg);
//        Assert.assertTrue(TestUtil.hasEdge(cg, "func2", "func3"));
    }

}
