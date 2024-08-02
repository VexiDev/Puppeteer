package gg.vexi.TicketSystem.Worker;

import java.util.concurrent.CompletableFuture;

import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketResult;

public abstract class AbstractWorker {

    WorkerStatus status = WorkerStatus.READY;
    final CompletableFuture<TicketResult> future;
    final Ticket associated_ticket;

    public AbstractWorker(Ticket ticket) {
        
        future = new CompletableFuture<>();
        associated_ticket = ticket;

    }

    // getters
    public WorkerStatus getStatus() { return status; }
    public CompletableFuture<TicketResult> getFuture() { return future; }
    public Ticket getTicket() { return associated_ticket; }

    // setters
    public void setStatus(WorkerStatus new_status) { status = new_status; }

    public abstract void start();

}