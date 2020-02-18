package TypeChecker;

import JavaParser.syntaxtree.*;
import Utils.Pair;

import java.util.*;

public class HelperFunctions {

    // Return null to represent empty.
    static Pair<String, String> linkset(Node C) {
        if (C instanceof MainClass)
            return null;
        if (C instanceof ClassDeclaration)
            return null;
        if (C instanceof ClassExtendsDeclaration) {
            ClassExtendsDeclaration theClass = (ClassExtendsDeclaration) C;
            String parentClassId = Utils.identifierName(theClass.f3);
            if (parentClassId.equals(JavaType.mainClassId))
                throw new RuntimeException("Cannot Extend minijava Class");
            return new Pair<>(Utils.classname(theClass), parentClassId);
        }
        throw new RuntimeException("Not a Class");
    }

    static Pair<String, String> linkset(String id) {
        return linkset(JavaType.definedClass.get(id));
    }

//    No longer to use this.
//    static boolean distinct(ArrayList<String> l) {
//        ArrayList<String> list = (ArrayList<String>) l.clone();
//        Collections.sort(list);
//        for (int i = 0; i < list.size() - 1; i++)
//            if (list.get(i).equals(list.get(i + 1)))
//                return false;
//        return true;
//    }

    static boolean acyclic(ArrayList<Pair> pairs) {
        HashMap<String, String> table = new HashMap<>();
        for (Pair<String, String> pair : pairs) {
            if (pair == null)
                continue;
            table.put(pair.key, pair.value);
        }
        for (String key : table.keySet()) {
            HashSet<String> visited = new HashSet<>();
            String now = key;
            visited.add(now);
            while (true) {
                now = table.get(now);
                if (now == null)
                    break;
                if (visited.contains(now))
                    return false;
                visited.add(now);
            }
        }
        return true;
    }

    static SymbolTable fields(String id) {
        Node t = JavaType.definedClass.get(id);
        SymbolTable res;
        if (t instanceof ClassDeclaration) {
            ClassDeclaration c = (ClassDeclaration) t;
            NodeListOptional vars = c.f3;
            return Utils.VarDeclarationListToSymbolTable(vars);
        } else if (t instanceof ClassExtendsDeclaration) {
            ClassExtendsDeclaration c = (ClassExtendsDeclaration) t;
            res = fields(Utils.identifierName(c.f3));
            NodeListOptional vars = c.f5;
            return res.appendTable(Utils.VarDeclarationListToSymbolTable(vars), false);
        } else throw new RuntimeException("Should not happen");
    }

}
