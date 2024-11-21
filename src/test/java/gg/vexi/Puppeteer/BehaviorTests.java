package gg.vexi.Puppeteer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ExamplePuppets.ExamplePuppet_String;
import gg.vexi.Puppeteer.Ticket.Result;
import gg.vexi.Puppeteer.Ticket.TicketPriority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class _Behavior {

    private Puppeteer puppeteer;

    @BeforeEach
    public void setup() {
        puppeteer = new Puppeteer();
        puppeteer.registerPuppet("test_action", ExamplePuppet_String.class);
    }

    // -----------------------------------------------------------------------------
    // --vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv--
    // --> ALL METHODS IN THIS FILE MUST BE RENAMED TO REPRESENT WHAT THEY TEST! <--
    // --^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^--
    // -----------------------------------------------------------------------------

    // (it does a full interaction test from going from:
    // only parameters -> queued ticket -> waiting for ticket completion -> handling completion
    @Test
    public void test_SingleTicketHandling() {
        // create ticket objects
        String ticket_puppet = "test_action";
        TicketPriority priority = TicketPriority.NORMAL;
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put("test_customer_parameter_1", true);
        parameters.put("test_customer_parameter_2", 0);
        parameters.put("test_customer_parameter_3", "This is the third parameter for our ticket");

        // queue ticket
        Ticket<String> ticket = puppeteer.queueTicket(ticket_puppet, priority, parameters);

        // wait for future
        CompletableFuture<Result<String>> ticketFuture = ticket.future();

        // set check value to ensure execution order
        AtomicInteger check = new AtomicInteger(0);
        AtomicInteger expected_check = new AtomicInteger(1);

        // <<<< EXECUTES FIRST WHEN TICKET FUTURE COMPLETE >>>>>>>
        ticketFuture.thenAccept(result -> {
            // verify current check and incrememnt check by 1
            assertEquals(new AtomicInteger(0).get(), check.getAndSet(1), "ticketFuture.thenAccept did not run first");
        });

        // wait for ticket result
        AtomicReference<Result<String>> ticket_result_holder = new AtomicReference<>();
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Result<String> ticket_result = ticketFuture.join();
            ticket_result_holder.set(ticket_result);
        }, "Ticket future took too long to complete\n");

        Result<String> ticket_result = ticket_result_holder.get();

        // <<<< EXECUTES AFTER thenAccept >>>>>>>
        assertEquals(expected_check.get(), check.get(), "ticketFuture.thenAccept did not run first");

        // verify ticket_result
        assertNotNull(ticket_result, "Ticket result does not exist");
        assertEquals(true, ticket_result instanceof Result, "Ticket future result is not a TicketResult object");
    }

    @Test
    public void test_MultiTicketHandling() {

        // create ticket arguments
        String ticket_puppet = "test_action";
        TicketPriority priority = TicketPriority.NORMAL;
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put("test", 0);

        int num_test_tickets = 5;

        List<Ticket<String>> tickets = new ArrayList<>();
        List<CompletableFuture<Result<String>>> futures = new ArrayList<>();

        for ( int i = 0; i <= num_test_tickets - 1; i++ ) {
            Ticket<String> ticket = puppeteer.createTicket(ticket_puppet, priority, parameters);
            tickets.add(ticket);
            futures.add(ticket.future());
        }

        AtomicInteger a = new AtomicInteger(0);
        // queue all the tickets
        for ( Ticket<String> ticket : tickets ) {
            puppeteer.queueTicket(ticket);
            ticket.future().thenAccept(result -> {
                if ( result.isSuccessful() ) { a.getAndIncrement(); }
            });
        }

        // ensure all puppets completed successfully within the alloted time
        assertTimeoutPreemptively(
            Duration.ofMillis(tickets.size() * 300), // 100ms overhead for each future
            ()
                -> {
                CompletableFuture.allOf(futures.toArray(CompletableFuture[] ::new)).join();
                assertEquals(tickets.size(), a.get(), "All Puppets did not complete");
            },
            "Puppets did not complete in time"
        );
    }

    @Test
    public void test_queueIntegrity() {

        // This test is to ensure that queues correctly poll correctly and maintain queue sizing
        // Note:
        // - This test assumes an empty queue when it executes! If the queue is not empty it will fail!
        // - This test was written in a sleep deprived state a may not accurately test for queue integrity
        // failure!

        // create ticket arguments
        String ticket_puppet = "test_action";
        TicketPriority priority = TicketPriority.NORMAL;
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put("test_customer_parameter_1", true);
        parameters.put("test_customer_parameter_2", 0);
        parameters.put("test_customer_parameter_3", "This is the third parameter for our ticket");

        int num_test_tickets = 5;

        List<Ticket<String>> tickets = new ArrayList<>();
        List<CompletableFuture<Result<String>>> futures = new ArrayList<>();
        AtomicInteger completedCount = new AtomicInteger(0);

        // this loop queues puppets and ensures the queue size increases accordingly
        for ( int i = 0; i <= num_test_tickets - 1; i++ ) {

            Ticket<String> ticket = puppeteer.createTicket(ticket_puppet, priority, parameters);
            tickets.add(ticket);
            futures.add(ticket.future());
            puppeteer.queueTicket(ticket);
            int expected = puppeteer.getQueue(ticket_puppet).size();
            assertEquals(
                expected, i
            ); // 0 on first loop works because the first ticket is immediately polled from the queue
        }

        assertEquals(5, tickets.size());
        assertEquals(5, futures.size());

        // this loop ensures that the queue decreases when a ticket ends (if it doesn't then we trigger a
        // flag)
        // - This flag based implementation is pretty bruteforce and should
        // be refactored such that we immediately fail the test when a mismatch is detected!
        // - Additionally since a queue integrity failure would cause our futures
        // to never complete we need a timeout on the test
        // (we could have puppets be able to kill themselves or
        // ticketmanager kills a puppet [heartbeat style implementation])
        AtomicBoolean flag = new AtomicBoolean(false);
        for ( int i = 0; i <= tickets.size() - 1; i++ ) {

            // wait for tickets to complete and ensure the queue is the size we expect
            futures.get(i).thenRun(() -> {
                int actual = puppeteer.getQueue(ticket_puppet).size();
                int totalCompleted = completedCount.incrementAndGet();
                int expected = futures.size() - totalCompleted;
                if ( expected != actual ) { flag.set(true); }
            });
        }
        assertTimeoutPreemptively(
            Duration.ofMillis(futures.size() * 2000), // 100ms overhead for each future
            ()
                -> {
                CompletableFuture.allOf(futures.toArray(CompletableFuture[] ::new)).join();
                assertEquals(false, flag.get(), "Queue count mismatch during processing!");
            },
            "Queued tickets took to long to execute (likely means that we failed to execute all tickets!)\nEnsure "
                + "that ticket queues are not being cleared and all ticket futures complete eventually!\n"
        );
    }
}
