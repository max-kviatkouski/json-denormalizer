import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

public class JsonToTable {
    public static void main(String args[]) throws IOException {
        File file = new File(JsonToTable.class.getClassLoader().getResource("sample.json").getFile());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(file);
        JsonToTable jsonToTable = new JsonToTable();
        List<Map<String, String>> sortedUnified = jsonToTable.sortByAllColumns(jsonToTable.unify(jsonToTable.jsonToMap(jsonNode, "")));
        ICsvMapWriter csvMapWriter = new CsvMapWriter(new OutputStreamWriter(System.out), CsvPreference.EXCEL_PREFERENCE);
        String[] sortedColumnNames = sortedUnified.get(0).keySet().stream().sorted(new HierarchicalStringComparator()).collect(Collectors.toList()).toArray(String[]::new);
        csvMapWriter.writeHeader(sortedColumnNames);
        for (Map<String, String> row : sortedUnified) {
            csvMapWriter.write(row, sortedColumnNames);
        }
        csvMapWriter.close();
    }

    /**
     * This method will take a json and transform it into a denormalized table
     * @param n
     * @return Each map in a list represents a row in a table
     */
    public List<Map<String, String>> jsonToMap(JsonNode n) {
        return this.jsonToMap(n, "");
    }

    private List<Map<String, String>> jsonToMap(JsonNode n, String keyContext) {
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
            }
            if (childTable.size() == 0) {
                result.add(objectValues);
//           Multiply child tables by own object fields
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

    /**
     * This method will update table so each Map in the list has exact same set of columns as the others in the list.
     * If a key is missing in a particular table it will be populated with empty string
     *
     * @param table
     */
    public List<Map<String, String>> unify(List<Map<String, String>> table) {
        Set<String> globalKeySet = new HashSet<>();
        List<Map<String, String>> result = new LinkedList<>();
        for (Map<String, String> row : table) {
            globalKeySet.addAll(row.keySet());
        }
        for (Map<String, String> row : table) {
            Map<String, String> unifiedRow = new HashMap<>();
            for (String globalKey : globalKeySet) {
                unifiedRow.put(globalKey, row.getOrDefault(globalKey, ""));
            }
            result.add(unifiedRow);
        }
        return result;
    }

    /**
     * This method takes all column names, sorts them in alphabetical order then sorts all rows by all columns starting
     * with first column.
     * @param table
     * @return
     */
    public List<Map<String, String>> sortByAllColumns(List<Map<String, String>> table) {
        List<Map<String, String>> result = new LinkedList<>(table);
        List<String> globalKeys = table.stream().flatMap(row -> row.keySet().stream()).collect(Collectors.toSet())
                .stream().sorted(new HierarchicalStringComparator()).collect(Collectors.toList());
        result.sort(new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> m1, Map<String, String> m2) {
                for (String globalKey : globalKeys) {
                    int c = m1.get(globalKey).compareTo(m2.get(globalKey));
                    if (c != 0) return c;
                }
                return 0;
            }
        });
        return result;
    }

    /**
     * Puts strings with more forward slashes further. This way simple keys appear first in alphabetical order,
     * then - first-level keys, then - second-level keys and so on.
     */
    static class HierarchicalStringComparator implements Comparator<String> {

        @Override
        public int compare(String s1, String s2) {
            int c1 = StringUtils.countMatches(s1, "/");
            int c2 = StringUtils.countMatches(s2, "/");
            if (c1 == c2) {
                return s1.compareTo(s2);
            } else {
                return Integer.compare(c1, c2);
            }
        }

    }
}
