package VaporMToMIPS;
import cs132.vapor.ast.*;

public class VM2MVisitor extends VInstr.Visitor {
    StringBuffer code = new StringBuffer();
    VMFunction function;

    void print(String s) {
        code.append("\t").append(s).append("\n");
    }

    public VM2MVisitor(VMFunction function) {
        this.function = function;
    }

    // Transfer Vapor-M op to MIPS register
    String opTrans(VOperand op, String tempReg) {
        if (op instanceof VVarRef.Register)
            return op.toString();
        else if (op instanceof VLitInt)
        {
            print(String.format("li %s %s", tempReg, op.toString()));
            return tempReg;
        }else if (op instanceof VLabelRef)
        {
            print(String.format("la %s %s", tempReg, ((VLabelRef) op).ident));
            return tempReg;
        }else Utils.checkCondition(false);
        return null;
    }

    public void visit(VAssign a) {
        String source = opTrans(a.source, "$t9");
        print(String.format("move %s %s", a.dest.toString(), source));
    }

    public void visit(VCall c) {
        if (c.addr instanceof VAddr.Var)
            print("jalr " + c.addr.toString());
        else if (c.addr instanceof VAddr.Label)
            print("jal " + ((VAddr.Label<VFunction>) c.addr).label.ident);
        else Utils.checkCondition(false);
    }

    void toA0(String reg)
    {
        Utils.checkCondition(reg != null);
        if (reg.equals("$a0"))
            return;
        print(String.format("move $a0 %s", reg));
    }

    public void visit(VBuiltIn c) {
        String op1 = null, op2 = null;
        if (c.op != VBuiltIn.Op.Error)
        {
            op1 = opTrans(c.args[0], "$a0");
            if (c.args.length > 1)
                op2 = opTrans(c.args[1], "$a1");
        }
        if (c.op == VBuiltIn.Op.PrintIntS)
        {
            toA0(op1);
            print("li $v0 1");
            print("syscall");
            print("la $a0 _newline");
            print("li $v0 4");
            print("syscall");
        }else if (c.op == VBuiltIn.Op.HeapAllocZ)
        {
            toA0(op1);
            print("li $v0 9");
            print("syscall");
            print(String.format("move %s $v0", c.dest.toString()));
        }else if (c.op == VBuiltIn.Op.Error)
        {
            Utils.checkCondition(c.args[0] instanceof VLitStr);
            String key = Utils.addString(c.args[0].toString());
            print(String.format("la $a0 %s", key));
            print("li $v0 4");
            print("syscall");
            print("li $v0 10");
            print("syscall");
        }else
        {
            String mipsOpName = null;
            if (c.op == VBuiltIn.Op.Add)
                mipsOpName = "addu";
            else if (c.op == VBuiltIn.Op.Sub)
                mipsOpName = "subu";
            else if (c.op == VBuiltIn.Op.MulS)
                mipsOpName = "mul";
            else if (c.op == VBuiltIn.Op.Eq)
                mipsOpName = "seq";
            else if (c.op == VBuiltIn.Op.Lt)
                mipsOpName = "sltu";
            else if (c.op == VBuiltIn.Op.LtS)
                mipsOpName = "slt";
            Utils.checkCondition(mipsOpName != null);
            Utils.checkCondition((op1 != null) && (op2 != null));
            print(String.format("%s %s %s %s", mipsOpName, c.dest.toString(), op1, op2));
        }
    }

    public void visit(VMemWrite w) {
        String op = opTrans(w.source, "$t9");
        print(String.format("sw %s %s", op, function.getMemRefAddress(w.dest)));
    }

    public void visit(VMemRead r) {
        Utils.checkCondition(r.dest instanceof VVarRef.Register);
        print(String.format("lw %s %s", r.dest.toString(), function.getMemRefAddress(r.source)));
    }

    public void visit(VBranch b) {
        String value = opTrans(b.value, "$t9");
        String target = b.target.ident;
        String sIf = b.positive ? "bnez" : "beqz";
        print(String.format("%s %s %s", sIf, value, target));
    }

    public void visit(VGoto g) {
        Utils.checkCondition(g.target instanceof VAddr.Label);
        // Here, g.target can only be a label (not id)
        print("j " + ((VAddr.Label)g.target).label.ident);
    }

    public void visit(VReturn r) {
        code.append(function.getFunTail());
    }
}
