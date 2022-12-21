package com.ibm.wala.cast.python.jython3.test;

import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ssa.IRView;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.ibm.wala.viz.DotUtil;

import java.util.Collection;
import java.util.LinkedList;

import static com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil.getShortName;

public class TestUtil {
    public static void dumpCG(PropagationCallGraphBuilder builder, CallGraph CG) throws WalaException {
        CAstCallGraphUtil.AVOID_DUMP = false;
        CAstCallGraphUtil.dumpCG((SSAContextInterpreter) builder.getContextInterpreter(), builder.getPointerAnalysis(), CG);
        DotUtil.dotify(CG, null, PDFTypeHierarchy.DOT_FILE, "callgraph.pdf", "dot");
    }

    public static String getSSA(PropagationCallGraphBuilder builder, CallGraph cg) {
        StringBuilder sb = new StringBuilder();
        for (CGNode N : cg) {
            boolean fst = true;
            for (CGNode n : Iterator2Iterable.make(cg.getSuccNodes(N))) {
                if (fst) fst = false;
                else sb.append(", ");
                sb.append(getShortName(n));
            }
            IRView ir = ((SSAContextInterpreter) builder.getContextInterpreter()).getIRView(N);
            if (ir != null) {
                sb.append(ir);
            }
        }
        return sb.toString();
    }

    public static boolean hasNEdge(CallGraph CG, String from, int num) {
        int cnt = 0;
        for (CGNode N : CG) {
            if (getShortName(N).startsWith(from)) {
                for (CGNode n : Iterator2Iterable.make(CG.getSuccNodes(N))) {
                    cnt++;
                }
            }
        }
        return cnt >= num;
    }

    public static Collection<CGNode> getNodes(CallGraph CG, String node) {
        Collection<CGNode> cgNodes = new LinkedList<>();
        for (CGNode n : CG) {
            if (getShortName(n).startsWith(node) || getShortName(n).endsWith(node)) {
                cgNodes.add(n);
            }
        }
        return cgNodes;
    }

    public static boolean hasEdge(CallGraph CG, String from, String to) {
        for (CGNode N : CG) {
            if (getShortName(N).startsWith(from)) {
                boolean fst = true;
                for (CGNode n : Iterator2Iterable.make(CG.getSuccNodes(N))) {
                    if (fst) fst = false;
                    if (getShortName(n).startsWith(to)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
