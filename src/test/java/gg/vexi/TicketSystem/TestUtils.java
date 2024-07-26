package gg.vexi.TicketSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;

public class TestUtils {

    public static void assertJsonObjectEquals(JsonObject expected, JsonObject actual) {
        for (String key : expected.keySet()) {
            assertTrue(actual.has(key), "Missing key: " + key);
            assertEquals(expected.get(key), actual.get(key), "Mismatch for key: " + key);
        }
        assertEquals(expected.size(), actual.size(), "Different number of properties");
    }
}