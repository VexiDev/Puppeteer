package gg.vexi.TicketSystem.ErrorHandling;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
import gg.vexi.TicketSystem.Exceptions.ExceptionRecord;
import gg.vexi.TicketSystem.TestUtils;
import static gg.vexi.TicketSystem.TestUtils.this_method_does_nothing;

class _CaughtExceptions {

    CaughtExceptions CaughtExceptions;
    ArrayList<ExceptionRecord> errors_list;

    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {

        CaughtExceptions = new CaughtExceptions();

        Field CaughtExceptions_field = CaughtExceptions.getClass().getDeclaredField("Exceptions");
        CaughtExceptions_field.setAccessible(true);
        errors_list = (ArrayList<ExceptionRecord>) CaughtExceptions_field.get(CaughtExceptions);
    }

    @Test
    public void test_init() {

        //verify errors is an empty map and is not null
        assertNotNull(errors_list, "Exceptions map is null");
        assertEquals(0, errors_list.size(), "Exceptions map is not an empty hashmap");

        //vscode is highlighting _ExceptionRecord as unused and its annoying me
        // until i find out how to make vscode notice it i will be `using` it here -__-
        this_method_does_nothing(new _ExceptionRecord());
    }

    @Test
    public void test_add() {

        ExceptionRecord error = new ExceptionRecord("Test_Error", "Error_Message");
        CaughtExceptions.add(error);

        assertEquals(1, errors_list.size(), "Exceptions list size mismatch");
    }

    @Test
    public void test_any() {

        assertFalse(CaughtExceptions.any(), "Any() returned true with no errors");

        ExceptionRecord error = new ExceptionRecord("Test_Error", "Error_Message");
        errors_list.add(error);

        assertTrue(CaughtExceptions.any(), "Any() returned false with no errors");

    }

    @Test
    public void test_getAsJson() {

        // build expected json object
        JsonObject expected_json = new JsonObject();
        for (int i = 0; i < 3; i++) {
            JsonObject error = new JsonObject();
            error.addProperty("id", i);
            error.addProperty("message", "Error_Message" + i);

            String type = (i < 2) ? "type1" : "type2";
            if (!expected_json.has(type)) {
                expected_json.add(type, new JsonArray());
            }
            expected_json.getAsJsonArray(type).add(error);
        }

        // add errors to errors_list
        ExceptionRecord error_0 = new ExceptionRecord("type1", "Error_Message0");
        ExceptionRecord error_1 = new ExceptionRecord("type1", "Error_Message1");
        ExceptionRecord error_2 = new ExceptionRecord("type2", "Error_Message2");
        errors_list.add(error_0);
        errors_list.add(error_1);
        errors_list.add(error_2);

        // convert errors_list to json
        JsonObject actual_json = CaughtExceptions.getAsJson();

        //because id's are unique and predicting them is annoying
        //IgnoringId = skips id value if it is a valid long
        TestUtils.assertJsonEqualsIgnoringId(expected_json, actual_json);

    }

    // Exception Records are the type and message associated with an exception
    @Nested
    class _ExceptionRecord {

        @Test
        public void test_init() {

            ExceptionRecord error = new ExceptionRecord("Test_Error", "Test_Error_Message");

            assertNotNull(error.getId(), "ExceptionRecord id is null");
            assertNotNull(error.getType(), "ExceptionRecord type is null");
            assertNotNull(error.getMessage(), "ExceptionRecord message is null");

            assertEquals("Test_Error", error.getType(), "ExceptionRecord type is not set correctly");
            assertEquals("Test_Error_Message", error.getMessage(), "ExceptionRecord message is not set correctly");

        }

    }
}
