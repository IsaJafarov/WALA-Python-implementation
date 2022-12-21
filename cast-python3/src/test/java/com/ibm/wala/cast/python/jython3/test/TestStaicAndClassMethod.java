package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.viz.DotUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestStaicAndClassMethod extends TestPythonCallGraphShape {
    @Test
    public void testClass1() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("static/class_method.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "class_func", "func"));
    }

    @Test
    public void testClass2() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("static/class_method2.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "class_func", "func"));
    }

    @Test
    public void testClass3() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("static/class_method3.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "func4", "cls_trampoline_func5"));
        Assert.assertTrue(TestUtil.hasEdge(cg,  "cls_trampoline_func5", "func5"));
    }


    @Test
    public void testClass4() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("static/class_method4.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "func4", "cls_trampoline_func5"));
        Assert.assertTrue(TestUtil.hasEdge(cg,  "cls_trampoline_func5", "func5"));
    }


    @Test
    public void testClass5() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("static/class_method5.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "func4", "cls_trampoline_func5"));
        Assert.assertTrue(TestUtil.hasEdge(cg,  "cls_trampoline_func5", "func5"));
    }

    @Test
    public void testClass6() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("static/class_method6.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "class_func2", "func"));
    }

    @Test
    public void testStatic1() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("static/static_method.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "static_func1", "func"));
    }

    @Test
    public void testStatic2() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> engine = makeEngine("static/static_method2.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        Assert.assertTrue(TestUtil.hasEdge(cg, "static_func1", "func"));
    }

}
