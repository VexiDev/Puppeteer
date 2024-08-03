package gg.vexi.TicketSystem.ErrorHandling;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Exceptions.Anomaly;
import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
import gg.vexi.TicketSystem.TestUtils;

class CaughtExceptionsTests {

    CaughtExceptions CaughtExceptions;
    ArrayList<Anomaly> errors_list;

    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {

        CaughtExceptions = new CaughtExceptions();

        Field CaughtExceptions_field = CaughtExceptions.getClass().getDeclaredField("Errors");
        CaughtExceptions_field.setAccessible(true);
        errors_list = (ArrayList<Anomaly>) CaughtExceptions_field.get(CaughtExceptions);

    }

    @Test
    public void test_init() {

        //verify errors is an empty map and is not null
        assertNotNull(errors_list, "Errors map is null");
        assertEquals(0, errors_list.size(), "Errors map is not an empty hashmap");

    }

    @Test
    public void test_add() {

        Anomaly error = new Anomaly("Test_Error", "Error_Message");
        CaughtExceptions.add(error);

        assertEquals(1, errors_list.size(), "Errors list size mismatch");
    }

    @Test
    public void test_any() {

        assertFalse(CaughtExceptions.any(), "Any() returned true with no errors");

        Anomaly error = new Anomaly("Test_Error", "Error_Message");
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
        Anomaly error_0 = new Anomaly("type1", "Error_Message0");
        Anomaly error_1 = new Anomaly("type1", "Error_Message1");
        Anomaly error_2 = new Anomaly("type2", "Error_Message2");
        errors_list.add(error_0);
        errors_list.add(error_1);
        errors_list.add(error_2);

        // convert errors_list to json
        JsonObject actual_json = CaughtExceptions.getAsJson();

        //because id's are unique and predicting them is annoying
        //IgnoringId = skips id value if it is a valid long
        TestUtils.assertJsonEqualsIgnoringId(expected_json, actual_json);

    }
}
