package VaporMToMIPS;

import cs132.vapor.ast.*;

public class VMFunction {
    VFunction vFunction;
    int in, out, local;
    int stackSize;

    VMFunction(VFunction vFunction) {
        this.vFunction = vFunction;
        in = vFunction.stack.in;
        local = vFunction.stack.local;
        out = vFunction.stack.out;
        stackSize = (out + local + 2) * 4;
    }

    String getMemRefAddress(VMemRef ref)
    {
        if (ref instanceof VMemRef.Global)
        {
            VMemRef.Global t = (VMemRef.Global) ref;
            return String.format("%d(%s)", t.byteOffset, t.base.toString());
        }else if (ref instanceof VMemRef.Stack)
        {
            VMemRef.Stack t = (VMemRef.Stack) ref;
            if (t.region == VMemRef.Stack.Region.In)
                return String.format("%d($fp)", t.index * 4);
            else if (t.region == VMemRef.Stack.Region.Out)
                return String.format("%d($sp)", t.index * 4);
            else
                return String.format("%d($sp)", (t.index + out) * 4);
        }else Utils.checkCondition(false);
        return null;
    }

    String getFunEntry() {
        StringBuffer res = new StringBuffer();
        res.append(vFunction.ident).append(":\n");
        res.append("\tsw $ra -4($sp)\n");
        res.append("\tsw $fp -8($sp)\n");
        res.append("\tmove $fp $sp\n");
        res.append(String.format("\tsubu $sp $sp %d\n", stackSize));
        return res.toString();
    }

    String getFunTail() {
        StringBuffer res = new StringBuffer();
        res.append("\tlw $ra -4($fp)\n");
        res.append("\tlw $fp -8($fp)\n");
        res.append(String.format("\taddu $sp $sp %d\n", stackSize));
        res.append("\tjr $ra\n");
        return res.toString();
    }
}
