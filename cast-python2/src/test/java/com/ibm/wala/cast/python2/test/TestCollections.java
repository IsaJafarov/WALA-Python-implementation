package com.ibm.wala.cast.python2.test;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

public class TestCollections extends TestPythonCallGraphShape {

	@Ignore
    @Test
	public void testCollections1() throws ClassHierarchyException, IllegalArgumentException, CancelException, IOException {
		CallGraph CG = process("collections.py");
		System.err.println(CG);
		//verifyGraphAssertions(CG, assertionsCalls1);
	}
	

}
