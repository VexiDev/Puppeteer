package gg.vexi.TicketSystem.ErrorHandling;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import gg.vexi.TicketSystem.Exceptions.Anomaly;
import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;

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

}
