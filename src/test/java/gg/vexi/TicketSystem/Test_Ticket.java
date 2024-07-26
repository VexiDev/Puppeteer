package gg.vexi.TicketSystem;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import gg.vexi.TicketSystem.Ticket.Ticket;

public class Test_Ticket {

    @Test
    public void test_init() {
        Ticket Ticket = new Ticket();
        assertNotNull(Ticket, "Ticket is Null");
    }

}
