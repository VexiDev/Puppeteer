package gg.vexi.TicketSystem;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.TicketManager;
import gg.vexi.TicketSystem.Ticket.Ticket;

public class Test_TicketManager {

    @Test
    public void Test_init() {
        TicketManager TicketManager = new TicketManager(); 
        assertNotNull(TicketManager, "TicketManager is Null");
    }

    @Test
    public void Test_ScheduleTicket() {

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

}
