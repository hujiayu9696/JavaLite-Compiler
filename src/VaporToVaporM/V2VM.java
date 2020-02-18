package VaporToVaporM;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import cs132.util.ProblemException;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.parser.VaporParser;

public class V2VM {
    static Op[] ops = {
            Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
            Op.PrintIntS, Op.HeapAllocZ, Op.Error,
    };
    static boolean allowLocals = true;
    static String[] registers = null;
    static boolean allowStack = false;

    public static String toVaporM(InputStream in) throws IOException, ProblemException {
        VaporProgram program = VaporParser.run(new InputStreamReader(in), 1, 1,
                Arrays.asList(ops), allowLocals, registers, allowStack);

        String res = Utils.getGlobalInfo(program.dataSegments) + Utils.translateFunctions(program.functions) + "\n";
        return res;
    }
}
