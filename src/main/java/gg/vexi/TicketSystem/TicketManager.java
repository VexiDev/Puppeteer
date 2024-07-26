package gg.vexi.TicketSystem;

import java.util.concurrent.ConcurrentLinkedQueue;

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

    private void executeTicket() {}

    public ConcurrentLinkedQueue<Ticket> getQueue() { return Queue; }

    public Ticket getActive() { return activeTicket; }

    public void setActive(Ticket ticket) { activeTicket = ticket; }

}
