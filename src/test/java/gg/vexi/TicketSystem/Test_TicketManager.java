package gg.vexi.TicketSystem;

import java.lang.reflect.Field;
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

        // get all ticketmanager queues
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
        
        // get actions_queue from ticketmanager using reflection
        Field actions_queue_field = TicketManager.getClass().getDeclaredField("actionQueues");
        actions_queue_field.setAccessible(true);
        ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> actions_queue = 
        (ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>>) actions_queue_field.get(TicketManager);

        // Simulate the queue if we had 2 tickets in in the queue  
        actions_queue.get(ticket1.getType()).add(ticket1);

        assertNotNull(ticket1, "TicketManager returned null value for queueTicket() [ticket1]");
        assertNotNull(ticket2, "TicketManager returned null value for queueTicket() [ticket2]");

        // Check that the nextTicket method returns the first scheduled ticket for that action type
        Ticket nextTicket = TicketManager.nextTicket(ActionType.ACTION);

        assertNotNull(nextTicket, "Expected nextTicket to return a non-null ticket, but it returned null.");
        assertEquals(ticket1, nextTicket, "Expected the first scheduled ticket to be returned.");

        // Set the first ticket to active
        TicketManager.setActive(nextTicket);

        // Try and get the next ticket, expect null because a ticket of that action type is already active
        nextTicket = TicketManager.nextTicket(ActionType.ACTION);
        assertEquals(null, nextTicket, "Expected nextTicket to return null when a ticket is active, but it returned a ticket.");

    }

    @Test
    public void test_executeTicket() {
        // Schedule ticket  
        // >>> (OLD METHOD! COULD IMPLEMENT USING OVERLOADING?) <<<
        // Ticket ticket1 = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
        // TicketManager.queueTicket(ticket1.getType(), ticket1);
        Ticket ticket1 = TicketManager.queueTicket(ActionType.ACTION, TicketPriority.NORMAL, new JsonObject());

        assertNotNull(ticket1, "Ticket is null");

        Ticket nextTicket = TicketManager.nextTicket(ActionType.ACTION);

        TicketManager.executeTicket(nextTicket);

        assertEquals(nextTicket, TicketManager.getActive(ActionType.ACTION), "activeTicket is not the ticket passed to executeTicket");

    }

}
