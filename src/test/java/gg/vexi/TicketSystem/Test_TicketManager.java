package gg.vexi.TicketSystem;

import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import gg.vexi.TicketSystem.Ticket.Ticket;

public class Test_TicketManager {

    @Test
    public void test_init() {
        TicketManager TicketManager = new TicketManager(); 
        assertNotNull(TicketManager, "TicketManager is Null");
    }

    @Test
    public void test_ScheduleTicket() {

        TicketManager TicketManager = new TicketManager(); 
        Ticket Ticket = new Ticket();

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
    public void test_NextTicket () {

        TicketManager TicketManager = new TicketManager();
        
        // schedule 2 tickets
        Ticket ticket_1 = new Ticket();
        Ticket ticket_2 = new Ticket();

        TicketManager.scheduleTicket(ticket_1);
        TicketManager.scheduleTicket(ticket_2);

        // Next ticket should poll the queue
        Ticket NextTicket = TicketManager.nextTicket();

        assertNotNull(NextTicket, "Ticket is Null");
        assertEquals(ticket_1, NextTicket, "Polled ticket is not the same as first scheduled ticket");

    }

    @Test
    public void Test_executeTicket() {

    }

}
