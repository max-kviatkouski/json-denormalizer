import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.*;

public class JsonToTable {
    public static void main(String args[]) {
    }

    public List<Map<String, String>> jsonToMap(JsonNode n, String keyContext) {
        if (!n.isContainerNode()) {
            return new LinkedList<>() {{
                add(Map.of(keyContext + "_val", n.asText("")));
            }};
        } else if (n.isArray()) {
            List<Map<String, String>> result = new LinkedList<>();
            Iterator<JsonNode> elements = n.elements();
            while (elements.hasNext()) {
                JsonNode e = elements.next();
                result.addAll(jsonToMap(e, keyContext));
            }
            return result;
        } else if (n.isObject()) {
            List<Map<String, String>> result = new LinkedList<>();
            Map<String, String> objectValues = new HashMap<>();
            List<Map<String, String>> childTable = new LinkedList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = n.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> f = fields.next();
                if (!f.getValue().isContainerNode()) {
                    objectValues.put(keyContext + f.getKey(), f.getValue().asText(""));
                } else {
                    childTable.addAll(jsonToMap(f.getValue(), keyContext + f.getKey() + "/"));
                }
//                Multiply child tables by own object fields
            }
            if (childTable.size() == 0) {
                result.add(objectValues);
            } else {
                for (Map<String, String> row : childTable) {
                    HashMap<String, String> _t = new HashMap<>();
                    _t.putAll(objectValues);
                    _t.putAll(row);
                    result.add(_t);
                }
            }
            return result;
        }
        return null;
    }
}
