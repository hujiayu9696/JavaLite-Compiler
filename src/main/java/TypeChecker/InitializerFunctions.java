package TypeChecker;

import JavaParser.syntaxtree.*;

import java.util.Enumeration;
import java.util.HashMap;

public class InitializerFunctions {
    /**
     * Grammar production:
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    static void initializeDefinedClass(Goal n) {
        // Add main class
        JavaType.mainClassId = Utils.classname(n.f0);
        JavaType.addClassDef(JavaType.mainClassId, n.f0);
        for (Enumeration<Node> e = n.f1.elements(); e.hasMoreElements(); ) {
            TypeDeclaration t = (TypeDeclaration) e.nextElement();
            Node c = t.f0.choice;
            JavaType.addClassDef(Utils.classname(c), c);
        }
    }

    static void fetchClassMethodTable(String s) {
        if (JavaType.classMethodTable.get(s) != null)
            return;
        Node C = JavaType.definedClass.get(s);
        if (C == null)
            throw new RuntimeException("Parent class is not defined");

        HashMap<String, MethodDeclaration> table = null;
        if (C instanceof MainClass)
            table = new HashMap<>();
        if (C instanceof ClassDeclaration) {
            table = new HashMap<>();
            NodeListOptional methods = ((ClassDeclaration) C).f4;
            MethodDeclarationListToTable(table, methods);
        }
        if (C instanceof ClassExtendsDeclaration) {
            String parent = HelperFunctions.linkset(C).value;
            fetchClassMethodTable(parent);
            table = (HashMap) JavaType.classMethodTable.get(parent).clone();
            // Check Overloading
            NodeListOptional methods = ((ClassExtendsDeclaration) C).f6;
            for (Enumeration<Node> e = methods.elements(); e.hasMoreElements(); )
            {
                MethodDeclaration method = (MethodDeclaration) e.nextElement();
                MethodDeclaration parentMethod = table.get(Utils.methodname(method));
                if (parentMethod != null &&
                        !Utils.getMethodSignature(method).equals(Utils.getMethodSignature(parentMethod)))
                    throw new RuntimeException("Overloading not allowed");
                // If this is overriding instead of overloading, then delete the parentMethod.
                if (parentMethod != null)
                    table.remove(Utils.methodname(parentMethod));
            }
            // Update the table.
            MethodDeclarationListToTable(table, methods);
        }
        Utils.checkCondition(table != null);
        JavaType.classMethodTable.put(s, table);
    }

    private static void MethodDeclarationListToTable(HashMap<String, MethodDeclaration> table,
                                                     NodeListOptional methods) {
        for (Enumeration<Node> e = methods.elements(); e.hasMoreElements(); ) {
            MethodDeclaration method = (MethodDeclaration) e.nextElement();
            Utils.addKeyValueToHashMapWithChecking(table, Utils.methodname(method), method);
        }
    }
}
