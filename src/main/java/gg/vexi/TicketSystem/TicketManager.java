package gg.vexi.TicketSystem;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketResult;

public class TicketManager {

    private ConcurrentLinkedQueue<Ticket> Queue = new ConcurrentLinkedQueue<>();
    
    public TicketManager() {}
    
    public boolean scheduleTicket(Ticket Ticket) {

        Queue.add(Ticket);

        return true;
    }
    
    private void nextTicket() {}

    private void executeTicket() {}

    public ConcurrentLinkedQueue<Ticket> getQueue() { return Queue; }

}
