package gg.vexi.Puppeteer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Ticket.TicketPriority;

class _Puppeteer {

    private Puppeteer Puppeteer;
    private PuppetRegistry puppetRegistry;
    private Map<String, PriorityBlockingQueue<Ticket>> actionQueues;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Puppeteer = new Puppeteer("gg.vexi");

        // get the registry using reflection
        Field registry_field = Puppeteer.getClass().getDeclaredField("puppetRegistry");
        registry_field.setAccessible(true);
        puppetRegistry = (PuppetRegistry) registry_field.get(Puppeteer);

        // get actions_queue using reflection
        Field actions_queue_field = Puppeteer.getClass().getDeclaredField("actionQueues");
        actions_queue_field.setAccessible(true);
        actionQueues = (Map<String, PriorityBlockingQueue<Ticket>>) actions_queue_field.get(Puppeteer);

    }

    @Test
    public void test_init() {
        // check if puppeteer actually initialized correctly 

        // build expected objects
        Map<String, PriorityBlockingQueue<Ticket>> expected_queues = new ConcurrentHashMap<>();
        for (String key : puppetRegistry.getFullRegistry().keySet()) {
            expected_queues.put(key, new PriorityBlockingQueue<>());
        }

        // verify puppeteer exists
        assertNotNull(Puppeteer, "puppeteer is Null");

        // get actual active map 
        Map<String, Ticket> actual_active_map = Puppeteer.getAllActive();


        // verify active tickets map exists
        assertNotNull(actual_active_map, "Actual active map is Null");
        // verify it has no entries
        assertEquals(new ConcurrentHashMap<>().size(), actual_active_map.size());
        
        // get actual puppeteer queues
        Map<String, PriorityBlockingQueue<Ticket>> actual_queues = Puppeteer.getAllQueues();

        // verify we have all queues (we should have a queue for each action type)
        assertEquals(expected_queues.size(), actual_queues.size(), "puppeteer does not have a concurrent queue for each actiontype");
        // verify puppeteer queue map contense against expected contense
        for (Map.Entry<String, PriorityBlockingQueue<Ticket>> entry : expected_queues.entrySet()) {
            // verify the puppeteer queue map contains expected key
            assertTrue(actual_queues.containsKey(entry.getKey()), "Missing queue for action type: " + entry.getKey());
            // verify the queue is of the correct length (0)
            assertEquals(entry.getValue().size(), actual_queues.get(entry.getKey()).size(), "Queue size mismatch for action type: " + entry.getKey());
        }

    }
    
    @Test
    public void test_addTicketToQueue() {

        Ticket Ticket = new Ticket("test_action", TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());

        // build expected queue object
        PriorityBlockingQueue<Ticket> expected_q = new PriorityBlockingQueue<>();
        expected_q.offer(Ticket);

        // add ticket to that ticket's action queue
        Puppeteer.addTicketToQueue(Ticket);

        // get queue length
        PriorityBlockingQueue<Ticket> actual_q = Puppeteer.getQueue("test_action");

        // check queue length
        assertEquals(expected_q.size(), actual_q.size(), "Queue sizes do not match");
        // compare elements in the queue
        assertArrayEquals(expected_q.toArray(), actual_q.toArray(), "Queue elements do not match");

    }

    @Test
    public void test_NextTicket() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {
        // create necessary objects
        Ticket ticket1 = new Ticket("test_action", TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
        Ticket ticket2 = new Ticket("test_action", TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());

        // verify tickets exist
        assertNotNull(ticket1, "puppeteer returned null value for queueTicket() [ticket1]");
        assertNotNull(ticket2, "puppeteer returned null value for queueTicket() [ticket2]");

        // attempt to get next ticket before adding any tickets (will always null)
        Ticket nextTicket = Puppeteer.nextTicket("test_action");
        assertEquals(null, nextTicket, "nextTicket returned a not null value before we added any tickets to the queue: "+nextTicket);

        // Simulate the queue if we had 2 tickets in in the queue (actionQueues reflected from puppeteer in setup() )
        actionQueues.get("test_action").offer(ticket1);
        actionQueues.get("test_action").offer(ticket2);

        // Check that the nextTicket method returns the first scheduled ticket for that action type
        nextTicket = Puppeteer.nextTicket("test_action");

        assertNotNull(nextTicket, "Expected nextTicket to return a non-null ticket, but it returned null.");
        assertEquals(ticket1, nextTicket, "Expected the first scheduled ticket to be returned.");

        // Set the first ticket to active (aka simulate what executeTucket would do)
        Puppeteer.setActive(nextTicket);

        // verify the actions_queue for our ticket is not empty
        assertFalse(actionQueues.get("test_action").isEmpty(), "Queue for ticket2 is empty after polling first ticket");

        // Try and get the next ticket
        nextTicket = Puppeteer.nextTicket("test_action");
        assertEquals(null, nextTicket, "Expected nextTicket to return null when a ticket is active, but it returned a ticket.");

    }

    @Test
    public void test_executeTicket() {
        // create ticket object
        Ticket ticket1 = new Ticket("test_action", TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());

        // verify ticket isn't null
        assertNotNull(ticket1, "Ticket is null");

        // execute the ticket
        Puppeteer.executeTicket(ticket1);

        // verify the ticket was set to active ticket
        assertEquals(ticket1, Puppeteer.getActive("test_action"), "activeTicket is not the ticket passed to executeTicket");

    }

    @Test
    public void test_TicketPriorityOrdering() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {

        // created test tickets
        Ticket normal_ticket1 = new Ticket("test_action", TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
        Ticket normal_ticket2 = new Ticket("test_action", TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
        Ticket elevated_ticket1 = new Ticket("test_action", TicketPriority.ELEVATED, new JsonObject(), new CompletableFuture<>());
        Ticket high_ticket1 = new Ticket("test_action", TicketPriority.HIGH, new JsonObject(), new CompletableFuture<>());
        Ticket high_ticket2 = new Ticket("test_action", TicketPriority.HIGH, new JsonObject(), new CompletableFuture<>());
        Ticket high_ticket3 = new Ticket("test_action", TicketPriority.HIGH, new JsonObject(), new CompletableFuture<>());
        Ticket highest_ticket1 = new Ticket("test_action", TicketPriority.HIGHEST, new JsonObject(), new CompletableFuture<>());

        // bundle tickets into a list (out of order)
        List<Ticket> ticket_list = List.of(
                high_ticket2,
                normal_ticket1,
                elevated_ticket1,
                high_ticket1,
                normal_ticket2,
                high_ticket3,
                highest_ticket1
        );

        // add all tickets to their respective queue (all the same queue for now)
        for (Ticket ticket : ticket_list) {
            actionQueues.get(ticket.getType()).offer(ticket);
        }
        // verify that tickets are in the correct order
        List<TicketPriority> expected_order = List.of(TicketPriority.HIGHEST, TicketPriority.HIGH, TicketPriority.ELEVATED, TicketPriority.NORMAL);
        TicketPriority lastPriority = null;
        TicketPriority currentPriority;
        int lastPriorityIndex = -1;
        Ticket current_ticket;

        while ((current_ticket = actionQueues.get("test_action").poll()) != null) {
            currentPriority = current_ticket.getPriority();
            int currentPriorityIndex = expected_order.indexOf(currentPriority);

            assertTrue(currentPriorityIndex != -1, "Unexpected priority found: " + currentPriority);

            if (lastPriority != null) {
                assertTrue(currentPriorityIndex >= lastPriorityIndex, "Priority out of order. Found " + currentPriority + " after " + lastPriority);
            }

            lastPriority = currentPriority;
            lastPriorityIndex = currentPriorityIndex;
        }
    }

}