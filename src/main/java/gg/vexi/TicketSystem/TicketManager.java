package gg.vexi.TicketSystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;

public class TicketManager {

    private final ConcurrentLinkedQueue<Ticket> Queue = new ConcurrentLinkedQueue<>();
    private Ticket activeTicket;
    
    public TicketManager() {}
    
    public boolean scheduleTicket(Ticket Ticket) {

        Queue.add(Ticket);

        return true;
    }
    
    protected Ticket nextTicket() { 
        if (getActive() == null) {
            return Queue.poll();
        } else {
            return null;
        }
    
    }

    protected void executeTicket(Ticket ticket) {
        activeTicket = ticket;
    }

    public ConcurrentLinkedQueue<Ticket> getQueue() { return Queue; }

    public ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> getAllQueues() { 
        ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> map = new ConcurrentHashMap<>(); 
        
        for (ActionType type : ActionType.values()) {
            map.put(type, new ConcurrentLinkedQueue<>());
        }
        
        return map;
    }

    public Ticket getActive() { return activeTicket; }

    public void setActive(Ticket ticket) { activeTicket = ticket; }

}
