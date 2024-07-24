package gg.vexi.TicketSystem.Factory;

import java.util.Map;
import java.util.UUID;

import gg.vexi.TicketSystem.Enums.ActionType;
import gg.vexi.TicketSystem.Tickets.Ticket;

public class TicketFactory {
    public static Ticket createTicket(UUID customerId, ActionType actionType, Map<String, Object> parameters) {
        Ticket ticket = new Ticket(customerId, actionType, parameters);
        
        // Set initial priority based on action type or other factors
        if (actionType == ActionType.WORLD_CREATION) {
            ticket.setPriority(10); // Higher priority for world creation
        } else {
            ticket.setPriority(0); // Default priority
        }
        
        return ticket;
    }
}