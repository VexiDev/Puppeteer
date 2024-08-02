package gg.vexi.TicketSystem.ErrorHandling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import gg.vexi.TicketSystem.Exceptions.Error;

class ErrorTests {

    @Test
    public void test_init() {

        Error error = new Error("Test_Error", "Test_Error_Message");

        assertNotNull(error.getId(), "Error id is null");
        assertNotNull(error.getType(), "Error type is null");
        assertNotNull(error.getMessage(), "Error message is null");
    
        assertEquals("Test_Error", error.getType(), "Error type is not set correctly");
        assertEquals("Test_Error_Message", error.getMessage(), "Error message is not set correctly");

    }

}
