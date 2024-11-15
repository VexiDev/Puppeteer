package gg.vexi.Puppeteer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUtils {

    /**
     * Deeply compares two maps including nested maps, lists, and primitive values.
     *
     * @param <T>      The type of values in the map
     * @param expected The expected map
     * @param actual   The actual map to compare against
     * @param path     The current path in the object structure (for error messages)
     */
    @SuppressWarnings("unchecked")
    public static <T> void assertMapEquals(Map<String, T> expected, Map<String, T> actual, String... path) {
        String currentPath = path.length > 0 ? String.join(".", path) + "." : "";

        for (String key : expected.keySet()) {
            String fullPath = currentPath + key;
            assertTrue(actual.containsKey(key), "Missing key at " + fullPath);

            T expectedValue = expected.get(key);
            T actualValue = actual.get(key);

            if (expectedValue == null) {
                assertNull(actualValue, "Value at " + fullPath + " should be null");
            } else {
                assertNotNull(actualValue, "Value at " + fullPath + " should not be null");

                if (expectedValue instanceof Map) {
                    assertTrue(actualValue instanceof Map,
                            "Value at " + fullPath + " should be a Map but was "
                                    + actualValue.getClass().getSimpleName());
                    assertMapEquals(
                            (Map<String, Object>) expectedValue,
                            (Map<String, Object>) actualValue,
                            appendPath(path, key));
                } else if (expectedValue instanceof List) {
                    assertTrue(actualValue instanceof List,
                            "Value at " + fullPath + " should be a List but was "
                                    + actualValue.getClass().getSimpleName());
                    assertListEquals(
                            (List<Object>) expectedValue,
                            (List<Object>) actualValue,
                            appendPath(path, key));
                } else {
                    assertEquals(
                            expectedValue,
                            actualValue,
                            "Mismatch at " + fullPath + ": expected " + expectedValue + " but got " + actualValue);
                }
            }
        }

        assertEquals(
                expected.size(),
                actual.size(),
                "Different Map sizes at " + currentPath + " - expected: " + expected.size() + ", actual: "
                        + actual.size());
    }

    /**
     * Deeply compares two lists including nested maps, lists, and primitive values.
     *
     * @param expected The expected list
     * @param actual   The actual list to compare against
     * @param path     The current path in the object structure (for error messages)
     */
    @SuppressWarnings("unchecked")
    private static void assertListEquals(List<Object> expected, List<Object> actual, String... path) {
        String currentPath = String.join(".", path);

        assertEquals(
                expected.size(),
                actual.size(),
                "Different List sizes at " + currentPath + " - expected: " + expected.size() + ", actual: "
                        + actual.size());

        for (int i = 0; i < expected.size(); i++) {
            Object expectedValue = expected.get(i);
            Object actualValue = actual.get(i);
            String fullPath = currentPath + "[" + i + "]";

            if (expectedValue == null) {
                assertNull(actualValue, "Value at " + fullPath + " should be null");
            } else {
                assertNotNull(actualValue, "Value at " + fullPath + " should not be null");

                if (expectedValue instanceof Map) {
                    assertTrue(actualValue instanceof Map,
                            "Value at " + fullPath + " should be a Map but was "
                                    + actualValue.getClass().getSimpleName());
                    assertMapEquals(
                            (Map<String, Object>) expectedValue,
                            (Map<String, Object>) actualValue,
                            appendPath(path, String.valueOf(i)));
                } else if (expectedValue instanceof List) {
                    assertTrue(actualValue instanceof List,
                            "Value at " + fullPath + " should be a List but was "
                                    + actualValue.getClass().getSimpleName());
                    assertListEquals(
                            (List<Object>) expectedValue,
                            (List<Object>) actualValue,
                            appendPath(path, String.valueOf(i)));
                } else {
                    assertEquals(
                            expectedValue,
                            actualValue,
                            "Mismatch at " + fullPath + ": expected " + expectedValue + " but got " + actualValue);
                }
            }
        }
    }

    /**
     * Helper method to append a new element to the path array
     * @param path The current path
     * @param element The new element to append
     * @return The new path array
     */
    private static String[] appendPath(String[] path, String element) {
        String[] newPath = new String[path.length + 1];
        System.arraycopy(path, 0, newPath, 0, path.length);
        newPath[path.length] = element;
        return newPath;
    }
}
