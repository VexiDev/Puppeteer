package gg.vexi.TicketSystem.ErrorHandling;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;

class CaughtExceptionsTests {

    CaughtExceptions CaughtExceptions;

    @BeforeEach
    public void setup() {
        CaughtExceptions = new CaughtExceptions();
    }


    @Test
    public void test_init() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {

        // get errors map from caughtexceptions using reflection
        Field CaughtExceptions_field = CaughtExceptions.getClass().getDeclaredField("Errors");
        CaughtExceptions_field.setAccessible(true);
        ArrayList<Error> errors_list
        = (ArrayList<Error>) CaughtExceptions_field.get(CaughtExceptions);

        //verify errors is an empty map and is not null
        assertNotNull(errors_list, "Errors map is null");
        assertEquals(0, errors_list.size(), "Errors map is not an empty hashmap");

    }

}
