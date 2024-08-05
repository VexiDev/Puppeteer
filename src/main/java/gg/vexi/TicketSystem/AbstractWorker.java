package gg.vexi.TicketSystem;

import java.util.concurrent.CompletableFuture;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketResult;

public abstract class AbstractWorker<T> {

    Status status = Status.CREATED;
    final CompletableFuture<TicketResult<T>> future;
    final Ticket associated_ticket;

    public AbstractWorker(Ticket ticket) {
        
        future = new CompletableFuture<>();
        associated_ticket = ticket;
        status = Status.READY;

    }

    // getters
    public Status getStatus() { return status; }
    public CompletableFuture<TicketResult<T>> getFuture() { return future; }
    public Ticket getTicket() { return associated_ticket; }

    // setters
    public void setStatus(Status new_status) { status = new_status; }

    public abstract void start();

    public final void completeSuccess(T data) {

        TicketResult<T> result = new TicketResult<>(new CaughtExceptions(), associated_ticket, Status.SUCCESS, data);
        future.complete(result);
        
    }

}