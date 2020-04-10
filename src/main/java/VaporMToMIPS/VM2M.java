package VaporMToMIPS;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import cs132.util.ProblemException;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.parser.VaporParser;

public class VM2M {
    static Op[] ops = {
            Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
            Op.PrintIntS, Op.HeapAllocZ, Op.Error,
    };
    static boolean allowLocals = false;
    static String[] registers = {
            "v0", "v1",
            "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
            "t8",
    };
    static boolean allowStack = true;

    public static String toMIPS(InputStream in) throws IOException, ProblemException {
        VaporProgram program = VaporParser.run(new InputStreamReader(in), 1, 1,
                Arrays.asList(ops), allowLocals, registers, allowStack);
        String res = Utils.getGlobalInfo(program.dataSegments) +
                Utils.translateFunctions(program.functions) + Utils.endOfMIPS() + "\n";
        return res;
    }
}
