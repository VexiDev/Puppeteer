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
import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketPriority;
import gg.vexi.TicketSystem.Ticket.TicketResult;

class _Ticket {

    private Ticket Ticket;

    @BeforeEach
    public void setup() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("setting_example", true);
        Ticket = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, parameters, new CompletableFuture<>());
    }

    @Test
    public void test_init() {
        // check if ticket is initialized correctly
        
        // create expected objects
        JsonObject expected_parameters = new JsonObject();
        expected_parameters.addProperty("setting_example", true);

        // verify ticket exists
        assertNotNull(Ticket, "Ticket is Null");

        // verify id exists
        assertNotNull(Ticket.getId(), "Ticket ID is Null");
        
        // verify type
        assertNotNull(Ticket.getType(), "Ticket has no action type");
        assertTrue(Ticket.getType() instanceof ActionType, "Ticket does not have a valid action type");

        // verify priority
        assertNotNull(Ticket.getPriority(), "Ticket has no priority level");
        assertTrue(Ticket.getPriority() instanceof TicketPriority, "Ticket does not have a valid priority level");

        // verify parameters
        assertNotNull(Ticket.getParameters(), "Ticket has no action type");
        assertJsonObjectEquals(expected_parameters, Ticket.getParameters());

        // verify future
        assertNotNull(Ticket.getFuture(), "Ticket has no future");
        assertTrue(Ticket.getFuture() instanceof CompletableFuture<TicketResult>);

        //vscode is highlighting _TicketResult as unused and its annoying me
        // until i find out how to make vscode notice it i will be `using` it here -__-
        this_method_does_nothing(new _TicketResult());

    }

    // TicketResult is the object returned by a worker to TicketManager to finish processing the ticket
    @Nested
    class _TicketResult { 
        
        @Test
        public void test_TicketResult() {

            TicketResult ticketResult = new TicketResult(new CaughtExceptions(), Ticket, Status.CREATED, null);
            assertNotNull(ticketResult, "TicketResult is null");

            assertNotNull(ticketResult.getTicket(), "TicketResult associated ticket is null");
            assertNotNull(ticketResult.getStatus(), "TicketResult status enum (status code) is null");
            assertNotNull(ticketResult.getExceptions(), "TicketResult CaughtExceptions is null");
        }

            // wrapper of if (status == SUCCESS)
            @Test
            @Disabled("Test not implemente")
            public void test_isSuccessful() {}

            // technically direct wrapper of CaughtExceptions.any(); but i guess its actually a wrapper for TicketResult.getExceptions().any();
            @Test
            @Disabled("Test not implemented")
            public void test_hasExceptions() {}


    }
}