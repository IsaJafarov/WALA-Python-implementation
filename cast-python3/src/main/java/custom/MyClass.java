package custom;

import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.cast.python.loader.PythonLoaderFactory;
import com.ibm.wala.cast.python.module.PyScriptModule;
import com.ibm.wala.cast.python.util.PythonInterpreter;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.viz.DotUtil;

import java.io.IOException;
import java.util.*;

public class MyClass {

    private static int totalNumOfInstructions;
    private static int totalNumOfBranchingStatements = 0;

    private static int totalNumOfNodes = 0;
    private static int totalNumOfEdges = 0;

    private CallGraph generateGraph() throws IllegalAccessException, IOException, CancelException, WalaException, ClassNotFoundException, InstantiationException {
        Class<?> j3 = Class.forName("com.ibm.wala.cast.python3.loader.Python3LoaderFactory");
        PythonAnalysisEngine.setLoaderFactory((Class<? extends PythonLoaderFactory>) j3);
        Class<?> i3 = Class.forName("com.ibm.wala.cast.python3.util.Python3Interpreter");
        PythonInterpreter.setInterpreter((PythonInterpreter) i3.newInstance());

        List<Module> src = new LinkedList<>();
        addScriptsToSrc(src);

        PythonAnalysisEngine<Void> analysisEngine = new PythonAnalysisEngine<Void>() {
            @Override
            public Void performAnalysis(PropagationCallGraphBuilder builder) throws CancelException {
                assert false;
                return null;
            }
        };

        System.out.println("Source scripts: ");
        src.forEach(System.out::println);

        analysisEngine.setModuleFiles(src);
        SSAPropagationCallGraphBuilder builder = (SSAPropagationCallGraphBuilder) analysisEngine.defaultCallGraphBuilder();
        CallGraph callGraph = builder.makeCallGraph(builder.getOptions());

        CAstCallGraphUtil.AVOID_DUMP = false;
        CAstCallGraphUtil.dumpCG((SSAContextInterpreter) builder.getContextInterpreter(), builder.getPointerAnalysis(), callGraph);
        DotUtil.dotify(callGraph, null, PDFTypeHierarchy.DOT_FILE, "callgraph.pdf", "dot");

        return callGraph;
    }

    private void addScriptsToSrc(Collection<Module> src) {
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("BinaryToDecimal.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("graph_adjacency-matrix.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("HashMap.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("bfs.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("DepthFirstSearch.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("factorial.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("gcf.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("graph_adjacency-list.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("HexToDec.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("lcm.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("MaxHeap.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("Primes.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("Queues implementaion.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("TempConversion.py")));
        src.add(new PyScriptModule(getClass().getClassLoader().getResource("turtle_graphics.py")));
    }

    public void run() throws WalaException, IOException, CancelException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Scanner myObj = new Scanner(System.in);
        System.out.print("Select task:\n1) WALA Quick Start\n2) Visitors in WALA\n3) Working with Control Flow Graphs\nEnter task number: ");

        int input = myObj.nextInt();

        CallGraph graph = generateGraph();

        switch (input) {
            case 1:
                walaQuickStart(graph);
                break;
            case 2:
                visitorsInWALA(graph);
                break;
            case 3:
                workingWithCFG(graph);
                break;
            default:
                System.out.println("Wrong input");
        }
    }



    private void workingWithCFG(CallGraph graph) {
        graph.forEach(node -> {
            // get the IR for the call graph "node." Each node in this graph represents a method.
            IR ir = node.getIR();

            if (ir != null) {
                // the method whose body the IR represents.
                IMethod method = ir.getMethod();

                System.out.println("\nProcessing application method: " + method);
                checkCFG(ir);
//                }
            }
        });

        System.out.println("------------------------------------------------------------");
        System.out.println("a. Total number of CFG nodes in all CFGs = " + totalNumOfNodes);
        System.out.println("b. Number of edges in those CFGs = " + totalNumOfEdges);
        System.out.println("c. Average number of instructions per basic block = " + (float) totalNumOfInstructions / totalNumOfNodes);
        System.out.println("=============================================================");
    }

    private static void checkCFG(IR ir) {
        SSACFG cfg = ir.getControlFlowGraph();

        // Nodes
        for (int i = 0; i < cfg.getNumberOfNodes(); i++) {
            System.out.println(cfg.getBasicBlock(i));

            if (doesBasicBlockEndWithConditionalBranch(cfg, i)) {
                Iterator<ISSABasicBlock> it = cfg.getSuccNodes(cfg.getBasicBlock(i));
                while (it.hasNext())
                    System.out.println("\t" + it.next());
            }

        }
        totalNumOfNodes += cfg.getNumberOfNodes();

        // Instructions
        totalNumOfInstructions += cfg.getInstructions().length;

        // Edges
        int numberOfEdgesOfTheCFG = 0;
        for (int i = 0; i < cfg.getNumberOfNodes(); i++)
            numberOfEdgesOfTheCFG += cfg.getSuccNodeCount(cfg.getBasicBlock(i));
        totalNumOfEdges += numberOfEdgesOfTheCFG;
    }

    private static boolean doesBasicBlockEndWithConditionalBranch(SSACFG cfg, int nodeIndex) {

        List<SSAInstruction> instructions = cfg.getBasicBlock(nodeIndex).getAllInstructions();

        if (instructions.isEmpty())
            return false;

        return instructions.get(instructions.size() - 1) instanceof SSAConditionalBranchInstruction;
    }



    private void walaQuickStart(CallGraph graph) {
        graph.forEach(node -> {
            // get the IR for the call graph "node." Each node in this graph represents a method.
            IR ir = node.getIR();

            if (ir != null) {
                // the method whose body the IR represents.
                IMethod method = ir.getMethod();
                System.out.println("Processing instructions for application method: " + method);

                // the instructions in the IR.
                SSAInstruction[] instructions = ir.getInstructions();

                // "check" each one.
                Arrays.stream(instructions).filter(Objects::nonNull)
                        .forEach(MyClass::checkInstruction);
            }
        });

        printStatistics();
    }

    private static void checkInstruction(SSAInstruction instruction) {
        System.out.println(instruction);

        totalNumOfInstructions++;

        if (instruction instanceof SSAGotoInstruction ||
                instruction instanceof SSAConditionalBranchInstruction ||
                instruction instanceof SSASwitchInstruction) {
            totalNumOfBranchingStatements++;
        }
    }

    private static void printStatistics() {
        System.out.println("*****************************************************************************");
        System.out.println(">>>>> Total number of Instructions = " + totalNumOfInstructions);
        System.out.println(">>>>> Total number of Branching Statements = " + totalNumOfBranchingStatements);
    }




    private void visitorsInWALA(CallGraph graph) {
        graph.forEach(node -> {
            // get the IR for the call graph "node." Each node in this graph represents a method.
            IR ir = node.getIR();

            if (ir != null) {
                // the method whose body the IR represents.
                IMethod method = ir.getMethod();
                System.out.println("Processing instructions for application method: " + method);

                // the instructions in the IR.
                SSAInstruction[] instructions = ir.getInstructions();

                // "check" each one.
                for (SSAInstruction instruction : instructions) {
                    if (instruction != null) {
                        System.out.println(instruction);
                        ir.visitNormalInstructions(new MyVisitor());
                    }
                }
            }
        });
    }
}
