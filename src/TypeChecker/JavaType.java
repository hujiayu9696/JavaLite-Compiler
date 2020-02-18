package TypeChecker;

import JavaParser.syntaxtree.MethodDeclaration;
import JavaParser.syntaxtree.Node;
import Utils.Pair;

import java.util.HashMap;
import java.util.Objects;

public class JavaType {

    static void addClassDef(String s, Node n) {
        Utils.addKeyValueToHashMapWithChecking(definedClass, s, n);
    }

    public static String mainClassId;
    public static HashMap<String, Node> definedClass = new HashMap<>();
    public static HashMap<String, HashMap<String, MethodDeclaration>> classMethodTable = new HashMap<>();


    public enum typeCategory {ArrayType, BooleanType, IntegerType, Class}

    static final JavaType ArrayType = new JavaType(typeCategory.ArrayType);
    static final JavaType BooleanType = new JavaType(typeCategory.BooleanType);
    static final JavaType IntegerType = new JavaType(typeCategory.IntegerType);

    typeCategory category;
    String classId;

    public JavaType(typeCategory category) {
        this.category = category;
    }

    public JavaType(String classId) {
        this.category = typeCategory.Class;
        this.classId = classId;
        if (!definedClass.containsKey(classId))
            throw new RuntimeException("Type " + classId + " is not defined");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaType javaType = (JavaType) o;
        return category == javaType.category &&
                Objects.equals(classId, javaType.classId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, classId);
    }

    public boolean subtype(JavaType t)
    {
        Utils.checkCondition(t != null);
        if (this.equals(t))
            return true;
        if (this.category != typeCategory.Class || t.category != typeCategory.Class)
            return false;
        Pair<String, String> linkset = HelperFunctions.linkset(this.classId);
        if (linkset == null)
            return false;
        return new JavaType(linkset.value).subtype(t);
    }

    @Override
    public String toString() {
        switch (category) {
            case Class:
                return classId;
            case ArrayType:
                return "int[]";
            case BooleanType:
                return "boolean";
            case IntegerType:
                return "int";
            default:
                throw new RuntimeException("Unknown Type");
        }
    }
}
