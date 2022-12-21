package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.cast.python.global.SystemPath;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.CancelException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

public class TestSlice extends TestPythonCallGraphShape {

	@Before
	public void beforeTest() {
		SystemPath.getInstance().unlockAppPath();
	}


	private static SSAAbstractInvokeInstruction find(IR ir, Function<SSAAbstractInvokeInstruction,Boolean> filter) {
		for(SSAInstruction inst : ir.getInstructions()) {
			if (inst instanceof SSAAbstractInvokeInstruction && filter.apply((SSAAbstractInvokeInstruction)inst)) {
				return (SSAAbstractInvokeInstruction)inst;
			}
		}
		
		return null;
	}
	
	@Test
	public void testSlice1() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
		CallGraph CG = process("slice/slice1.py");
		
		Collection<CGNode> nodes = getNodes(CG, "slice1.py");
		assert nodes.size() >= 1;
		
		CGNode node = nodes.iterator().next();
		DefUse du = node.getDU();
		IR script = node.getIR();

		SSAAbstractInvokeInstruction sliceImport = find(script, (SSAAbstractInvokeInstruction inst) -> {
			if (inst.getNumberOfUses() > 0) {
				int f = inst.getUse(0);
				SSAInstruction def = du.getDef(f);
				if (def instanceof SSANewInstruction) {
					return "Lwala/builtin/slice".equals(((SSANewInstruction)def).getConcreteType().getName().toString());				
				} 
			}
			
			return false;
			
		});
		
		assert sliceImport != null;		
	}	
	
	 protected static final Object[][] assertionsSlice2 = new Object[][] {
		    new Object[] { ROOT, new String[] {"slice2.py"} },
		    new Object[] {
					"slice2.py",
		        new String[] { "slice", "a", "b", "c", "d" } }
	 };

	@Test
	public void testSlice2() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> engine = makeEngine("slice/slice2.py");
		SSAPropagationCallGraphBuilder builder = (SSAPropagationCallGraphBuilder) engine.defaultCallGraphBuilder();
		CallGraph CG = builder.makeCallGraph(builder.getOptions());
		CAstCallGraphUtil.AVOID_DUMP = false;
		CAstCallGraphUtil.dumpCG((SSAContextInterpreter)builder.getContextInterpreter(), builder.getPointerAnalysis(), CG);
		verifyGraphAssertions(CG, assertionsSlice2);

		Collection<CGNode> nodes = getNodes(CG, "slice2.py");
		assert nodes.size() == 1;
	}
}
