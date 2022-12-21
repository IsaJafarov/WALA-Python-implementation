package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.cast.python.global.SystemPath;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.IRView;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.Iterator2Iterable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.regex.Pattern;

import static com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil.getShortName;

public class TestAssign extends TestPythonCallGraphShape {

	@Before
	public void beforeTest() {
		SystemPath.getInstance().unlockAppPath();
	}
	@Test
	public void testAssign1() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("assign/assign1.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
		Assert.assertTrue(TestUtil.hasEdge(cg,"assign1.py","f"));
		Assert.assertTrue(TestUtil.hasEdge(cg,"assign1.py","g"));
		Assert.assertTrue(TestUtil.hasEdge(cg,"f","a"));
		Assert.assertTrue(TestUtil.hasEdge(cg,"g","a"));
	}

	@Test
	public void testAssign2() throws WalaException, IllegalArgumentException, CancelException, IOException {

		PythonAnalysisEngine<?> engine = makeEngine("assign/assign2.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
		String ssa=TestUtil.getSSA(builder, cg).replace("\n"," ");
		Assert.assertTrue(Pattern.matches(".*putfield v1.*FIELD_TEST.*", ssa));
		Assert.assertTrue(Pattern.matches(".*putfield v1.*CLASSBBBBB.*", ssa));
		Assert.assertTrue(Pattern.matches(".*putfield v49.*CLASSCCCCCCC, FIELD_CCCC.*", ssa));
	}

}

