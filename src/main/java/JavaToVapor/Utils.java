package JavaToVapor;

import JavaParser.syntaxtree.*;
import Utils.Pair;

import java.util.Enumeration;

public class Utils {

    static void printAllFieldTable()
    {
        for (JavaClass c : Initializer.definedClass.values())
        {
            System.out.println(classname(c.classNode) + ":");
            System.out.println(c.classField.data + " len: " + c.fieldLength);
            System.out.println();
        }
    }

    static void checkCondition(boolean c) {
        if (!c)
            throw new RuntimeException("Assert Error");
    }

    static void wrongWay() {
        throw new RuntimeException("Should not happen!!");
    }

    static String identifierName(Identifier i) {
        return i.f0.tokenImage;
    }

    static String classname(Node C) {
        if (C instanceof MainClass)
            return identifierName(((MainClass) C).f1);
        if (C instanceof ClassDeclaration)
            return identifierName(((ClassDeclaration) C).f1);
        if (C instanceof ClassExtendsDeclaration)
            return identifierName(((ClassExtendsDeclaration) C).f1);
        throw new RuntimeException("Not a Class");
    }

    static String methodname(MethodDeclaration m) {
        return identifierName(m.f2);
    }

    public static String getTypeFromNode(Type type)
    {
        Node id = type.f0.choice;
        if (id instanceof Identifier)
            return Utils.identifierName((Identifier) id);
        return "Non-Object-Type";
    }

    // Can be field or local!!
    // When dealing with field, fieldStart != -1 !!!
    // fieldStart represents the current index that will be used, which should start at 1.
    static Pair<SymbolTable, Integer> VarDeclarationListToSymbolTable(NodeListOptional vars, int fieldStart) {
        SymbolTable res = new SymbolTable();
        for (Enumeration<Node> e = vars.elements(); e.hasMoreElements(); ) {
            VarDeclaration var = (VarDeclaration) e.nextElement();
            String varId = identifierName(var.f1);
            String varType = getTypeFromNode(var.f0);
            if (fieldStart != -1)
                res.addSymbol(varId, fieldStart++, varType);
            else res.addSymbol(varId, varId, varType);
        }
        return new Pair<>(res, fieldStart);
    }
}
