package TypeChecker;

import JavaParser.syntaxtree.*;
import JavaParser.visitor.GJNoArguDepthFirst;
import Utils.Pair;

import java.util.ArrayList;
import java.util.Enumeration;

public class TypeCheckVisitor extends GJNoArguDepthFirst<JavaType> {
    SymbolTable currentSymbol;
    String currentClass;

    public JavaType visit(NodeList n) {
        return super.visit(n);
    }

    public JavaType visit(NodeListOptional n) {
        return super.visit(n);
    }

    public JavaType visit(NodeOptional n) {
        return super.visit(n);
    }

    public JavaType visit(NodeSequence n) {
        return super.visit(n);
    }

    public JavaType visit(NodeToken n) {
        throw new RuntimeException("Should not enter here");
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     *
     * @param n
     */
    // Rule 21
    public JavaType visit(Goal n) {
        // Initialize JavaType.definedClass, which will check that the Class name is distinct.
        InitializerFunctions.initializeDefinedClass(n);
        // No cycle in classes.
        ArrayList<Pair> linksets = new ArrayList<>();
        for (Node c : JavaType.definedClass.values())
            linksets.add(HelperFunctions.linkset(c));
        if (!HelperFunctions.acyclic(linksets))
            throw new RuntimeException("Cycle in classes");
        // Fetch classMethodTable, which will also check that method name is distinct.
        for (String c : JavaType.definedClass.keySet())
            InitializerFunctions.fetchClassMethodTable(c);
        // Type check begins here.
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
    // Rule 22
    public JavaType visit(MainClass n) {
        SymbolTable theField = Utils.VarDeclarationListToSymbolTable(n.f14);
        // Treat the type of the parameter of minijava as null.
        // Try to not add this one. // theField.addSymbol(Utils.identifierName(n.f11), null);
        // Do type Checking.
        currentSymbol = theField;
        n.f15.accept(this);
        currentSymbol = null;
        return null;
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     *
     * @param n
     */
    public JavaType visit(TypeDeclaration n) {
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
    // Rule 23
    public JavaType visit(ClassDeclaration n) {
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
    // Rule 24
    public JavaType visit(ClassExtendsDeclaration n) {
        currentClass = Utils.classname(n);
        n.f6.accept(this);
        currentClass = null;
        return null;
    }

    public JavaType visit(VarDeclaration n) {
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
    // Rule 25
    public JavaType visit(MethodDeclaration n) {
        currentSymbol = HelperFunctions.fields(currentClass);
        SymbolTable methodSymbol = Utils.methodParametersToSymbolTable(n);
        // We need to check that method parameters and local variables cannot have the same id.
        methodSymbol.appendTable(Utils.VarDeclarationListToSymbolTable(n.f7), true);
        currentSymbol.appendTable(methodSymbol, false);
        n.f8.accept(this);
        JavaType returnType = n.f10.accept(this);
        currentSymbol = null;
        if (!returnType.equals(Utils.methodReturnType(n)))
            throw new RuntimeException("Return type error");
        return null;
    }

    public JavaType visit(FormalParameterList n) {
        return null;
    }

    public JavaType visit(FormalParameter n) {
        return null;
    }

    public JavaType visit(FormalParameterRest n) {
        return null;
    }

    public JavaType visit(Type n) {
        return null;
    }

    public JavaType visit(ArrayType n) {
        return null;
    }

    public JavaType visit(BooleanType n) {
        return null;
    }

    public JavaType visit(IntegerType n) {
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
    public JavaType visit(Statement n) {
        return super.visit(n);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     *
     * @param n
     */
    // Rule 26
    public JavaType visit(Block n) {
        return n.f1.accept(this);
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     *
     * @param n
     */
    // Rule 27
    public JavaType visit(AssignmentStatement n) {
        JavaType idType = currentSymbol.lookUp(Utils.identifierName(n.f0));
        JavaType eType = n.f2.accept(this);
        Utils.checkCondition(eType != null);
        if (!eType.subtype(idType))
            throw new RuntimeException("Cannot assign type " + idType + " with " + eType);
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
    // Rule 28
    public JavaType visit(ArrayAssignmentStatement n) {
        JavaType idType = currentSymbol.lookUp(Utils.identifierName(n.f0));
        if (!idType.equals(JavaType.ArrayType))
            throw new RuntimeException("Identifier " + Utils.identifierName(n.f0) + " is not an array.");
        JavaType e1Type = n.f2.accept(this);
        JavaType e2Type = n.f5.accept(this);
        Utils.checkCondition(e1Type != null && e2Type != null);
        if (e1Type.equals(JavaType.IntegerType) && e2Type.equals(JavaType.IntegerType))
            return null;
        throw new RuntimeException("Invalid Array Assignment");
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
    // Rule 29
    public JavaType visit(IfStatement n) {
        if (!n.f2.accept(this).equals(JavaType.BooleanType))
            throw new RuntimeException("Condition is not of boolean type");
        n.f4.accept(this);
        n.f6.accept(this);
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
    // Rule 30
    public JavaType visit(WhileStatement n) {
        if (!n.f2.accept(this).equals(JavaType.BooleanType))
            throw new RuntimeException("Condition is not of boolean type");
        n.f4.accept(this);
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
    // Rule 31
    public JavaType visit(PrintStatement n) {
        JavaType eType = n.f2.accept(this);
        if (!eType.equals(JavaType.IntegerType))
            throw new RuntimeException("Cannot print type " + eType);
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
    public JavaType visit(Expression n) {
        return n.f0.choice.accept(this);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     *
     * @param n
     */
    // Rule 32
    public JavaType visit(AndExpression n) {
        JavaType p1Type = n.f0.accept(this);
        JavaType p2Type = n.f2.accept(this);
        if (p1Type.equals(JavaType.BooleanType) && p2Type.equals(JavaType.BooleanType))
            return JavaType.BooleanType;
        throw new RuntimeException("And Expression Should Contain Boolean Type");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     *
     * @param n
     */
    // Rule 33
    public JavaType visit(CompareExpression n) {
        JavaType p1Type = n.f0.accept(this);
        JavaType p2Type = n.f2.accept(this);
        if (p1Type.equals(JavaType.IntegerType) && p2Type.equals(JavaType.IntegerType))
            return JavaType.BooleanType;
        throw new RuntimeException("Compare Expression Should Contain int Type");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     *
     * @param n
     */
    // Rule 34
    public JavaType visit(PlusExpression n) {
        JavaType p1Type = n.f0.accept(this);
        JavaType p2Type = n.f2.accept(this);
        if (p1Type.equals(JavaType.IntegerType) && p2Type.equals(JavaType.IntegerType))
            return JavaType.IntegerType;
        throw new RuntimeException("Plus Expression Should Contain int Type");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     *
     * @param n
     */
    // Rule 35
    public JavaType visit(MinusExpression n) {
        JavaType p1Type = n.f0.accept(this);
        JavaType p2Type = n.f2.accept(this);
        if (p1Type.equals(JavaType.IntegerType) && p2Type.equals(JavaType.IntegerType))
            return JavaType.IntegerType;
        throw new RuntimeException("Minus Expression Should Contain int Type");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     *
     * @param n
     */
    // Rule 36
    public JavaType visit(TimesExpression n) {
        JavaType p1Type = n.f0.accept(this);
        JavaType p2Type = n.f2.accept(this);
        if (p1Type.equals(JavaType.IntegerType) && p2Type.equals(JavaType.IntegerType))
            return JavaType.IntegerType;
        throw new RuntimeException("Times Expression Should Contain int Type");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     *
     * @param n
     */
    // Rule 37
    public JavaType visit(ArrayLookup n) {
        JavaType p1Type = n.f0.accept(this);
        JavaType p2Type = n.f2.accept(this);
        if (p1Type.equals(JavaType.ArrayType) && p2Type.equals(JavaType.IntegerType))
            return JavaType.IntegerType;
        throw new RuntimeException("Invalid Array lookup");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     *
     * @param n
     */
    // Rule 38
    public JavaType visit(ArrayLength n) {
        JavaType pType = n.f0.accept(this);
        if (pType.equals(JavaType.ArrayType))
            return JavaType.IntegerType;
        throw new RuntimeException("Not a Array");
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
    // Rule 39
    public JavaType visit(MessageSend n) {
        JavaType pType = n.f0.accept(this);
        if (pType.classId == null)
            throw new RuntimeException("Need to be an object");
        String classId = pType.classId;
        String methodId = Utils.identifierName(n.f2);
        MethodDeclaration theMethod = Utils.getMethodFromClassIdAndMethodID(classId, methodId);
        if (theMethod == null)
            throw new RuntimeException("Method " + methodId + " is not defined");
        ArrayList methodSig = Utils.getMethodSignature(theMethod);
        JavaType returnType = (JavaType) methodSig.get(0);
        methodSig.remove(0);
        ArrayList<JavaType> expTypes = getExpressionListTypes(n.f4);

        if (methodSig.size() != expTypes.size())
            throw new RuntimeException("Wrong Call");

        for (int i = 0; i < methodSig.size(); i++)
            if (!expTypes.get(i).subtype(
                    ((Pair<String, JavaType>) methodSig.get(i)).value
            ))
                throw new RuntimeException("Type is not consistent in method call");
        return returnType;
    }
    private ArrayList<JavaType> getExpressionListTypes(NodeOptional exps)
    {
        ArrayList<JavaType> res = new ArrayList();
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
    public JavaType visit(ExpressionList n) {
        Utils.checkCondition(false);
        return null;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     *
     * @param n
     */
    public JavaType visit(ExpressionRest n) {
        Utils.checkCondition(false);
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
    public JavaType visit(PrimaryExpression n) {
        return n.f0.choice.accept(this);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     *
     * @param n
     */
    // Rule 40
    public JavaType visit(IntegerLiteral n) {
        return JavaType.IntegerType;
    }

    /**
     * f0 -> "true"
     *
     * @param n
     */
    // Rule 41
    public JavaType visit(TrueLiteral n) {
        return JavaType.BooleanType;
    }

    /**
     * f0 -> "false"
     *
     * @param n
     */
    // Rule 42
    public JavaType visit(FalseLiteral n) {
        return JavaType.BooleanType;
    }

    /**
     * f0 -> <IDENTIFIER>
     *
     * @param n
     */
    // Rule 43
    public JavaType visit(Identifier n) {
        return currentSymbol.lookUp(Utils.identifierName(n));
    }

    /**
     * f0 -> "this"
     *
     * @param n
     */
    // Rule 44
    public JavaType visit(ThisExpression n) {
        if (currentClass == null)
            throw new RuntimeException("\"this\" should not be used in minijava Class");
        return new JavaType(currentClass);
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
    // Rule 45
    public JavaType visit(ArrayAllocationExpression n) {
        if (n.f3.accept(this).equals(JavaType.IntegerType))
            return JavaType.ArrayType;
        throw new RuntimeException("Require an int");
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     *
     * @param n
     */
    // Rule 46
    public JavaType visit(AllocationExpression n) {
        return new JavaType(Utils.identifierName(n.f1));
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     *
     * @param n
     */
    // Rule 47
    public JavaType visit(NotExpression n) {
        if (n.f1.accept(this).equals(JavaType.BooleanType))
            return JavaType.BooleanType;
        throw new RuntimeException("Require a boolean");
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     *
     * @param n
     */
    // Rule 48
    public JavaType visit(BracketExpression n) {
        return n.f1.accept(this);
    }
}
