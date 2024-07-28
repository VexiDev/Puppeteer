package gg.vexi.TicketSystem;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import static gg.vexi.TicketSystem.TestUtils.assertJsonObjectEquals;
import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketPriority;

public class Test_Ticket {

    Ticket Ticket;

    @BeforeEach
    public void setup() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("setting_example", true);
        Ticket = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, parameters, new CompletableFuture<>());
    }

    @Test
    public void test_init() {
        // check if ticket is initialized correctly
        
        // create expected objects
        JsonObject expected_parameters = new JsonObject();
        expected_parameters.addProperty("setting_example", true);


        // verify ticket exists
        assertNotNull(Ticket, "Ticket is Null");

        // verify id exists
        assertNotNull(Ticket.getId(), "Ticket ID is Null");
        
        // verify type
        assertNotNull(Ticket.getType(), "Ticket has no action type");
        assertEquals(true, Ticket.getType() instanceof ActionType, "Ticket does not have a valid action type");

        // verify priority
        assertNotNull(Ticket.getPriority(), "Ticket has no priority level");
        assertEquals(true, Ticket.getPriority() instanceof TicketPriority, "Ticket does not have a valid priority level");

        // verify parameters
        assertNotNull(Ticket.getParameters(), "Ticket has no action type");
        assertJsonObjectEquals(expected_parameters, Ticket.getParameters());

        // verify future
        assertNotNull(Ticket.getFuture(), "Ticket has no future");
        assertEquals(true, Ticket.getFuture() instanceof CompletableFuture);
    }
    
}