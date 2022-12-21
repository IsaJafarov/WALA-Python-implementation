package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestCollections extends TestPythonCallGraphShape {

	@Test
	public void testList1() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("collections/listTest1.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
		Assert.assertTrue(TestUtil.hasEdge(cg,  "listTest", "func2"));
	}

	@Test
	public void testList2() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("collections/listTest2.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
//		TestUtil.dumpCG(builder, cg);
		Assert.assertTrue(TestUtil.hasEdge(cg,  "listTest", "func3"));
	}

	@Test
	public void testDict1() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("collections/dictTest1.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
//		TestUtil.dumpCG(builder, cg);
		Assert.assertTrue(TestUtil.hasEdge(cg,  "dictTest1", "func1"));
	}

	@Test
	public void testDict2() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("collections/dictTest2.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
//		TestUtil.dumpCG(builder, cg);
		Assert.assertTrue(TestUtil.hasEdge(cg,  "dictTest2", "func1"));
	}

	@Test
	public void testTuple() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("collections/tupleTest1.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
//		TestUtil.dumpCG(builder, cg);
		Assert.assertTrue(TestUtil.hasEdge(cg,  "tupleTest1", "func2"));
	}
}
