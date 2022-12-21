package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

public class TestLambda extends TestPythonCallGraphShape {

    protected static final Object[][] assertionsLambda1 = new Object[][]{
            new Object[]{ROOT, new String[]{"lambda1.py"}},
            new Object[]{
                    "lambda1.py",
                    new String[]{"Foo", "self_trampoline_foo", "lambda1", "lambda2"}},
            new Object[]{
                    "self_trampoline_foo",
                    new String[]{"foo"}}
    };

    @Test
    public void testLambda1() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> e = new PythonAnalysisEngine<Void>() {
            @Override
            public Void performAnalysis(PropagationCallGraphBuilder builder) throws CancelException {
                assert false;
                return null;
            }
        };
        e.setModuleFiles(Collections.singleton(getScript("lambdas/lambda1.py")));
        PropagationCallGraphBuilder cgBuilder = (PropagationCallGraphBuilder) e.defaultCallGraphBuilder();
        CallGraph CG = cgBuilder.makeCallGraph(cgBuilder.getOptions());
        verifyGraphAssertions(CG, assertionsLambda1);
    }

    protected static final Object[][] assertionsLambda2 = new Object[][]{
            new Object[]{ROOT, new String[]{"lambda2.py"}},
            new Object[]{
                    "lambda2.py",
                    new String[]{"Foo", "self_trampoline", "lambda2", "lambda3"}},
            new Object[]{
                    "self_trampoline",
                    new String[]{"foo"}},
            new Object[]{
                    "lambda2",
                    new String[]{"lambda1"}},
            new Object[]{
                    "lambda3",
                    new String[]{"lambda1"}}
    };

    @Test
    public void testLambda2() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
        CallGraph CG = process("lambdas/lambda2.py");
        verifyGraphAssertions(CG, assertionsLambda2);
    }

    protected static final Object[][] assertionsLambda3 = new Object[][]{
            new Object[]{ROOT, new String[]{"lambda3.py"}},
            new Object[]{
                    "lambda3.py",
                    new String[]{"Foo", "self_trampoline"}},
            new Object[]{
                    "self_trampoline",
                    new String[]{"foo"}},
            new Object[]{
                    "foo",
                    new String[]{"lambda3"}},
            new Object[]{
                    "lambda3",
                    new String[]{"lambda1", "lambda2"}}
    };

    @Test
    public void testLambda3() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
        CallGraph CG = process("lambdas/lambda3.py");
        System.err.println(CG);
        verifyGraphAssertions(CG, assertionsLambda3);
    }
}
