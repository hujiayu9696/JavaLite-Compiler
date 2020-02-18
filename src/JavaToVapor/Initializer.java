package JavaToVapor;

import JavaParser.syntaxtree.*;
import Utils.Pair;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;

class JavaClass
{
    public JavaClass(Node classNode) {
        this.classNode = classNode;
    }

    public Node classNode;
    public LinkedHashMap<String, JavaMethod> methodTable;
    public SymbolTable classField; // Field index staring from 1
    public int fieldLength;
}

class JavaMethod
{
    public JavaMethod(MethodDeclaration methodNode, String vaporLabel) {
        this.methodNode = methodNode;
        this.vaporLabel = vaporLabel;
    }

    public String returnType()
    {
        return Utils.getTypeFromNode(methodNode.f1);
    }

    public MethodDeclaration methodNode;
    public String vaporLabel;
    public int methodIndex; // Starting from 0.

    // Get method parameters' ids.
    ArrayList<Pair<String, String>> getMethodParaID() {
        ArrayList res = new ArrayList<>();

        NodeOptional arguListOpt = methodNode.f4;
        if (!arguListOpt.present())
            return res;
        FormalParameterList arguList = (FormalParameterList) arguListOpt.node;
        fetchParametersFromArguList(res, arguList);
        return res;
    }


    private void fetchParametersFromArguList(ArrayList<Pair<String, String>> res, FormalParameterList arguList) {
        res.add(fetchParameterFromFormalParameter(arguList.f0));
        NodeListOptional FormalParameterRestList = arguList.f1;
        for (Enumeration<Node> e = FormalParameterRestList.elements(); e.hasMoreElements(); ) {
            FormalParameterRest formalParameterRest = (FormalParameterRest) e.nextElement();
            res.add(fetchParameterFromFormalParameter(formalParameterRest.f1));
        }
    }

    private Pair<String, String> fetchParameterFromFormalParameter(FormalParameter par) {
        return new Pair<>(Utils.identifierName(par.f1), Utils.getTypeFromNode(par.f0));
    }

    SymbolTable parametersToSymbolTable()
    {
        ArrayList<Pair<String, String>> ids = getMethodParaID();
        SymbolTable res = new SymbolTable();
        for (Pair<String, String> id : ids)
            res.addSymbol(id.key, id.key, id.value);
        return res;
    }
}

public class Initializer {
    /**
     * Grammar production:
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public static String mainClassId;
    public static LinkedHashMap<String, JavaClass> definedClass = new LinkedHashMap<>();

    static void initializeDefinedClass(Goal n) {
        // Add main class
        mainClassId = Utils.classname(n.f0);
        // definedClass.put(mainClassId, new JavaClass(n.f0));

        for (Enumeration<Node> e = n.f1.elements(); e.hasMoreElements(); ) {
            TypeDeclaration t = (TypeDeclaration) e.nextElement();
            JavaClass theClass = new JavaClass(t.f0.choice);
            definedClass.put(Utils.classname(theClass.classNode), theClass);
        }
    }

    static Pair<SymbolTable, Integer> fields(String id) {
        int start = 1;
        Node classNode = Initializer.definedClass.get(id).classNode;
        if (classNode instanceof ClassDeclaration) {
            ClassDeclaration c = (ClassDeclaration) classNode;
            NodeListOptional vars = c.f3;
            return Utils.VarDeclarationListToSymbolTable(vars, start);
        } else if (classNode instanceof ClassExtendsDeclaration) {
            ClassExtendsDeclaration c = (ClassExtendsDeclaration) classNode;
            Pair<SymbolTable, Integer> last = fields(Utils.identifierName(c.f3));
            start = last.value;

            NodeListOptional vars = c.f5;
            Pair<SymbolTable, Integer> current = Utils.VarDeclarationListToSymbolTable(vars, start);
            start = current.value;

            SymbolTable resST = last.key;
            resST.appendTable(current.key);
            return new Pair<>(resST, start);
        } else Utils.wrongWay();
        return null;
    }

    public static void fetchField(String className)
    {
        Pair<SymbolTable, Integer> theField = fields(className);
        definedClass.get(className).classField = theField.key;
        definedClass.get(className).fieldLength = theField.value - 1;
    }

    public static void fetchClassMethodTable(String className) {
        if (definedClass.get(className).methodTable != null)
            return;

        Node C = definedClass.get(className).classNode;

        LinkedHashMap<String, JavaMethod> table = null;

        if (C instanceof MainClass)
            table = new LinkedHashMap<>();
        if (C instanceof ClassDeclaration) {
            table = new LinkedHashMap<>();
            NodeListOptional methods = ((ClassDeclaration) C).f4;
            MethodDeclarationListToTable(className, table, methods);
        }
        if (C instanceof ClassExtendsDeclaration) {
            String parent = Utils.identifierName(((ClassExtendsDeclaration) C).f3);
            fetchClassMethodTable(parent);
            table = (LinkedHashMap<String, JavaMethod>) definedClass.get(parent).methodTable.clone();
            NodeListOptional methods = ((ClassExtendsDeclaration) C).f6;
            MethodDeclarationListToTable(className, table, methods);
        }
        definedClass.get(className).methodTable = table;
    }

    private static void MethodDeclarationListToTable(String className, LinkedHashMap<String, JavaMethod> table,
                                                     NodeListOptional methods) {
        for (Enumeration<Node> e = methods.elements(); e.hasMoreElements(); ) {
            MethodDeclaration methodNode = (MethodDeclaration) e.nextElement();
            String methodName = Utils.methodname(methodNode);
            JavaMethod theMethod = new JavaMethod(methodNode, className + "." + methodName);

            // Check overriding
            JavaMethod parrentMethod = table.get(methodName);
            if (parrentMethod != null)
                theMethod.methodIndex = parrentMethod.methodIndex;
            else
                theMethod.methodIndex = table.size();
            table.put(methodName, theMethod);
        }
    }
}
