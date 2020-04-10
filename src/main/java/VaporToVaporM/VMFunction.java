package VaporToVaporM;

import cs132.vapor.ast.*;

import java.util.LinkedHashMap;

public class VMFunction {
    VFunction vFunction;
    int in, out, local;
    LinkedHashMap<String, Integer> inTable = new LinkedHashMap<>();
    LinkedHashMap<String, Integer> localTable = new LinkedHashMap<>();

    VMFunction(VFunction vFunction) {
        this.vFunction = vFunction;
        in = vFunction.params.length;
        for (VVarRef.Local param : vFunction.params)
            inTable.put(param.ident, inTable.size());

        local = vFunction.vars.length - in;
        for (String id : vFunction.vars)
            if (inTable.get(id) == null)
                localTable.put(id, localTable.size());

        Utils.checkCondition(in == inTable.size());
        Utils.checkCondition(local == localTable.size());

        out = 0;
        for (VInstr instr : vFunction.body) {
            if (!(instr instanceof VCall))
                continue;
            out = Math.max(out, ((VCall) instr).args.length);
        }
    }

    String getVarRef(String id) {
        if (inTable.get(id) != null)
            return String.format("in[%d]", inTable.get(id));
        Utils.checkCondition(localTable.get(id) != null);
        return String.format("local[%d]", localTable.get(id));
    }

    String getFunInfo() {
        return String.format("func %s [in %d, out %d, local %d]\n", vFunction.ident, in, out, local);
    }
}
