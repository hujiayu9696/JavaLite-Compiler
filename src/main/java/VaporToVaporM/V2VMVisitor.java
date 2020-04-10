package VaporToVaporM;

import cs132.vapor.ast.*;

import java.util.ArrayList;

public class V2VMVisitor extends VInstr.Visitor {
    StringBuffer code = new StringBuffer();
    VMFunction function;
    int t = -1;

    void print(String s) {
        code.append("\t").append(s).append("\n");
    }

    public V2VMVisitor(VMFunction function) {
        this.function = function;
    }

    // Transfer Vapor op to Vapor-M op (right value)
    String opTrans(String s) {
        String opRef = function.getVarRef(s);
        String rt = "$t" + (++t);
        print(String.format("%s = %s", rt, opRef));
        return rt;
    }

    ArrayList<String> opTrans(VOperand... ops) {
        ArrayList<String> res = new ArrayList<>();
        for (VOperand op : ops) {
            if (op instanceof VOperand.Static)
                res.add(op.toString());
            else if (op instanceof VVarRef)
                res.add(opTrans(op.toString()));
            else if (op instanceof VLitStr)
                res.add(op.toString());
            else Utils.checkCondition(false);
        }
        return res;
    }

    // Let a register accept the "right" value.
    String acceptRight(String right) {
        String rt = "$t" + (++t);
        print(String.format("%s = %s", rt, right));
        return rt;
    }

    // Let Vapor ID "destID" accept the Vapor-M "op".
    void makeAssignment(VVarRef dest, String op) {
        String destRef = function.getVarRef(dest.toString());
        print(String.format("%s = %s", destRef, op));
    }

    public void visit(VAssign a) {
        makeAssignment(a.dest, opTrans(a.source).get(0));
    }

    public void visit(VCall c) {
        int i = 0;
        for (VOperand vop : c.args)
        {
            t = -1;
            String op = opTrans(vop).get(0);
            print(String.format("out[%d] = %s", i, op));
            i++;
        }
        String addrOp = null;
        if (c.addr instanceof VAddr.Var)
            addrOp = opTrans(c.addr.toString());
        else if (c.addr instanceof VAddr.Label)
            addrOp = c.addr.toString();
        else Utils.checkCondition(false);
        print("call " + addrOp);
        makeAssignment(c.dest, "$v0");
    }

    public void visit(VBuiltIn c) {
        ArrayList<String> ops = opTrans(c.args);
        String right = String.format("%s(%s)", c.op.name, String.join(" ", ops));
        if (c.dest == null)
            print(right);
        else
            makeAssignment(c.dest, acceptRight(right));
    }

    public String transferMemRef(VMemRef.Global memRef) {
        String op = null;
        if (memRef.base instanceof VAddr.Var)
            op = opTrans(memRef.base.toString());
        else if (memRef.base instanceof VAddr.Label)
            op = memRef.base.toString();
        else Utils.checkCondition(false);
        return String.format("[%s + %d]", op, memRef.byteOffset);
    }

    public void visit(VMemWrite w) {
        String memRef = transferMemRef((VMemRef.Global) w.dest);
        String op = opTrans(w.source).get(0);
        print(String.format("%s = %s", memRef, op));
    }

    public void visit(VMemRead r) {
        String rt = acceptRight(transferMemRef((VMemRef.Global) r.source));
        makeAssignment(r.dest, rt);
    }

    public void visit(VBranch b) {
        String value = opTrans(b.value).get(0);
        String target = b.target.toString();
        String sIf = b.positive ? "if" : "if0";
        print(String.format("%s %s goto %s", sIf, value, target));
    }

    public void visit(VGoto g) {
        print("goto " + g.target); // Here, g.target can only be a label (not id)
    }

    public void visit(VReturn r) {
        if (r.value != null) {
            String op = opTrans(r.value).get(0);
            print(String.format("$v0 = %s", op));
        }else
            print("$v0 = 0");
        print("ret");
    }
}
