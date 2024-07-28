package gg.vexi.TicketSystem;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.TicketPriority;
import gg.vexi.TicketSystem.Ticket.TicketResult;

class Test_Customer {

    private TicketManager TicketManager;

    @BeforeEach
    public void setup() {
        TicketManager = new TicketManager();
    }

    @Test
    public void Test_TicketHandlingBehavior() { // this method should be renamed!

        // create ticket objects
        ActionType ticket_type = ActionType.ACTION;
        TicketPriority priority = TicketPriority.NORMAL;
        JsonObject parameters = new JsonObject();
        parameters.addProperty("test_customer_parameter_1", true);
        parameters.addProperty("test_customer_parameter_2", 0);
        parameters.addProperty("test_customer_parameter_3", "This is the third parameter for our ticket");

        // queue ticket
        Ticket ticket = TicketManager.queueTicket(ticket_type, priority, parameters);

        // wait for future
        CompletableFuture<TicketResult> ticketFuture = ticket.getFuture();
        
        // set check value to ensure execution order
        AtomicInteger check = new AtomicInteger(0);

        // <<<< EXECUTES FIRST WHEN TICKET FUTURE COMPLETE >>>>>>>
        ticketFuture.thenAccept(result -> {

            // verify check
            assertEquals(0, check.incrementAndGet(), "ticketFuture.thenAccept did not run first");

        });

        // wait for ticket result
        TicketResult ticket_result = ticketFuture.join();

        // <<<< EXECUTES AFTER thenAccept >>>>>>>
        assertEquals(1, check, "ticketFuture.thenAccept did not run first");
        
        // verify ticket_result
        assertNotNull(ticket_result, "Ticket result does not exist");
        assertEquals(true, ticket_result instanceof TicketResult, "Ticket future result is not a TicketResult object");


    }

}
