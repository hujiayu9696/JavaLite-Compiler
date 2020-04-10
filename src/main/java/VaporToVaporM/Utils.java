package VaporToVaporM;

import cs132.vapor.ast.*;

public class Utils {
    static void checkCondition(boolean c) {
        if (!c)
            throw new RuntimeException("Assert Error");
    }

    public static String getGlobalInfo(VDataSegment[] dataSegments) {
        StringBuffer res = new StringBuffer();
        for (VDataSegment dataSegment : dataSegments) {
            res.append("const ").append(dataSegment.ident).append("\n");
            for (VOperand.Static value : dataSegment.values)
                res.append("\t").append(value.toString()).append("\n");
            res.append("\n");
        }
        return res.toString();
    }

    public static String translateFunctions(VFunction[] fs)
    {
        StringBuffer res = new StringBuffer();
        for (VFunction f : fs)
        {
            VMFunction function = new VMFunction(f);
            res.append(function.getFunInfo());
            V2VMVisitor visitor = new V2VMVisitor(function);
            int labelInd = 0;
            for (int i = 0; i < f.body.length; i++)
            {
                while (labelInd < f.labels.length && f.labels[labelInd].instrIndex <= i)
                {
                    visitor.print(f.labels[labelInd].ident + ":");
                    labelInd ++;
                }
                VInstr instr = f.body[i];
                visitor.t = -1;
                instr.accept(visitor);
            }
            res.append(visitor.code).append("\n");
        }
        return res.toString();
    }
}
