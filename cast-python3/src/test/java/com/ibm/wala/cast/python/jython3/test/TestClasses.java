package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.cast.python.global.SystemPath;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestClasses extends TestPythonCallGraphShape {

	@Before
	public void beforeTest() {
		SystemPath.getInstance().unlockAppPath();
	}

	@Test
	public void testClasses1() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("clazz/classes1.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
		Assert.assertTrue(hasEdge(cg,"classes1.py","Outer"));
		Assert.assertTrue(hasEdge(cg,"classes1.py","Inner"));
		Assert.assertTrue(hasEdge(cg,"classes1.py","self_trampoline_foo"));
		Assert.assertTrue(hasNEdge(cg,"classes1.py",4));
		Assert.assertTrue(hasEdge(cg,"self_trampoline_foo","foo"));
	}

	@Test
	public void testClasses2() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("clazz/classes2.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
		TestUtil.dumpCG(builder, cg);
		Assert.assertTrue(hasEdge(cg,"fakeRootMethod","classes2"));
		Assert.assertTrue(hasEdge(cg,"classes2","fc"));
		Assert.assertTrue(hasEdge(cg,"classes2","Ctor"));
		Assert.assertTrue(hasEdge(cg,"classes2","self_trampoline_get"));
		Assert.assertTrue(hasEdge(cg,"Ctor","__init__"));
		Assert.assertTrue(hasEdge(cg,"self_trampoline_get","get"));
		Assert.assertTrue(hasEdge(cg,"get","fa"));
		Assert.assertTrue(hasEdge(cg,"get","fb"));
		Assert.assertTrue(hasEdge(cg,"get","fc"));
	}

	 protected static final Object[][] assertionsClasses3 = new Object[][] {
		    new Object[] { ROOT, new String[] { "script classes3.py" } },
		    new Object[] {
		        "script classes3.py",
		        new String[] { "script classes3.py/Ctor",
		        		"$script classes3.py/Ctor/get:trampoline2",
		        		"script classes3.py/SubCtor",
		        		"script classes3.py/OtherSubCtor"}
		    },
		    new Object[] {
			        "script classes3.py",
			        new String[] { "script classes3.py/Ctor",
			        		"$script classes3.py/Ctor/get:trampoline2"}
			},
		    new Object[] {
			        "script classes3.py/Ctor",
			        new String[] { "script classes3.py/Ctor/__init__" }
		    },
		    new Object[] {
			        "script classes3.py/SubCtor",
			        new String[] { "script classes3.py/SubCtor/__init__" }
		    },
		    new Object[] {
			        "script classes3.py/OtherSubCtor",
			        new String[] { "script classes3.py/OtherSubCtor/__init__" }
		    },
		    new Object[] {
			        "script classes3.py/SubCtor/__init__",
			        new String[] { "$script classes3.py/Ctor/__init__:trampoline4" }
		    },
		    new Object[] {
			        "$script classes3.py/Ctor/__init__:trampoline4",
			        new String[] { "script classes3.py/Ctor/__init__" }
		    },
		    new Object[] {
			        "script classes3.py/OtherSubCtor/__init__",
			        new String[] { "$script classes3.py/Ctor/__init__:trampoline4" }
		    }
	 };
	 
	@Test
	public void testClasses3() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("clazz/classes3.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
		Assert.assertTrue(hasEdge(cg,"classes3","Ctor"));
		Assert.assertTrue(hasEdge(cg,"classes3","SubCtor"));
		Assert.assertTrue(hasEdge(cg,"classes3","OtherSubCtor"));
		Assert.assertTrue(hasEdge(cg,"classes3","self_trampoline_get"));
		Assert.assertTrue(hasEdge(cg,"Ctor","__init__"));
		Assert.assertTrue(hasEdge(cg,"SubCtor","__init__"));
		Assert.assertTrue(hasEdge(cg,"OtherSubCtor","__init__"));
		Assert.assertTrue(hasEdge(cg,"__init__","self_trampoline"));
	}


	@Test
	public void testClasses4() throws WalaException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("clazz/classes4.py");
		PropagationCallGraphBuilder builder = (PropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph cg = builder.makeCallGraph(engine.getOptions(), new NullProgressMonitor());
		TestUtil.dumpCG(builder,cg);
	}
}
