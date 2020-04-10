package TypeChecker;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    HashMap<String, JavaType> data;

    public SymbolTable() {
        data = new HashMap<>();
    }

    public JavaType lookUp(String id)
    {
        JavaType res = data.get(id);
        if (res == null)
            throw new RuntimeException("Identifier " + id + " is not defined");
        return res;
    }

    public void addSymbol(String id, JavaType type) {
        Utils.addKeyValueToHashMapWithChecking(data, id, type);
    }

    public SymbolTable appendTable(SymbolTable table, boolean useChecking) {
        for (Object o : table.data.entrySet()) {
            Map.Entry<String, JavaType> e = (Map.Entry<String, JavaType>) o;
            if (useChecking)
                Utils.addKeyValueToHashMapWithChecking(data, e.getKey(), e.getValue());
            else
                data.put(e.getKey(), e.getValue());
        }
        return this;
    }
}
