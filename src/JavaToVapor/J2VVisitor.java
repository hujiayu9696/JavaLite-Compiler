package JavaToVapor;

import JavaParser.syntaxtree.*;
import JavaParser.visitor.GJNoArguDepthFirst;
import Utils.Pair;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

public class J2VVisitor extends GJNoArguDepthFirst<String> {
    public StringBuffer vaporCode = new StringBuffer();

    ProgramState programState;
    String currentClass;

    private void printMata(String s) {
        vaporCode.append(s);
    }

    private void print(String s) {
        vaporCode.append(programState.join(s));
    }

    public String visit(NodeList n) {
        return super.visit(n);
    }

    public String visit(NodeListOptional n) {
        return super.visit(n);
    }

    public String visit(NodeOptional n) {
        return super.visit(n);
    }

    public String visit(NodeSequence n) {
        return super.visit(n);
    }

    public String visit(NodeToken n) {
        Utils.wrongWay();
        return null;
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     *
     * @param n
     */
    public String visit(Goal n) {
        Initializer.initializeDefinedClass(n);

        // Fetch classField and methodTable for all the classes.
        for (String c : Initializer.definedClass.keySet()) {
            Initializer.fetchField(c);
            Initializer.fetchClassMethodTable(c);
        }

        for (Map.Entry<String, JavaClass> c : Initializer.definedClass.entrySet()) {
            printMata(String.format("const vmt_%s\n", c.getKey()));
            String[] stringArray = new String[c.getValue().methodTable.size()];
            for (JavaMethod method : c.getValue().methodTable.values())
                stringArray[method.methodIndex] = String.format("\t:%s\n", method.vaporLabel);
            printMata(String.join("", stringArray));
            printMata("\n");
        }
        n.f0.accept(this);
        n.f1.accept(this);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     *
     * @param n
     */
    public String visit(MainClass n) {
        printMata("func minijava()\n");
        programState = new ProgramState(null);
        programState.currentSymbol = Utils.VarDeclarationListToSymbolTable(n.f14, -1).key;
        n.f15.accept(this);
        print("ret");
        programState = null;
        printMata("\n");
        return null;
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     *
     * @param n
     */
    public String visit(TypeDeclaration n) {
        return super.visit(n);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     *
     * @param n
     */
    public String visit(ClassDeclaration n) {
        currentClass = Utils.classname(n);
        n.f4.accept(this);
        currentClass = null;
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     *
     * @param n
     */
    public String visit(ClassExtendsDeclaration n) {
        currentClass = Utils.classname(n);
        n.f6.accept(this);
        currentClass = null;
        return null;
    }

    public String visit(VarDeclaration n) {
        Utils.wrongWay();
        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     *
     * @param n
     */
    public String visit(MethodDeclaration n) {
        String methodName = Utils.methodname(n);
        JavaMethod theMethod = Initializer.definedClass.get(currentClass).methodTable.get(methodName);

        // Print Function Header
        printMata("func " + theMethod.vaporLabel + "(this");
        ArrayList<Pair<String, String>> parameterIDs = theMethod.getMethodParaID();
        for (Pair<String, String> par : parameterIDs)
            printMata(" " + par.key);
        printMata(")\n");

        // Translate the function
        programState = new ProgramState(currentClass);
        programState.currentSymbol = Initializer.fields(programState.currentClass).key; // Field
        SymbolTable methodSymbol = theMethod.parametersToSymbolTable(); // Parameters
        methodSymbol.appendTable(Utils.VarDeclarationListToSymbolTable(n.f7, -1).key); // Locals
        programState.currentSymbol.appendTable(methodSymbol); // currentSymbol = Field * (Parameters * Locals)
        n.f8.accept(this);
        String returnV = n.f10.accept(this);
        print("ret " + returnV);
        programState = null;
        printMata("\n");
        return null;
    }

    public String visit(FormalParameterList n) {
        Utils.wrongWay();
        return null;
    }

    public String visit(FormalParameter n) {
        Utils.wrongWay();
        return null;
    }

    public String visit(FormalParameterRest n) {
        Utils.wrongWay();
        return null;
    }

    public String visit(Type n) {
        Utils.wrongWay();
        return null;
    }

    public String visit(ArrayType n) {
        Utils.wrongWay();
        return null;
    }

    public String visit(BooleanType n) {
        Utils.wrongWay();
        return null;
    }

    public String visit(IntegerType n) {
        Utils.wrongWay();
        return null;
    }

    /**
     * f0 -> Block()
     * | AssignmentStatement()
     * | ArrayAssignmentStatement()
     * | IfStatement()
     * | WhileStatement()
     * | PrintStatement()
     *
     * @param n
     */
    public String visit(Statement n) {
        return super.visit(n);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     *
     * @param n
     */
    public String visit(Block n) {
        n.f1.accept(this);
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     *
     * @param n
     */
    public String visit(AssignmentStatement n) {
        String id = programState.fetchID(Utils.identifierName(n.f0));
        String e = n.f2.accept(this);
        print(String.format("%s = %s", id, e));
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     *
     * @param n
     */
    public String visit(ArrayAssignmentStatement n) {
        String e1 = n.f2.accept(this);
        String e2 = n.f5.accept(this);
        String javaId = programState.fetchID(Utils.identifierName(n.f0));
        Pair<String, String> vid = programState.acceptExpToTemp(javaId);
        printMata(vid.key);
        Pair<String, String> vArrayRef = programState.checkAndReturnArrayIndexLeftValue(vid.value, e1);
        printMata(vArrayRef.key);
        print(String.format("%s = %s", vArrayRef.value, e2));
        return null;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     *
     * @param n
     */
    public String visit(IfStatement n) {
        String e = n.f2.accept(this);
        Pair<String, String> iflabel = VaporGlobalState.ifx();
        String ifx_else = iflabel.key, ifx_end = iflabel.value;
        print(String.format("if0 %s goto :%s", e, ifx_else));
        programState.indent++;
        n.f4.accept(this);
        print(String.format("goto :%s", ifx_end));
        programState.indent--;
        print(ifx_else + ":");
        programState.indent++;
        n.f6.accept(this);
        programState.indent--;
        print(ifx_end + ":");
        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     *
     * @param n
     */
    public String visit(WhileStatement n) {
        Pair<String, String> whilelabel = VaporGlobalState.whilex();
        String whilex_top = whilelabel.key, whilex_end = whilelabel.value;
        print(whilex_top + ":");
        String e = n.f2.accept(this);
        print(String.format("if0 %s goto :%s", e, whilex_end));
        programState.indent++;
        n.f4.accept(this);
        print(String.format("goto :%s", whilex_top));
        programState.indent--;
        print(whilex_end + ":");
        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     *
     * @param n
     */
    public String visit(PrintStatement n) {
        String e = n.f2.accept(this);
        print(String.format("PrintIntS(%s)", e));
        return null;
    }

    /**
     * f0 -> AndExpression()
     * | CompareExpression()
     * | PlusExpression()
     * | MinusExpression()
     * | TimesExpression()
     * | ArrayLookup()
     * | ArrayLength()
     * | MessageSend()
     * | PrimaryExpression()
     *
     * @param n
     */
    public String visit(Expression n) {
        return n.f0.choice.accept(this);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     *
     * @param n
     */
    public String visit(AndExpression n) {
        String res = programState.getATempVariable();
        Pair<String, String> ssLabel = VaporGlobalState.ssx();
        String ssx_else = ssLabel.key, ssx_end = ssLabel.value;

        String e1 = n.f0.accept(this);
        print(String.format("if0 %s goto :%s", e1, ssx_else));
        programState.indent++;
        String e2 = n.f2.accept(this);
        print(String.format("%s = %s", res, e2));
        print(String.format("goto :%s", ssx_end));
        programState.indent--;
        print(ssx_else + ":");
        print(String.format("\t%s = 0", res));
        print(ssx_end + ":");
        return res;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     *
     * @param n
     */
    public String visit(CompareExpression n) {
        String res = programState.getATempVariable();
        String e1 = n.f0.accept(this);
        String e2 = n.f2.accept(this);
        print(String.format("%s = LtS(%s %s)", res, e1, e2));
        return res;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     *
     * @param n
     */
    public String visit(PlusExpression n) {
        String res = programState.getATempVariable();
        String e1 = n.f0.accept(this);
        String e2 = n.f2.accept(this);
        print(String.format("%s = Add(%s %s)", res, e1, e2));
        return res;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     *
     * @param n
     */
    public String visit(MinusExpression n) {
        String res = programState.getATempVariable();
        String e1 = n.f0.accept(this);
        String e2 = n.f2.accept(this);
        print(String.format("%s = Sub(%s %s)", res, e1, e2));
        return res;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     *
     * @param n
     */
    public String visit(TimesExpression n) {
        String res = programState.getATempVariable();
        String e1 = n.f0.accept(this);
        String e2 = n.f2.accept(this);
        print(String.format("%s = MulS(%s %s)", res, e1, e2));
        return res;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     *
     * @param n
     */
    public String visit(ArrayLookup n) {
        String e1 = n.f0.accept(this);
        String e2 = n.f2.accept(this);
        Pair<String, String> vArrayRef = programState.checkAndReturnArrayIndexLeftValue(e1, e2);
        printMata(vArrayRef.key);
        Pair<String, String> vid = programState.acceptExpToTemp(vArrayRef.value);
        printMata(vid.key);
        return vid.value;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     *
     * @param n
     */
    public String visit(ArrayLength n) {
        String e = n.f0.accept(this);
        String res = programState.getATempVariable();
        print(String.format("%s = [%s]", res, e));
        return res;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     *
     * @param n
     */
    public String visit(MessageSend n) {
        String e = n.f0.accept(this);
        printMata(programState.checkNull(e));
        String eType = programState.currentSymbol.getTempExpType(e);
        String methodId = Utils.identifierName(n.f2);
        JavaMethod theMethod = Initializer.definedClass.get(eType).methodTable.get(methodId);
        Utils.checkCondition(theMethod != null);

        ArrayList<String> parameters = getExpressionListTemps(n.f4);
        String vMethodPointer = programState.getATempVariable();
        String s1 = String.format("%s = [%s]", vMethodPointer, e);
        String s2 = String.format("%s = [%s + %d]", vMethodPointer, vMethodPointer, theMethod.methodIndex * 4);

        String res = programState.getATempVariable();
        programState.currentSymbol.typeTable.put(res, theMethod.returnType());
        String s3 = String.format("%s = call %s(%s", res, vMethodPointer, e);
        for (String p : parameters)
            s3 += " " + p;
        s3 += ")";

        printMata(programState.join(s1, s2, s3));
        return res;
    }

    private ArrayList<String> getExpressionListTemps(NodeOptional exps)
    {
        ArrayList<String> res = new ArrayList();
        if (!exps.present())
            return res;
        ExpressionList expList = (ExpressionList)exps.node;
        Expression firstExp = expList.f0;
        res.add(firstExp.accept(this));
        for (Enumeration<Node> e = expList.f1.elements(); e.hasMoreElements(); )
        {
            Expression exp = ((ExpressionRest)e.nextElement()).f1;
            res.add(exp.accept(this));
        }
        return res;
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     *
     * @param n
     */
    public String visit(ExpressionList n) {
        Utils.wrongWay();
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     *
     * @param n
     */
    public String visit(ExpressionRest n) {
        Utils.wrongWay();
        return null;
    }

    /**
     * f0 -> IntegerLiteral()
     * | TrueLiteral()
     * | FalseLiteral()
     * | Identifier()
     * | ThisExpression()
     * | ArrayAllocationExpression()
     * | AllocationExpression()
     * | NotExpression()
     * | BracketExpression()
     *
     * @param n
     */
    public String visit(PrimaryExpression n) {
        return n.f0.choice.accept(this);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     *
     * @param n
     */
    public String visit(IntegerLiteral n) {
        return n.f0.tokenImage;
    }

    /**
     * f0 -> "true"
     *
     * @param n
     */
    public String visit(TrueLiteral n) {
        return "1";
    }

    /**
     * f0 -> "false"
     *
     * @param n
     */
    public String visit(FalseLiteral n) {
        return "0";
    }

    /**
     * f0 -> <IDENTIFIER>
     *
     * @param n
     */
    public String visit(Identifier n) {
        String javaId = Utils.identifierName(n);
        Object idv = programState.currentSymbol.lookUp(javaId);
        if (idv instanceof String)
            return (String)idv;
        else if (idv instanceof Integer)
        {
            String vidRef = String.format("[this + %d]", (Integer)idv * 4);
            String res = programState.getATempVariable();
            print(String.format("%s = %s", res, vidRef));
            programState.currentSymbol.typeTable.put(res, programState.currentSymbol.getTempExpType(javaId));
            return res;
        }else Utils.wrongWay();
        return null;
    }

    /**
     * f0 -> "this"
     *
     * @param n
     */
    public String visit(ThisExpression n) {
        programState.currentSymbol.typeTable.put("this", currentClass);
        return "this";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     *
     * @param n
     */
    public String visit(ArrayAllocationExpression n) {
        String size = n.f3.accept(this);
        String bytes = programState.getATempVariable();
        String v = programState.getATempVariable();
        String s1 = String.format("%s = MulS(%s 4)", bytes, size);
        String s2 = String.format("%s = Add(%s 4)", bytes, bytes);
        String s3 = String.format("%s = HeapAllocZ(%s)", v, bytes);
        String s4 = String.format("[%s] = %s", v, size);
        printMata(programState.join(s1, s2, s3, s4));
        return v;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     *
     * @param n
     */
    public String visit(AllocationExpression n)
    {
        String classID = Utils.identifierName(n.f1);
        JavaClass theClass = Initializer.definedClass.get(classID);
        String res = programState.getATempVariable();
        print(String.format("%s = HeapAllocZ(%d)", res, theClass.fieldLength * 4 + 4));
        print(String.format("[%s] = :%s", res, "vmt_" + classID));
        programState.currentSymbol.typeTable.put(res, classID);
        return res;
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     *
     * @param n
     */
    public String visit(NotExpression n) {
        String e = n.f1.accept(this);
        String res = programState.getATempVariable();
        print(String.format("%s = Sub(1 %s)", res, e));
        return res;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     *
     * @param n
     */
    public String visit(BracketExpression n) {
        return n.f1.accept(this);
    }
}
