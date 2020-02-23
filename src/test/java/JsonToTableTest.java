import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonToTableTest {

    @Test
    @DisplayName("Converts POJO json")
    void primitivesToRow() throws IOException {
        Map<String, String> expected = Map.of(
                "make", "Mazda",
                "model", "323",
                "year", "1981"
        );
        JsonNode jsonNode = getJsonNodeFromFile("pojo.json");
        JsonToTable jsonToTable = new JsonToTable();

        Map<String, String> actual = jsonToTable.primitivesToMap(jsonNode, "");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Converts POJO and Ignores Nested Objects and Arrays")
    void convertsPOJOAndIgnoresNestedObjectsAndArrays() throws IOException {
        Map<String, String> expected = Map.of(
                "make", "Mazda",
                "model", "323",
                "year", "1981"
        );
        JsonNode jsonNode = getJsonNodeFromFile("pojo-with-pojo-and-array.json");
        JsonToTable jsonToTable = new JsonToTable();

        Map<String, String> actual = jsonToTable.primitivesToMap(jsonNode, "");
        assertEquals(expected, actual);
    }

    private JsonNode getJsonNodeFromFile(String fileName) throws IOException {
        return new ObjectMapper().readTree(
                new File(this.getClass().getClassLoader().getResource(fileName).getFile())
        );
    }

    @Test
    @DisplayName("Converts POJO into single-element list with a map inside")
    void jsonToMap() throws IOException {
        List<Map<String, String>> expected = List.of(
                Map.of(
                        "make", "Mazda",
                        "model", "323",
                        "year", "1981"
                )
        );
        JsonNode jsonNode = getJsonNodeFromFile("pojo.json");
        JsonToTable jsonToTable = new JsonToTable();

        List<Map<String, String>> actual = jsonToTable.jsonToMap(jsonNode, "");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Converts POJO and adds nested pojos with hierarchical keys")
    void jsonToMapNestedPojos() throws IOException {
        List<Map<String, String>> expected = List.of(
                Map.of(
                        "make", "Mazda",
                        "model", "323",
                        "year", "1981",
                        "stats/weight", "3100",
                        "stats/towing_capacity", "5000"
                )
        );
        JsonNode jsonNode = getJsonNodeFromFile("pojo-with-pojo.json");
        JsonToTable jsonToTable = new JsonToTable();

        List<Map<String, String>> actual = jsonToTable.jsonToMap(jsonNode, "");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Converts POJO and adds nested primitive array with hierarchical keys")
    void jsonToMapPojoWithPrimitiveArray() throws IOException {
        List<Map<String, String>> expected = List.of(
                Map.of(
                        "make", "Mazda",
                        "model", "323",
                        "year", "1981",
                        "features/_val", "Climate control"
                ),
                Map.of(
                        "make", "Mazda",
                        "model", "323",
                        "year", "1981",
                        "features/_val", "Heated seats"
                )
        );
        JsonNode jsonNode = getJsonNodeFromFile("pojo-with-primitive-array.json");
        JsonToTable jsonToTable = new JsonToTable();

        List<Map<String, String>> actual = jsonToTable.jsonToMap(jsonNode, "");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Handles double nested arrays")
    void jsonToMapMultiLevelArray() throws IOException {
        List<Map<String, String>> expected = List.of(
                Map.of("model","323","year","1981","make","Mazda","features/name","Climate control","features/perks/_val","Automated temperature control"),
                Map.of("model","323","year","1981","make","Mazda","features/name","Climate control","features/perks/_val","Quick window defrosting"),
                Map.of("model","323","year","1981","make","Mazda","features/name","Powered seats","features/perks/_val","3-set memory for driver"),
                Map.of("model","323","year","1981","make","Mazda","features/name","Powered seats","features/perks/_val","9-position adjustment"),
                Map.of("model","323","year","1981","make","Mazda","features/name","Trunk enhancement","features/perks/_val","Enhanced interior lights for trunk"),
                Map.of("model","323","year","1981","make","Mazda","features/name","Trunk enhancement","features/perks/sub_perk1/_val","Spill-proof trunk liner"),
                Map.of("model","323","year","1981","make","Mazda","features/name","Trunk enhancement","features/perks/sub_perk1/_val","Cargo net"),
                Map.of("model","323","year","1981","make","Mazda","features/name","Trunk enhancement","features/perks/sub_perk2/_val","Emergency toolkit"),
                Map.of("model","323","year","1981","make","Mazda","features/name","Trunk enhancement","features/perks/sub_perk2/_val","Organizer")
        );
        JsonNode jsonNode = getJsonNodeFromFile("multilevel-array.json");
        JsonToTable jsonToTable = new JsonToTable();

        List<Map<String, String>> actual = jsonToTable.jsonToMap(jsonNode, "");
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Handles single level plain array")
    void jsonToMapFirstLevelPlainArray() throws IOException {
        List<Map<String, String>> expected = List.of(
                Map.of("_val", "Value 1"),
                Map.of("_val", "Value 2")
        );
        JsonNode jsonNode = getJsonNodeFromFile("plain-array.json");
        JsonToTable jsonToTable = new JsonToTable();

        List<Map<String, String>> actual = jsonToTable.jsonToMap(jsonNode, "");
        assertEquals(expected, actual);
    }
}