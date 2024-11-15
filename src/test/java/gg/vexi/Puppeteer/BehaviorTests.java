package gg.vexi.Puppeteer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ExamplePuppets.Puppet_TestAction;
import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.Result;

class _Behavior {

    private Puppeteer Puppeteer;

    @BeforeEach
    public void setup() {
        Puppeteer = new Puppeteer(false);
        Puppeteer.registerPuppet(Puppet_TestAction.class, "test_action"); 

    }


// -----------------------------------------------------------------------------
// --vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv--
// --> ALL METHODS IN THIS FILE MUST BE RENAMED TO REPRESENT WHAT THEY TEST! <--
// --^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^--
// -----------------------------------------------------------------------------


    //(it does a full interaction test from going from:
    //only parameters -> queued ticket -> waiting for ticket completion -> handling completion(WIP) -> more later on...
    @Test
    public void test_TicketHandling() {
        // create ticket objects
        String ticket_type = "test_action";
        TicketPriority priority = TicketPriority.NORMAL;
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put("test_customer_parameter_1", true);
        parameters.put("test_customer_parameter_2", 0);
        parameters.put("test_customer_parameter_3", "This is the third parameter for our ticket");

        // queue ticket
        Ticket ticket = Puppeteer.queueTicket(ticket_type, priority, parameters);

        // wait for future
        CompletableFuture<Result> ticketFuture = ticket.getFuture();

        // set check value to ensure execution order
        AtomicInteger check = new AtomicInteger(0);
        AtomicInteger expected_check = new AtomicInteger(1);

        // <<<< EXECUTES FIRST WHEN TICKET FUTURE COMPLETE >>>>>>>
        ticketFuture.thenAccept(result -> {

            // verify current check and incrememnt check by 1
            assertEquals(new AtomicInteger(0).get(), check.getAndSet(1), "ticketFuture.thenAccept did not run first");

        });

        // wait for ticket result
        AtomicReference<Result> ticket_result_holder = new AtomicReference<>();
        assertTimeoutPreemptively(
                Duration.ofSeconds(5),
                () -> {
                    Result ticket_result = ticketFuture.join();
                    ticket_result_holder.set(ticket_result);
                },
                "Ticket future took too long to complete\n"
        );

        Result ticket_result = ticket_result_holder.get();

        // <<<< EXECUTES AFTER thenAccept >>>>>>>
        assertEquals(expected_check.get(), check.get(), "ticketFuture.thenAccept did not run first");

        // verify ticket_result
        assertNotNull(ticket_result, "Ticket result does not exist");
        assertEquals(true, ticket_result instanceof Result, "Ticket future result is not a TicketResult object");

    }

    @Test
    public void test_queueIntegrity() {
        
        // This test is to ensure that queues correctly poll correctly and maintain queue sizing
        // Note: 
        //  - This test assumes an empty queue when it executes! If the queue is not empty it will fail!
        //  - This test was written in a sleep deprived state a may not accurately test for queue integrity failure!
        
        // create ticket arguments
        String ticket_type = "test_action";
        TicketPriority priority = TicketPriority.NORMAL;
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put("test_customer_parameter_1", true);
        parameters.put("test_customer_parameter_2", 0);
        parameters.put("test_customer_parameter_3", "This is the third parameter for our ticket");

        int num_test_tickets = 5;

        List<Ticket> tickets = new ArrayList<>();
        List<CompletableFuture<Result>> futures = new ArrayList<>();
        AtomicInteger completedCount = new AtomicInteger(0);

        // this loop queues puppets and ensures the queue size increases accordingly
        for (int i=0; i<=num_test_tickets-1;i++) {
            Ticket ticket = Puppeteer.createTicket(ticket_type, priority, parameters);
            tickets.add(ticket);
            futures.add(ticket.getFuture());
            Puppeteer.queueTicket(ticket);   
            int expected = Puppeteer.getQueue(ticket_type).size();
            assertEquals(expected, i); // 0 on first loop works because the first ticket is immediately polled from the queue
        }
        
        // this loop ensures that the queue decreases when a ticket ends (if it doesn't then we trigger a flag) 
        //   - This flag based implementation is pretty bruteforce and should
        //      be refactored such that we immediately fail the test when a mismatch is detected!
        //   - Additionally since a queue integrity failure would cause our futures 
        //      to never complete we need a timeout on the test
        //     (we could have puppets be able to kill themselves or 
        //     ticketmanager kills a puppet [heartbeat style implementatino])
        AtomicBoolean flag = new AtomicBoolean(false); 
        for (int i=0; i<=tickets.size()-1;i++) {
            // wait for tickets to complete and ensure the queue is the size we expect
            futures.get(i).thenRun(() -> {
                int actual = Puppeteer.getQueue(ticket_type).size();
                int totalCompleted = completedCount.incrementAndGet();
                int expected = futures.size()-totalCompleted;
                if (expected!=actual) { flag.set(true); }
            });
        }
        assertTimeoutPreemptively(
            Duration.ofMillis(futures.size()*300), // 100ms overhead for each future
            () -> {
                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
            },
            "Queued tickets took to long to execute (likely means that we failed to execute all tickets!)\nEnsure that ticket queues are not being cleared and all ticket futures complete eventually!\n"
        );
        assertEquals(false, flag.get(), "Queue count mismatch during processing!");
    }

}
