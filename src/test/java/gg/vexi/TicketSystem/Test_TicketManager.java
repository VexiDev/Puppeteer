package gg.vexi.TicketSystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;

public class Test_TicketManager {

    private TicketManager TicketManager;

    @BeforeEach
    public void setup() {
        TicketManager = new TicketManager();
    }

    @Test
    public void test_init() {
        // make sure ticketmanager actually initialized
        assertNotNull(TicketManager, "TicketManager is Null");

        // we should have a queue for each action type
        ConcurrentHashMap<ActionType, ConcurrentLinkedQueue> expected_queues = new ConcurrentHashMap<>();
        for (ActionType type : ActionType.values()) {
            expected_queues.put(type, new ConcurrentLinkedQueue());
        }

        ConcurrentHashMap<ActionType, ConcurrentLinkedQueue> actual_queues = TicketManager.getAllQueues();

        assertEquals(expected_queues.size(), actual_queues.size(), "TicketManager does not have a concurrent queue for each actiontype");
        assertEquals(expected_queues, actual_queues, "TicketManager does not have a concurrent queue for each actiontype");
    }

    @Test
    public void test_ScheduleTicket() {

        Ticket Ticket = new Ticket(ActionType.ACTION);

        // build expected queue object
        ConcurrentLinkedQueue<Ticket> expected_q = new ConcurrentLinkedQueue<>();
        expected_q.add(Ticket);

        // schedule a ticket
        TicketManager.scheduleTicket(Ticket);
        // get queue length
        ConcurrentLinkedQueue<Ticket> actual_q = TicketManager.getQueue();

        // check queue length
        assertEquals(expected_q.size(), actual_q.size(), "Queue sizes do not match");
        // compare elements in the queue
        assertArrayEquals(expected_q.toArray(), actual_q.toArray(), "Queue elements do not match");

    }

    @Test
    public void test_NextTicket() {
        // Schedule 2 tickets
        Ticket ticket1 = new Ticket(ActionType.ACTION);
        Ticket ticket2 = new Ticket(ActionType.ACTION);
        TicketManager.scheduleTicket(ticket1);
        TicketManager.scheduleTicket(ticket2);

        // Check that the nextTicket method returns the first scheduled ticket
        Ticket nextTicket = TicketManager.nextTicket();
        assertNotNull(nextTicket, "Expected nextTicket to return a non-null ticket, but it returned null.");
        assertEquals(ticket1, nextTicket, "Expected the first scheduled ticket to be returned.");

        // Set the first ticket to active
        TicketManager.setActive(nextTicket);

        // Try and get the next ticket, expect null because a ticket is active
        nextTicket = TicketManager.nextTicket();
        assertEquals(null, nextTicket, "Expected nextTicket to return null when a ticket is active, but it returned a ticket.");

    }

    @Test
    public void test_executeTicket() {
        Ticket ticket1 = new Ticket(ActionType.ACTION);
        TicketManager.scheduleTicket(ticket1);
        Ticket nextTicket = TicketManager.nextTicket();

        TicketManager.executeTicket(nextTicket);

        assertEquals(nextTicket, TicketManager.getActive(), "activeTicket is not the ticket passed to executeTicket");

    }

}
