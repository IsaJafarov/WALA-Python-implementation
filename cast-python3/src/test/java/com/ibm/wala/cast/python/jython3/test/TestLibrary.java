package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

public class TestLibrary extends TestPythonLibraryCallGraphShape {

    protected static final Object[][] assertionsLib1 = new Object[][]{
            new Object[]{ROOT, new String[]{"lib1.py", "es1", "es2", "es3"}},
            new Object[]{"script lib1.py/es1", new String[]{"turtle:turtle"}},
            new Object[]{"script lib1.py/es2", new String[]{"turtle:turtle"}},
            new Object[]{"script lib1.py/es3", new String[]{"turtle:turtle"}}
    };

    @Ignore
    @Test
    public void testLib1() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> e = new PythonAnalysisEngine<Void>() {
            @Override
            public Void performAnalysis(PropagationCallGraphBuilder builder) throws CancelException {
                assert false;
                return null;
            }
        };
        e.setModuleFiles(Collections.singleton(getScript("lib/lib1.py")));
        PropagationCallGraphBuilder cgBuilder = (PropagationCallGraphBuilder) e.defaultCallGraphBuilder();
        CallGraph CG = cgBuilder.makeCallGraph(cgBuilder.getOptions());
        TestUtil.dumpCG(cgBuilder, CG);
        verifyGraphAssertions(CG, assertionsLib1);
    }

    protected static final Object[][] assertionsLib2 = new Object[][]{
            new Object[]{ROOT,
                    new String[]{"script lib2.py",
                            "script lib2.py/Lib",
                            "$script lib2.py/Lib/es1:trampoline4",
                            "$script lib2.py/Lib/es2:trampoline3",
                            "$script lib2.py/Lib/es3:trampoline3"}},
            new Object[]{"$script lib2.py/Lib/es1:trampoline4",
                    new String[]{"script lib2.py/Lib/es1"}},
            new Object[]{"$script lib2.py/Lib/es2:trampoline3",
                    new String[]{"script lib2.py/Lib/es2"}},
            new Object[]{"$script lib2.py/Lib/es3:trampoline3",
                    new String[]{"script lib2.py/Lib/es3"}},
            new Object[]{"script lib2.py/Lib/es1",
                    new String[]{"turtle:turtle"}},
            new Object[]{"script lib2.py/Lib/es2",
                    new String[]{"turtle:turtle"}},
            new Object[]{"script lib2.py/Lib/es3",
                    new String[]{"turtle:turtle"}}
    };

    @Ignore
    @Test
    public void testLib2() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
        CallGraph CG = process("lib/lib2.py");
        System.err.println(CG);
        CG.forEach((n) -> {
            System.err.println(n.getIR());
        });
        verifyGraphAssertions(CG, assertionsLib2);
    }

    @Ignore
    @Test
    public void testLib4() throws WalaException, IOException, CancelException {
        PythonAnalysisEngine<?> engine = makeEngine("call1/calls1.py");
        PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
        CallGraph CG = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
        TestUtil.dumpCG(builder, CG);
        Assert.assertTrue(TestUtil.hasEdge(CG,  "calls1", "foo"));
    }

}
