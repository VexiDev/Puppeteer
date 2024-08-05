package gg.vexi.TicketSystem;

import java.util.concurrent.CompletableFuture;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
import gg.vexi.TicketSystem.Exceptions.ExceptionRecord;
import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketResult;

public abstract class AbstractWorker<T> {

    Status status = Status.CREATED;
    final CompletableFuture<TicketResult<T>> future;
    final Ticket associated_ticket;
    final CaughtExceptions exceptionHandler;

    public AbstractWorker(Ticket ticket) {

        associated_ticket = ticket;
        exceptionHandler = new CaughtExceptions();
        future = new CompletableFuture<>();
        status = Status.READY;

    }
    
    // entry point to worker
    public abstract void main();


    // start point of worker (run by ticketmanager)
    public final void start() {
        try {
            main();
        } catch (Exception e) {
            recordException(new ExceptionRecord("Unhandled Exception", e.getMessage()));
            complete(Status.FAILED, null);
        }
    }

    public final void recordException(ExceptionRecord record) { exceptionHandler.add(record); }

    // exit point of worker
    protected void complete(Status result_status, T data) {
        TicketResult<T> result = new TicketResult<>(new CaughtExceptions(), associated_ticket, result_status, data);
        future.complete(result);
    }

    // getters
    public Status getStatus() {
        return status;
    }

    public CompletableFuture<TicketResult<T>> getFuture() {
        return future;
    }

    public Ticket getTicket() {
        return associated_ticket;
    }

    // setters
    public void setStatus(Status new_status) {
        status = new_status;
    }

}
