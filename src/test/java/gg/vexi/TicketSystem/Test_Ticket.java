package gg.vexi.TicketSystem;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gg.vexi.TicketSystem.Ticket.Ticket;

public class Test_Ticket {

    Ticket Ticket;

    @BeforeEach
    public void setup() {
        Ticket = new Ticket();
    }

    @Test
    public void test_init() {
        assertNotNull(Ticket, "Ticket is Null");
        assertNotNull(Ticket.getType(), "Ticket has no action type");
        assertEquals(true, Ticket.getType() instanceof ActionType, "Ticket does not have a valid action type");
    }
    
}