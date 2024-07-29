package gg.vexi.TicketSystem;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketPriority;
import gg.vexi.TicketSystem.Ticket.TicketResult;

class Test_Customer {

    private TicketManager TicketManager;

    @BeforeEach
    public void setup() {
        TicketManager = new TicketManager();
    }

    @Test
    @Timeout(5)
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
        AtomicReference<TicketResult> ticket_result_holder = new AtomicReference<>();
        assertTimeoutPreemptively(
                Duration.ofSeconds(5),
                () -> {
                    TicketResult ticket_result = ticketFuture.join();
                    ticket_result_holder.set(ticket_result);
                },
                "ticket future took too long to execute (>5 seconds)"
        );

        TicketResult ticket_result = ticket_result_holder.get();

        // <<<< EXECUTES AFTER thenAccept >>>>>>>
        assertEquals(1, check, "ticketFuture.thenAccept did not run first");

        // verify ticket_result
        assertNotNull(ticket_result, "Ticket result does not exist");
        assertEquals(true, ticket_result instanceof TicketResult, "Ticket future result is not a TicketResult object");

    }

}
