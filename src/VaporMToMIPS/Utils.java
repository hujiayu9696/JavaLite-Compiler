package VaporMToMIPS;

import cs132.vapor.ast.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class Utils {
    static void checkCondition(boolean c) {
        if (!c)
            throw new RuntimeException("Assert Error");
    }

    private static String staticOpString(VOperand.Static op)
    {
        if (op instanceof VLitInt)
            return Integer.toString(((VLitInt) op).value);
        else if (op instanceof VLabelRef)
            return ((VLabelRef) op).ident;
        else checkCondition(false);
        return null;
    }

    public static String getGlobalInfo(VDataSegment[] dataSegments) {
        StringBuffer res = new StringBuffer();
        res.append(".data\n\n");
        for (VDataSegment dataSegment : dataSegments) {
            res.append(dataSegment.ident).append(":\n");
            for (VOperand.Static value : dataSegment.values)
                res.append("\t").append(staticOpString(value)).append("\n");
            res.append("\n");
        }
        res.append(".text\n\n");
        res.append("\tjal minijava\n");
        res.append("\tli $v0 10\n");
        res.append("\tsyscall\n");
        res.append("\n");
        return res.toString();
    }

    public static LinkedHashMap<String, String> stringMap = new LinkedHashMap<>();
    public static String addString(String value)
    {
        String key = String.format("_str%d", stringMap.size());
        stringMap.put(key, value);
        return key;
    }

    public static String translateFunctions(VFunction[] fs)
    {
        StringBuffer res = new StringBuffer();
        for (VFunction f : fs)
        {
            VMFunction function = new VMFunction(f);
            res.append(function.getFunEntry());
            VM2MVisitor visitor = new VM2MVisitor(function);
            int labelInd = 0;
            for (int i = 0; i < f.body.length; i++)
            {
                while (labelInd < f.labels.length && f.labels[labelInd].instrIndex <= i)
                {
                    visitor.code.append(f.labels[labelInd].ident + ":\n");
                    labelInd ++;
                }
                VInstr instr = f.body[i];
                instr.accept(visitor);
            }
            res.append(visitor.code).append("\n");
        }
        return res.toString();
    }

    public static String endOfMIPS()
    {
        StringBuffer res = new StringBuffer();
        res.append(".data\n");
        res.append(".align 0\n");
        res.append("_newline: .asciiz \"\\n\"\n");
        for (Map.Entry e : stringMap.entrySet())
            res.append(String.format("%s: .asciiz %s\n", e.getKey(), e.getValue()));
        return res.toString();
    }
}
