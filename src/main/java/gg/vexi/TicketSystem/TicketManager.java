package gg.vexi.TicketSystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;

public class TicketManager {

    private final ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> actionQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ActionType, Ticket> activeTickets = new ConcurrentHashMap<>();
    
    public TicketManager() {
        for (ActionType type : ActionType.values()) {
            actionQueues.put(type, new ConcurrentLinkedQueue<>());
        }
    }
    
    public boolean scheduleTicket(ActionType TicketType, Ticket Ticket) {
        
        actionQueues.get(TicketType).add(Ticket);

        return true;
    }
    
    protected Ticket nextTicket(ActionType type) { 
        if (getActive(type) == null) {
            return actionQueues.get(type).poll();
        } else {
            return null;
        }
    
    }

    protected void executeTicket(Ticket ticket) {
        activeTickets.put(ticket.getType(), ticket);
    }

    public ConcurrentLinkedQueue<Ticket> getQueue(ActionType type) { return actionQueues.get(type); }

    public ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> getAllQueues() { return actionQueues; }

    public Ticket getActive(ActionType type) { return activeTickets.get(type); }

    public void setActive(Ticket ticket) { activeTickets.put(ticket.getType(), ticket); }

}
