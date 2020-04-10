package TypeChecker;

import JavaParser.syntaxtree.*;
import Utils.Pair;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    static void printProgInfo() {
        System.out.println("Defined Class:");
        for (Map.Entry<String, Node> classEntry : JavaType.definedClass.entrySet())
        {
            System.out.println(classEntry.getKey());
            HashMap<String, MethodDeclaration> methods = JavaType.classMethodTable.get(classEntry.getKey());
            for (Map.Entry<String, MethodDeclaration> methodEntry : methods.entrySet())
            {
                System.out.print("  " + methodEntry.getKey() + ": ");
                ArrayList methodSig = Utils.getMethodSignature(methodEntry.getValue());
                for (Object o : methodSig)
                    System.out.print(o + " ");
                System.out.println();
            }
        }
    }

    static void addKeyValueToHashMapWithChecking(HashMap H, Object key, Object values)
    {
        if (H.containsKey(key))
            throw new RuntimeException("Identifier " + key + " already exists");
        H.put(key, values);
    }

    static void checkCondition(boolean c) {
        if (!c)
            throw new RuntimeException("Assert Error");
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

    static JavaType nodeTypeToJavaType(Type t) {
        if (t.f0.which == 0)
            return new JavaType(JavaType.typeCategory.ArrayType);
        if (t.f0.which == 1)
            return new JavaType(JavaType.typeCategory.BooleanType);
        if (t.f0.which == 2)
            return new JavaType(JavaType.typeCategory.IntegerType);
        if (t.f0.which == 3)
            return new JavaType(identifierName((Identifier) t.f0.choice));
        throw new RuntimeException("Should not happen");
    }

    static ArrayList getMethodSignature(MethodDeclaration method) {
        ArrayList res = new ArrayList<>();
        res.add(methodReturnType(method));

        NodeOptional arguListOpt = method.f4;
        if (!arguListOpt.present())
            return res;
        FormalParameterList arguList = (FormalParameterList) arguListOpt.node;
        fetchParametersFromArguList(res, arguList);
        return res;
    }

    private static void fetchParametersFromArguList(ArrayList res, FormalParameterList arguList) {
        res.add(fetchParameterFromFormalParameter(arguList.f0));
        NodeListOptional FormalParameterRestList = arguList.f1;
        for (Enumeration<Node> e = FormalParameterRestList.elements(); e.hasMoreElements(); ) {
            FormalParameterRest formalParameterRest = (FormalParameterRest) e.nextElement();
            res.add(fetchParameterFromFormalParameter(formalParameterRest.f1));
        }
    }

    private static Pair<String, JavaType> fetchParameterFromFormalParameter(FormalParameter par) {
        return new Pair<String, JavaType>(identifierName(par.f1), nodeTypeToJavaType(par.f0));
    }

    static SymbolTable methodParametersToSymbolTable(MethodDeclaration method)
    {
        ArrayList<Pair<String, JavaType>> sig = getMethodSignature(method);
        sig.remove(0); // Remove the return type.
        SymbolTable res = new SymbolTable();
        for (Pair<String, JavaType> p : sig)
            res.addSymbol(p.key, p.value);
        return res;
    }

    static Pair<String, JavaType> getVarDeclaration(VarDeclaration var) {
        return new Pair<String, JavaType>(identifierName(var.f1), nodeTypeToJavaType(var.f0));
    }

    static SymbolTable VarDeclarationListToSymbolTable(NodeListOptional vars) {
        SymbolTable res = new SymbolTable();
        for (Enumeration<Node> e = vars.elements(); e.hasMoreElements(); ) {
            Pair<String, JavaType> var = getVarDeclaration((VarDeclaration) e.nextElement());
            res.addSymbol(var.key, var.value);
        }
        return res;
    }

    static JavaType methodReturnType(MethodDeclaration method) {
        return nodeTypeToJavaType(method.f1);
    }

    static MethodDeclaration getMethodFromClassIdAndMethodID(String classId, String methodId) {
        return JavaType.classMethodTable.get(classId).get(methodId);
    }
}
