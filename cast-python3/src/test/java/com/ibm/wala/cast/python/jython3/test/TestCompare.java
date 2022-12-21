package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.cfg.cdg.ControlDependenceGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.util.CancelException;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class TestCompare extends TestPythonCallGraphShape {

	 protected static final Object[][] assertionsCmp1 = new Object[][] {
		    new Object[] { ROOT, new String[] { "cmp1.py" } },
		    new Object[] {
		        "cmp1.py",
		        new String[] { "ctwo", "cthree", "cfour" } }
	 };

	 private void findReturns(CGNode n, Consumer<SSAReturnInstruction> act) {
		 n.getIR().iterateNormalInstructions().forEachRemaining(inst -> { 
			 if (inst instanceof SSAReturnInstruction) {
				 act.accept((SSAReturnInstruction)inst);
			 }
		 });
	 }
	 
	@Test
	public void testAssign1() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
		PythonAnalysisEngine<?> e = new PythonAnalysisEngine<Void>() {
			@Override
			public Void performAnalysis(PropagationCallGraphBuilder builder) throws CancelException {
				assert false;
				return null;
			}
		};
		e.setModuleFiles(Collections.singleton(getScript("compare/cmp1.py")));
		PropagationCallGraphBuilder cgBuilder = (PropagationCallGraphBuilder) e.defaultCallGraphBuilder();
		CallGraph CG = cgBuilder.makeCallGraph(cgBuilder.getOptions());
		verifyGraphAssertions(CG, assertionsCmp1);
	}


}
