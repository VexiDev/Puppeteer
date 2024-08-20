package gg.vexi.TicketSystem;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
import static gg.vexi.TicketSystem.TestUtils.assertJsonObjectEquals;
import static gg.vexi.TicketSystem.TestUtils.this_method_does_nothing;
import gg.vexi.TicketSystem.ticket.Ticket;
import gg.vexi.TicketSystem.ticket.TicketPriority;
import gg.vexi.TicketSystem.ticket.TicketResult;

class _Ticket {

    private Ticket ticket;

    @BeforeEach
    public void setup() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("setting_example", true);
        ticket = new Ticket("test_action", TicketPriority.NORMAL, parameters, new CompletableFuture<>());
    }

    @Test
    public void test_init() {
        // check if ticket is initialized correctly
        
        // create expected objects
        JsonObject expected_parameters = new JsonObject();
        expected_parameters.addProperty("setting_example", true);

        // verify ticket exists
        assertNotNull(ticket, "Ticket is Null");

        // verify id exists
        assertNotNull(ticket.getId(), "Ticket ID is Null");
        
        // verify type
        assertNotNull(ticket.getType(), "Ticket has no action type");
        assertTrue(ticket.getType() instanceof String, "Ticket does not have a valid action type");

        // verify priority
        assertNotNull(ticket.getPriority(), "Ticket has no priority level");
        assertTrue(ticket.getPriority() instanceof TicketPriority, "Ticket does not have a valid priority level");

        // verify parameters
        assertNotNull(ticket.getParameters(), "Ticket has no action type");
        assertJsonObjectEquals(expected_parameters, ticket.getParameters());

        // verify future
        assertNotNull(ticket.getFuture(), "Ticket has no future");
        assertTrue(ticket.getFuture() instanceof CompletableFuture<TicketResult>);

        //vscode is highlighting _TicketResult as unused and its annoying me
        // until i find out how to make vscode notice it i will be `using` it here -__-
        this_method_does_nothing(new _TicketResult());

    }

    // TicketResult is the object returned by a worker to TicketManager to finish processing the ticket
    @Nested
    class _TicketResult { 
        
        @Test
        public void test_init() {

            TicketResult TicketResult = new TicketResult(new CaughtExceptions(), ticket, Status.CREATED, null);

            assertNotNull(TicketResult, "TicketResult is null");
            assertNotNull(TicketResult.getTicket(), "TicketResult associated ticket is null");
            assertNotNull(TicketResult.getStatus(), "TicketResult status enum (status code) is null");
            assertNotNull(TicketResult.getExceptions(), "TicketResult CaughtExceptions is null");
        }

        @Test
        // @Disabled("Test not implemented") 
        public void test_isSuccessful() {

            TicketResult TicketResult = new TicketResult(new CaughtExceptions(), ticket, Status.SUCCESS, null);

            boolean isSuccessful = TicketResult.isSuccessful();

            assertNotNull(isSuccessful, "TicketResult returned null when checking status");
            assertTrue(isSuccessful, "TicketResult was not successful");

        }

        // technically direct wrapper of CaughtExceptions.any(); but i guess its actually a wrapper for TicketResult.getExceptions().any();
        @Test
        @Disabled("Test not implemented")
        public void test_hasExceptions() {}


    }
}