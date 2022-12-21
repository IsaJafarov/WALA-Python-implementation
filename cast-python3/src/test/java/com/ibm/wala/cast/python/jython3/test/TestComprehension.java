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

public class TestComprehension extends TestPythonCallGraphShape {

    protected static final Object[][] assertionsComp1 = new Object[][]{
            new Object[]{ROOT, new String[]{"comp1.py"}},
            new Object[]{
                    "comp1.py",
                    new String[]{
                            "comprehension1",
                            "comprehension3"}},
            new Object[]{
                    "comprehension1",
                    new String[]{
                            "comprehension1"
                    }},
            new Object[]{
                    "comprehension3",
                    new String[]{
                            "comprehension3"
                    }},
            new Object[]{
                    "comprehension1",
                    new String[]{
                            "f1",
                            "f2",
                            "f3"
                    }
            },
            new Object[]{
                    "comprehension3",
                    new String[]{
                            "lambda1",
                            "lambda1",
                            "lambda1"
                    }
            }

    };

    @Test
    public void testComp1() throws WalaException, IllegalArgumentException, CancelException, IOException {
        PythonAnalysisEngine<?> e = new PythonAnalysisEngine<Void>() {
            @Override
            public Void performAnalysis(PropagationCallGraphBuilder builder) throws CancelException {
                assert false;
                return null;
            }
        };
        e.setModuleFiles(Collections.singleton(getScript("comprehension/comp1.py")));
        PropagationCallGraphBuilder cgBuilder = (PropagationCallGraphBuilder) e.defaultCallGraphBuilder();
        CallGraph CG = cgBuilder.makeCallGraph(cgBuilder.getOptions());
        verifyGraphAssertions(CG, assertionsComp1);
    }

    protected static final Object[][] assertionsComp3 = new Object[][]{
		new Object[]{ROOT, new String[]{"comp3.py"}},
		new Object[]{
			"comp3.py",
			new String[]{
				"comprehension1",
				"comprehension3"}},
		new Object[]{
				"comprehension1",
				new String[]{
						"comprehension1"
				}},
		new Object[]{
				"comprehension3",
				new String[]{
						"comprehension3"
				}},
		new Object[]{
				"comprehension1",
				new String[]{
						"g1",
						"g2",
						"f1",
						"f2",
						"f3",
						"lambda1"}},
		new Object[]{
				"lambda1",
				new String[]{
						"f1",
						"f2",
						"f3"}},
		new Object[]{
				"comprehension3",
				new String[]{
						"lambda1"}},
    };

    @Test
    public void testComp3() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> e = new PythonAnalysisEngine<Void>() {
			@Override
			public Void performAnalysis(PropagationCallGraphBuilder builder) throws CancelException {
				assert false;
				return null;
			}
		};
		e.setModuleFiles(Collections.singleton(getScript("comprehension/comp3.py")));
		PropagationCallGraphBuilder cgBuilder = (PropagationCallGraphBuilder) e.defaultCallGraphBuilder();
		CallGraph CG = cgBuilder.makeCallGraph(cgBuilder.getOptions());
		verifyGraphAssertions(CG, assertionsComp3);
	}

}
