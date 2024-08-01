package gg.vexi.TicketSystem;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketPriority;

public class Test_TicketManager {

    private TicketManager TicketManager;

    @BeforeEach
    public void setup() {
        TicketManager = new TicketManager();
    }

    @Test
    public void test_init() {
        // check if ticketmanager actually initialized correctly 

        // build expected objects
        ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> expected_queues = new ConcurrentHashMap<>();
        for (ActionType type : ActionType.values()) {
            expected_queues.put(type, new ConcurrentLinkedQueue<>());
        }

        // verify ticketmanager exists
        assertNotNull(TicketManager, "TicketManager is Null");

        // get actual active map 
        ConcurrentHashMap<ActionType, Ticket> actual_active_map = TicketManager.getAllActive();

        // verify active tickets map exists
        assertNotNull(actual_active_map, "Actual active map is Null");
        // verify it has no entries
        assertEquals(new ConcurrentHashMap<>().size(), actual_active_map.size());

        // get actual ticketmanager queues
        ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> actual_queues = TicketManager.getAllQueues();

        // verify we have all queues (we should have a queue for each action type)
        assertEquals(expected_queues.size(), actual_queues.size(), "TicketManager does not have a concurrent queue for each actiontype");
        // verify ticketmanager queue map contense against expected contense
        for (Map.Entry<ActionType, ConcurrentLinkedQueue<Ticket>> entry : expected_queues.entrySet()) {
            // verify the ticketmanager queue map contains expected key
            assertTrue(actual_queues.containsKey(entry.getKey()), "Missing queue for action type: " + entry.getKey());
            // verify the queue is of the correct length (0)
            assertEquals(entry.getValue().size(), actual_queues.get(entry.getKey()).size(), "Queue size mismatch for action type: " + entry.getKey());
        }

    }

    @Test
    public void test_addTicketToQueue() {

        Ticket Ticket = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());

        // build expected queue object
        ConcurrentLinkedQueue<Ticket> expected_q = new ConcurrentLinkedQueue<>();
        expected_q.add(Ticket);

        // add ticket to that ticket's action queue
        TicketManager.addTicketToQueue(Ticket);

        // get queue length
        ConcurrentLinkedQueue<Ticket> actual_q = TicketManager.getQueue(ActionType.ACTION);

        // check queue length
        assertEquals(expected_q.size(), actual_q.size(), "Queue sizes do not match");
        // compare elements in the queue
        assertArrayEquals(expected_q.toArray(), actual_q.toArray(), "Queue elements do not match");

    }

    @Test
    public void test_NextTicket() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {
        // create necessary objects
        Ticket ticket1 = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
        Ticket ticket2 = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());

        // verify tickets exist
        assertNotNull(ticket1, "TicketManager returned null value for queueTicket() [ticket1]");
        assertNotNull(ticket2, "TicketManager returned null value for queueTicket() [ticket2]");

        // get actions_queue from ticketmanager using reflection
        Field actions_queue_field = TicketManager.getClass().getDeclaredField("actionQueues");
        actions_queue_field.setAccessible(true);
        ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> actions_queue
                = (ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>>) actions_queue_field.get(TicketManager);

        // Simulate the queue if we had 2 tickets in in the queue  
        actions_queue.get(ticket1.getType()).add(ticket1);

        // Check that the nextTicket method returns the first scheduled ticket for that action type
        Ticket nextTicket = TicketManager.nextTicket(ActionType.ACTION);

        assertNotNull(nextTicket, "Expected nextTicket to return a non-null ticket, but it returned null.");
        assertEquals(ticket1, nextTicket, "Expected the first scheduled ticket to be returned.");

        // Set the first ticket to active (aka simulate what executeTucket would do)
        TicketManager.setActive(nextTicket);

        // Try and get the next ticket, expect null because a ticket of that action type is already active
        nextTicket = TicketManager.nextTicket(ActionType.ACTION);
        assertEquals(null, nextTicket, "Expected nextTicket to return null when a ticket is active, but it returned a ticket.");

    }

    @Test
    public void test_executeTicket() {
        // create ticket object
        Ticket ticket1 = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());

        // verify ticket isn't null
        assertNotNull(ticket1, "Ticket is null");

        // execute the ticket
        TicketManager.executeTicket(ticket1);

        // verify the ticket was set to active ticket
        assertEquals(ticket1, TicketManager.getActive(ActionType.ACTION), "activeTicket is not the ticket passed to executeTicket");

    }

    @Test
    public void test_ticketPriorityOrdering() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {

        // get actions_queue from ticketmanager using reflection
        Field actions_queue_field = TicketManager.getClass().getDeclaredField("actionQueues");
        actions_queue_field.setAccessible(true);
        ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> actions_queue
                = (ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>>) actions_queue_field.get(TicketManager);

        // created test tickets
        Ticket normal_ticket1 = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
        Ticket normal_ticket2 = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
        Ticket elevated_ticket1 = new Ticket(ActionType.ACTION, TicketPriority.ELEVATED, new JsonObject(), new CompletableFuture<>());
        Ticket high_ticket1 = new Ticket(ActionType.ACTION, TicketPriority.HIGH, new JsonObject(), new CompletableFuture<>());
        Ticket high_ticket2 = new Ticket(ActionType.ACTION, TicketPriority.HIGH, new JsonObject(), new CompletableFuture<>());
        Ticket high_ticket3 = new Ticket(ActionType.ACTION, TicketPriority.HIGH, new JsonObject(), new CompletableFuture<>());
        Ticket system_ticket1 = new Ticket(ActionType.ACTION, TicketPriority.SYSTEM, new JsonObject(), new CompletableFuture<>());

        // bundle tickets into a list (out of order)
        List<Ticket> ticket_list = List.of(
                high_ticket2,
                normal_ticket1,
                elevated_ticket1,
                high_ticket1,
                normal_ticket2,
                high_ticket3,
                system_ticket1
        );

        // add all tickets to their respective queue (all the same queue for now)
        for (Ticket ticket : ticket_list) {
            actions_queue.get(ticket.getType()).offer(ticket);
        }
        // verify that tickets are in the correct order
        List<TicketPriority> expected_order = List.of(TicketPriority.SYSTEM, TicketPriority.HIGH, TicketPriority.ELEVATED, TicketPriority.NORMAL);
        TicketPriority lastPriority = null;
        TicketPriority currentPriority;
        int lastPriorityIndex = -1;
        Ticket current_ticket;

        while ((current_ticket = actions_queue.get(ActionType.ACTION).poll()) != null) {
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
