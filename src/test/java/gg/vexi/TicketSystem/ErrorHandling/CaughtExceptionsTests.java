package gg.vexi.TicketSystem.ErrorHandling;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ErrorHandlingTests {

    CaughtExceptions CaughtExceptions;

    @BeforeEach
    public void setup() {
        CaughtExceptions = new CaughtExceptions();
    }


    @Test
    public void test_init() {

        // get errors map from caughtexceptions using reflection
        Field CaughtExceptions_field = CaughtExceptions.getClass().getDeclaredField("Errors");
        CaughtExceptions_field.setAccessible(true);
        ConcurrentHashMap<Long, Error> errors_map 
        = (ConcurrentHashMap<Long, Error>) CaughtExceptions_field.get(CaughtExceptions);

        //verify errors is an empty map and is not null
        assertNotNull(errors_map, "Errors map is null");
        assertEquals(new ConcurrentHashMap<>().size(), errors_map.size(), "Errors map is not an empty hashmap");

    }

}
