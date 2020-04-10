package JavaToVapor;

import Utils.Pair;

public class ProgramState {
    ProgramState(String currentClass) {
        indent = 1;
        tempVSize = 0;
        currentSymbol = null;
        this.currentClass = currentClass;
    }

    SymbolTable currentSymbol;
    String currentClass;

    int indent;
    int tempVSize;

    String printIndent()
    {
        String res = "";
        for (int i=1;i<=indent;i++)
            res += "\t";
        return res;
    }

    String getATempVariable() {
        return String.format("t.%d", tempVSize++);
    }

    String join(String... elements) {
        String res = "";
        for (String s:elements)
            res += printIndent() + s + "\n";
        return res;
    }

    String checkNull(String pointer)
    {
        String nullLabel = VaporGlobalState.nullx();
        String s1 = String.format("if %s goto :%s", pointer, nullLabel);
        String s2 = "\tError(\"null pointer\")";
        String s3 = nullLabel + ":";
        return join(s1, s2, s3);
    }

    // Returned can be both of left and right value.
    // Returned is an MemRef or id, which is not an "Operand", which should only be used in an assignment, and at most once.
    String fetchID(String id)
    {
        Object idv = currentSymbol.lookUp(id);
        if (idv instanceof String)
            return (String)idv;
        else if (idv instanceof Integer)
        {
            String s = String.format("[this + %d]", (Integer)idv * 4);
            return s;
        }else Utils.wrongWay();
        return null;
    }

    // Here "exp" represents o, op(o1 o2), m, id
    Pair<String, String> acceptExpToTemp(String e)
    {
        String temp = getATempVariable();
        String s = String.format("%s = %s", temp , e);
        return new Pair<>(join(s), temp);
    }

    // Returned is a memory ref.
    Pair<String, String> checkAndReturnArrayIndexLeftValue(String array, String ind)
    {
        String s0 = checkNull(array);
        String boundLabel = VaporGlobalState.boundsx();
        String tempV = getATempVariable();
        String s1 = String.format("%s = [%s]", tempV, array);
        String s2 = String.format("%s = Lt(%s %s)", tempV, ind, tempV);
        String s3 = String.format("if %s goto :%s", tempV, boundLabel);
        String s4 = "\tError(\"array index out of bounds\")";
        String s5 = boundLabel + ":";
        String s6 = String.format("%s = MulS(%s 4)", tempV, ind);
        String s7 = String.format("%s = Add(%s %s)",tempV, tempV, array);
        return new Pair<>(s0 + join(s1, s2, s3, s4, s5, s6, s7), String.format("[%s + 4]", tempV));
    }
}