package gg.vexi.TicketSystem.ErrorHandling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import gg.vexi.TicketSystem.Exceptions.Anomaly;

class ErrorTests {

    @Test
    public void test_init() {

        Anomaly error = new Anomaly("Test_Error", "Test_Error_Message");

        assertNotNull(error.getId(), "VError id is null");
        assertNotNull(error.getType(), "VError type is null");
        assertNotNull(error.getMessage(), "VError message is null");

        assertEquals("Test_Error", error.getType(), "VError type is not set correctly");
        assertEquals("Test_Error_Message", error.getMessage(), "VError message is not set correctly");

    }

}
