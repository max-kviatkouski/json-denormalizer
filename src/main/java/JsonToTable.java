import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.*;

public class JsonToTable {
    public static void main(String args[]) {
    }

    public Map<String, String> primitivesToMap(JsonNode n, String keyContext) {
//        todo: possible optimization: this method can also return names of all non-primitive fields
//        so we don't have to iterate twice
        Map<String, String> result = new HashMap<>();
        if (n.isValueNode()) {
            result.put(keyContext + "_val", n.asText(""));
        } else {
            Iterator<Map.Entry<String, JsonNode>> fields = n.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> f = fields.next();
                JsonNodeType nodeType = f.getValue().getNodeType();
                if (!f.getValue().isContainerNode()) {
                    result.put(keyContext + f.getKey(), f.getValue().asText(""));
                }
            }
        }
        return result;
    }

    public List<Map<String, String>> jsonToMap(JsonNode n, String keyContext) {
        List<Map<String, String>> result = new LinkedList<>();
        Map<String, String> currentMap = primitivesToMap(n, keyContext);
        Iterator<Map.Entry<String, JsonNode>> fields = n.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> f = fields.next();
            JsonNodeType nodeType = f.getValue().getNodeType();
            if (nodeType == JsonNodeType.OBJECT) {
                List<Map<String, String>> subTable = jsonToMap(f.getValue(), keyContext + f.getKey() + "/");
                for (Map<String, String> m : subTable) {
                    HashMap<String, String> _t = new HashMap<>(currentMap);
                    _t.putAll(m);
                    result.add(_t);
                }
            } else if (nodeType == JsonNodeType.ARRAY) {
                Iterator<JsonNode> arrayElements = f.getValue().elements();
                while (arrayElements.hasNext()) {
                    JsonNode arrayElement = arrayElements.next();
                    List<Map<String, String>> subTable = jsonToMap(arrayElement, keyContext + f.getKey() + "/");
                    for (Map<String, String> m : subTable) {
                        HashMap<String, String> _t = new HashMap<>(currentMap);
                        _t.putAll(m);
                        result.add(_t);
                    }
                }
            }
        }
        if (result.size() == 0) {
            LinkedList<Map<String, String>> singleResult = new LinkedList<Map<String, String>>();
            singleResult.add(currentMap);
            return singleResult;
        } else {
            return result;
        }
    }
}
