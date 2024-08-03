package gg.vexi.TicketSystem;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TestUtils {

    public static void assertJsonObjectEquals(JsonObject expected, JsonObject actual) {
        for (String key : expected.keySet()) {
            assertTrue(actual.has(key), "Missing key: " + key);
            assertEquals(expected.get(key), actual.get(key), "Mismatch for key: " + key);
        }
        assertEquals(expected.size(), actual.size(), "Different JsonObject sizes");
    }


    // only used in ../ErrorHandling/CaughtExceptionsTests.java
    public static void assertJsonEqualsIgnoringId(JsonElement expected, JsonElement actual) {
        if (!expected.getClass().equals(actual.getClass())) {
            fail("JsonElement types do not match");
        }
        
        if (expected.isJsonObject()) {
            JsonObject expectedObj = expected.getAsJsonObject();
            JsonObject actualObj = actual.getAsJsonObject();
            assertEquals(expectedObj.size(), actualObj.size(), "Different JsonObject sizes");
            for (Map.Entry<String, JsonElement> entry : expectedObj.entrySet()) {
                String key = entry.getKey();
                assertTrue(actualObj.has(key), "Missing key: " + key);
                if ("id".equals(key) && entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
                    assertTrue(actualObj.get(key).isJsonPrimitive() && actualObj.get(key).getAsJsonPrimitive().isNumber(),
                            "ID field should be a number in both expected and actual");
                } else {
                    assertJsonEqualsIgnoringId(entry.getValue(), actualObj.get(key));
                }
            }
        } else if (expected.isJsonArray()) {
            JsonArray expectedArr = expected.getAsJsonArray();
            JsonArray actualArr = actual.getAsJsonArray();
            assertEquals(expectedArr.size(), actualArr.size(), "JsonArray sizes do not match");
            for (int i = 0; i < expectedArr.size(); i++) {
                assertJsonEqualsIgnoringId(expectedArr.get(i), actualArr.get(i));
            }
        } else if (expected.isJsonPrimitive()) {
            assertEquals(expected, actual, "Primitive values do not match");
        }
    }
    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    // temp method to `read` an object without doing anything until i figure out how to suppress unused warnings in VSCode
    public static void this_method_does_nothing(Object object) {}

}