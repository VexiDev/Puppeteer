package gg.vexi.TicketSystem;

import java.util.concurrent.ConcurrentLinkedQueue;

import gg.vexi.TicketSystem.Ticket.Ticket;

public class TicketManager {

    private final ConcurrentLinkedQueue<Ticket> Queue = new ConcurrentLinkedQueue<>();
    
    public TicketManager() {}
    
    public boolean scheduleTicket(Ticket Ticket) {

        Queue.add(Ticket);

        return true;
    }
    
    protected Ticket nextTicket() { return Queue.poll(); }

    private void executeTicket() {}

    public ConcurrentLinkedQueue<Ticket> getQueue() { return Queue; }

    public Ticket getActive() { return new Ticket(); }

    public void setActive(Ticket ticket) {}

}
