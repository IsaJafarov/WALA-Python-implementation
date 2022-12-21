package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

public class TestFor extends TestPythonCallGraphShape {

    protected static final Object[][] assertionsFor1 = new Object[][]{
            new Object[]{ROOT, new String[]{"for1.py"}},
            new Object[]{
                    "for1.py",
                    new String[]{"f1", "f2", "f3"}}
    };

    @Test
    public void testFor1() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
        CallGraph CG = process("for/for1.py");
        verifyGraphAssertions(CG, assertionsFor1);
    }

    protected static final Object[][] assertionsFor2 = new Object[][]{
            new Object[]{ROOT, new String[]{"for2.py"}},
            new Object[]{
                    "for2.py",
                    new String[]{"doit"}},
            new Object[]{
                    "doit",
                    new String[]{"f1", "f2", "f3"}}
    };

    @Test
    public void testFor2() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
        CallGraph CG = process("for/for2.py");
        verifyGraphAssertions(CG, assertionsFor2);
    }

    protected static final Object[][] assertionsFor3 = new Object[][]{
            new Object[]{ROOT, new String[]{"for3.py"}},
            new Object[]{
                    "for3.py",
                    new String[]{"f1", "f2", "f3"}}
    };

    @Test
    public void testFor3() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
        CallGraph CG = process("for/for3.py");
        verifyGraphAssertions(CG, assertionsFor3);
    }


}
