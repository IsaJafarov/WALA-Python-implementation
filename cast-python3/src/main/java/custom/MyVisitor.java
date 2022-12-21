package custom;

import com.ibm.wala.cast.python.ssa.PythonInstructionVisitor;
import com.ibm.wala.cast.python.ssa.PythonInvokeInstruction;
import com.ibm.wala.ssa.*;

public class MyVisitor extends SSAInstruction.Visitor implements PythonInstructionVisitor {

    public void visitSwitch(SSASwitchInstruction instruction) {
        System.out.println("> Visiting Switch Instruction");
    }

    public void visitPythonInvoke(PythonInvokeInstruction inst) {
        System.out.println(">>>>>> "+inst.toString());
    }

    public void visitGoto(SSAGotoInstruction instruction) {
        System.out.println("> Visiting Goto Instruction");
    }

    public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
        System.out.println("> Visiting ArrayLoad Instruction");
    }

    public void visitArrayStore(SSAArrayStoreInstruction instruction) {
        System.out.println("> Visiting ArrayStore Instruction");
    }

    public void visitBinaryOp(SSABinaryOpInstruction instruction) {
        System.out.println("> Visiting BinaryOp Instruction");
    }

    public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
        System.out.println("> Visiting UnaryOp Instruction");
    }

    public void visitConversion(SSAConversionInstruction instruction) {
        System.out.println("> Visiting Conversion Instruction");
    }

    public void visitComparison(SSAComparisonInstruction instruction) {
        System.out.println("> Visiting Comparison Instruction");
    }

    public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
        System.out.println("> Visiting Conditional Branch Instruction");
    }

    public void visitReturn(SSAReturnInstruction instruction) {
        System.out.println("> Visiting Return Instruction");
    }

    public void visitGet(SSAGetInstruction instruction) {
        System.out.println("> Visiting Get Instruction");
    }

    public void visitPut(SSAPutInstruction instruction) {
        System.out.println("> Visiting Put Instruction");
    }

    public void visitInvoke(SSAInvokeInstruction instruction) {
        System.out.println("> Visiting Invoke Instruction");
    }

    public void visitNew(SSANewInstruction instruction) {
        System.out.println("> Visiting New Instruction");
    }

    public void visitArrayLength(SSAArrayLengthInstruction instruction) {
        System.out.println("> Visiting ArrayLength Instruction");
    }

    public void visitThrow(SSAThrowInstruction instruction) {
        System.out.println("> Visiting Throw Instruction");
    }

    public void visitMonitor(SSAMonitorInstruction instruction) {
        System.out.println("> Visiting Monitor Instruction");
    }

    public void visitCheckCast(SSACheckCastInstruction instruction) {
        System.out.println("> Visiting CheckCast Instruction");
    }

    public void visitInstanceof(SSAInstanceofInstruction instruction) {
        System.out.println("> Visiting Instanceof Instruction");
    }

    public void visitPhi(SSAPhiInstruction instruction) {
        System.out.println("> Visiting Phi Instruction");
    }

    public void visitPi(SSAPiInstruction instruction) {
        System.out.println("> Visiting Pi Instruction");
    }

    public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
        System.out.println("> Visiting GetCaughtException Instruction");
    }

    public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
        System.out.println("> Visiting LoadMetadata Instruction");
    }

}
