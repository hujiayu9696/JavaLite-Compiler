package JavaToVapor;

import java.util.LinkedHashMap;
import java.util.Map;

// Object can be String(local or parameter) or Integer(field offset starting from 1)
public class SymbolTable {
    LinkedHashMap<String, Object> data;

    // Used to store all the types for field, parameter, local, and temporary vapor variables.
    LinkedHashMap<String, String> typeTable = new LinkedHashMap<>();
    String getTempExpType(String vid)
    {
        String res = typeTable.get(vid);
        Utils.checkCondition(res != null);
        return res;
    }

    public SymbolTable() {
        data = new LinkedHashMap<>();
    }

    public Object lookUp(String id)
    {
        Object res = data.get(id);
        if (res == null)
            throw new RuntimeException("Identifier " + id + " is not defined");
        return res;
    }

    public void addSymbol(String id, Object idVapor, String type) {
        data.put(id, idVapor);
        typeTable.put(id, type);
    }

    public SymbolTable appendTable(SymbolTable table) {
        for (Map.Entry<String, Object> e : table.data.entrySet())
            data.put(e.getKey(), e.getValue());
        for (Map.Entry<String, String> e : table.typeTable.entrySet())
            typeTable.put(e.getKey(), e.getValue());
        return this;
    }
}
