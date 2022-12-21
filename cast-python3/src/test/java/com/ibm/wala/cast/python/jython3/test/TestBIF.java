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

public class TestBIF extends TestPythonCallGraphShape {

    @Before
    public void beforeTest() {
        SystemPath.getInstance().unlockAppPath();
    }

    // test pkg
    @Test
    public void testFromPkgDotModuleImport() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("BIFs/bif_string.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "bif_string", "str"));
    }

    @Test
    public void testPkgSummaries() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("BIFs/func_summaries.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg,  "func_summaries", "system"));
    }
}
