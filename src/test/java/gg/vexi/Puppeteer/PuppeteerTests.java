package gg.vexi.Puppeteer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.ResultStatus;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ExamplePuppets.ExamplePuppet_String;
import gg.vexi.Puppeteer.Ticket.TicketPriority;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class _Puppeteer {

    private Puppeteer puppeteer;
    private Registry registry;
    private AtomicInteger state;
    private Map<String, Puppet<?>> activePuppets;
    private Map<String, PriorityBlockingQueue<Ticket<?>>> ticketQueues;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        puppeteer = new Puppeteer();

        puppeteer.registerPuppet("test_action", ExamplePuppet_String.class);

        // get the registry using reflection
        Field registry_field = puppeteer.getClass().getDeclaredField("registry");
        registry_field.setAccessible(true);
        registry = (Registry) registry_field.get(puppeteer);

        // get state using reflection
        Field state_field = puppeteer.getClass().getDeclaredField("state");
        state_field.setAccessible(true);
        state = (AtomicInteger) state_field.get(puppeteer);

        // get tickets queue using reflection
        Field ticket_queue_field = puppeteer.getClass().getDeclaredField("ticketQueues");
        ticket_queue_field.setAccessible(true);
        ticketQueues = (Map<String, PriorityBlockingQueue<Ticket<?>>>) ticket_queue_field.get(puppeteer);

        // get active puppet map using reflection
        Field active_map_field = puppeteer.getClass().getDeclaredField("activePuppets");
        active_map_field.setAccessible(true);
        activePuppets = (Map<String, Puppet<?>>) active_map_field.get(puppeteer);
    }

    @Test
    public void test_init() {
        // check if puppeteer actually initialized correctly

        // build expected objects
        Map<String, PriorityBlockingQueue<Ticket<String>>> expected_queues = new ConcurrentHashMap<>();
        for ( String key : registry.all().keySet() ) { expected_queues.put(key, new PriorityBlockingQueue<>()); }

        // verify puppeteer exists
        assertNotNull(puppeteer, "puppeteer is Null");

        // get actual active map
        Map<String, Ticket<?>> actual_active_map = puppeteer.getAllActive();

        // verify active tickets map exists
        assertNotNull(actual_active_map, "Actual active map is Null");
        // verify it has no entries
        assertEquals(new ConcurrentHashMap<>().size(), actual_active_map.size());

        // get actual puppeteer queues
        Map<String, PriorityBlockingQueue<Ticket<?>>> actual_queues = puppeteer.getAllQueues();

        // verify we have all queues (we should have a queue for each action type)
        assertEquals(
            expected_queues.size(), actual_queues.size(), "puppeteer does not have a concurrent queue for each puppet"
        );
        // verify puppeteer queue map contense against expected contense
        for ( Map.Entry<String, PriorityBlockingQueue<Ticket<String>>> entry : expected_queues.entrySet() ) {
            // verify the puppeteer queue map contains expected key
            assertTrue(actual_queues.containsKey(entry.getKey()), "Missing queue for action type: " + entry.getKey());
            // verify the queue is of the correct length (0)
            assertEquals(
                entry.getValue().size(),
                actual_queues.get(entry.getKey()).size(),
                "Queue size mismatch for action type: " + entry.getKey()
            );
        }
    }

    @Test
    public void test_addTicketToQueue() {

        Ticket<String> Ticket =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());

        // build expected queue object
        PriorityBlockingQueue<Ticket<String>> expected_q = new PriorityBlockingQueue<>();
        expected_q.offer(Ticket);

        // add ticket to that ticket's action queue
        puppeteer.addTicketToQueue(Ticket);

        // get queue length
        PriorityBlockingQueue<Ticket<?>> actual_q = puppeteer.getQueue("test_action");

        // check queue length
        assertEquals(expected_q.size(), actual_q.size(), "Queue sizes do not match");
        // compare elements in the queue
        assertArrayEquals(expected_q.toArray(), actual_q.toArray(), "Queue elements do not match");
    }

    @Test
    public void test_NextTicket()
        throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {
        // create necessary objects
        Ticket<String> ticket1 =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> ticket2 =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());

        // verify tickets exist
        assertNotNull(ticket1, "puppeteer returned null value for queueTicket() [ticket1]");
        assertNotNull(ticket2, "puppeteer returned null value for queueTicket() [ticket2]");

        // attempt to get next ticket before adding any tickets (will always null)
        Ticket<?> nextTicket = puppeteer.nextTicket("test_action");

        assertEquals(
            null,
            nextTicket,
            "nextTicket returned a not null value before we added any tickets to the queue: " + nextTicket
        );

        // Simulate the queue if we had 2 tickets in in the queue (actionQueues reflected from puppeteer in setup() )
        ticketQueues.get("test_action").offer(ticket1);
        ticketQueues.get("test_action").offer(ticket2);

        // Check that the nextTicket method returns the first scheduled ticket for that action type
        nextTicket = puppeteer.nextTicket("test_action");

        assertNotNull(nextTicket, "Expected nextTicket to return a non-null ticket, but it returned null.");
        assertEquals(ticket1, nextTicket, "Expected the first scheduled ticket to be returned.");

        // Set the first ticket's puppet to active (aka simulate what executeTucket would do)
        activePuppets.putIfAbsent(nextTicket.puppet(), registry.retreive(nextTicket));

        // verify the actions_queue for our ticket is not empty
        assertFalse(
            ticketQueues.get("test_action").isEmpty(), "Queue for test_action is empty after polling first ticket"
        );

        // Try and get the next ticket
        nextTicket = puppeteer.nextTicket("test_action");
        assertEquals(
            null,
            nextTicket,
            "Expected nextTicket to return null when an associated puppet is active, but it returned a ticket."
        );
    }

    @Test
    public void test_executeTicket() {
        // create ticket object
        Ticket<String> ticket1 =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());

        // verify ticket isn't null
        assertNotNull(ticket1, "Ticket is null");

        // execute the ticket
        puppeteer.executeTicket(ticket1);

        // verify the ticket was set to active ticket
        assertEquals(
            ticket1, puppeteer.getActive("test_action"), "activeTicket is not the ticket passed to executeTicket"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_TicketPriorityOrdering()
        throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {

        // created test tickets
        Ticket<String> normal_ticket1 =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> normal_ticket2 =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> elevated_ticket1 =
            new Ticket<>("test_action", TicketPriority.ELEVATED, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> high_ticket1 =
            new Ticket<>("test_action", TicketPriority.HIGH, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> high_ticket2 =
            new Ticket<>("test_action", TicketPriority.HIGH, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> high_ticket3 =
            new Ticket<>("test_action", TicketPriority.HIGH, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> highest_ticket1 =
            new Ticket<>("test_action", TicketPriority.HIGHEST, new ConcurrentHashMap<>(), new CompletableFuture<>());

        // bundle tickets into a list (out of order)
        List<Ticket<String>> ticket_list = List.of(
            high_ticket2, normal_ticket1, elevated_ticket1, high_ticket1, normal_ticket2, high_ticket3, highest_ticket1
        );

        // add all tickets to their respective queue (all the same queue for now)
        for ( Ticket<String> ticket : ticket_list ) { ticketQueues.get(ticket.puppet()).offer(ticket); }
        // verify that tickets are in the correct order
        List<TicketPriority> expected_order =
            List.of(TicketPriority.HIGHEST, TicketPriority.HIGH, TicketPriority.ELEVATED, TicketPriority.NORMAL);
        TicketPriority lastPriority = null;
        TicketPriority currentPriority;
        int lastPriorityIndex = -1;
        Ticket<String> current_ticket;

        while ( (current_ticket = (Ticket<String>) ticketQueues.get("test_action").poll()) != null ) {
            currentPriority = current_ticket.priority();
            int currentPriorityIndex = expected_order.indexOf(currentPriority);

            assertTrue(currentPriorityIndex != -1, "Unexpected priority found: " + currentPriority);

            if ( lastPriority != null ) {
                assertTrue(
                    currentPriorityIndex >= lastPriorityIndex,
                    "Priority out of order. Found " + currentPriority + " after " + lastPriority
                );
            }

            lastPriority = currentPriority;
            lastPriorityIndex = currentPriorityIndex;
        }
    }

    @Test
    public void testIsPerforming() {

        // created test tickets
        Ticket<String> normal_ticket1 =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> normal_ticket2 =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> elevated_ticket1 =
            new Ticket<>("test_action", TicketPriority.ELEVATED, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> high_ticket1 =
            new Ticket<>("test_action", TicketPriority.HIGH, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> high_ticket2 =
            new Ticket<>("test_action", TicketPriority.HIGH, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> high_ticket3 =
            new Ticket<>("test_action", TicketPriority.HIGH, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> highest_ticket1 =
            new Ticket<>("test_action", TicketPriority.HIGHEST, new ConcurrentHashMap<>(), new CompletableFuture<>());

        //<String> bundle tickets into a list
        List<Ticket<String>> ticket_list = List.of(
            high_ticket2, normal_ticket1, elevated_ticket1, high_ticket1, normal_ticket2, high_ticket3, highest_ticket1
        );

        // queue all the tickets
        for ( Ticket<String> ticket : ticket_list ) { puppeteer.queueTicket(ticket); }

        // since "test_action" is just a 200ms timer puppeteer
        // should have plenty of tickets in queue and one active
        assertTrue(puppeteer.isPerforming(), "Value error, puppeteer should be performing");
    }

    @Test
    //TODO: Implement multiple puppets
    //All queues should cancel
    //All active puppets should finish normally
    public void testSoftShutdown() {

        // created test tickets
        Ticket<String> normal_ticket1 =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> normal_ticket2 =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> elevated_ticket1 =
            new Ticket<>("test_action", TicketPriority.ELEVATED, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> high_ticket1 =
            new Ticket<>("test_action", TicketPriority.HIGH, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> high_ticket2 =
            new Ticket<>("test_action", TicketPriority.HIGH, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> high_ticket3 =
            new Ticket<>("test_action", TicketPriority.HIGH, new ConcurrentHashMap<>(), new CompletableFuture<>());
        Ticket<String> highest_ticket1 =
            new Ticket<>("test_action", TicketPriority.HIGHEST, new ConcurrentHashMap<>(), new CompletableFuture<>());

        //<String> bundle tickets into a list
        List<Ticket<String>> ticket_list = List.of(
            highest_ticket1, // runs first
            high_ticket1,
            high_ticket2,
            high_ticket3,
            elevated_ticket1,
            normal_ticket1,
            normal_ticket2
        );

        // queue all the tickets
        for ( Ticket<String> ticket : ticket_list ) { puppeteer.queueTicket(ticket); }

        // get active ticket
        Ticket<?> active = puppeteer.getActive("test_action");

        puppeteer.shutdown();

        // assert all queued tickets are canceled
        PriorityBlockingQueue<Ticket<?>> q = puppeteer.getQueue("test_action");
        assertTrue(q.isEmpty(), "Ticket queue was not empty after shutdown");

        // skip first ticket in ticket_list since it is our "active" puppet
        ticket_list.stream().skip(1).forEach(t -> {
            assertTrue(t.future().isDone(), "Queued ticket futures were not completed");
            assertEquals(ResultStatus.CANCELED, t.future().join().status(), "ticket future result was not CANCELED");
        });

        // assert the active ticket completes successfully
        assertTrue(active.future().join().isSuccessful(), "Active ticket did not complete");
    }

    @Test
    public void testHardShutdown() {
        //TODO: Implement hard shutdown test
        //This should cancel all queued and interrupt all active
    }

    @Test
    public void testStateHandling() {

        // trying to queue a ticket when in these states should throw an exception
        // 0 = SHUTDOWN
        // 1 = SHUTTING_DOWN
        // 2 = CLOSED

        for ( int i = 0; i <= 2; i++ ) {
            state.set(i);

            int flag = 0;
            try {
                Ticket<String> t = puppeteer.queueTicket("test_action");
            } catch ( IllegalStateException e ) { flag = 1; }

            assertEquals(1, flag, "queueTicket did not throw an exception for state " + i);
        }
    }
}
