package gg.vexi.Puppeteer.Core;

import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.Exceptions.CaughtExceptions;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.State;
import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.Ticket.TicketResult;

public abstract class AbstractPuppet {

    private State status = State.CREATED;
    private final CompletableFuture<TicketResult> future;
    private final Ticket associated_ticket;
    private final CaughtExceptions exceptionHandler;
    protected final JsonObject ticket_parameters;

    public AbstractPuppet(Ticket ticket) {

        associated_ticket = ticket;
        exceptionHandler = new CaughtExceptions();
        future = new CompletableFuture<>();
        ticket_parameters = ticket.getParameters();
        status = State.READY;

    }
    
    // entry point to puppet
    public abstract void main();


    // start point of puppet (run by puppeteer)
    public final void start() {
        try {
            setStatus(State.PROCESSING);
            main();
        } catch (Exception e) {
            recordException(new ExceptionRecord("Unhandled Exception", e.getMessage()));
            complete(Status.FAILED, null);
        }
    }

    public final void recordException(ExceptionRecord record) { exceptionHandler.add(record); }
    public final void recordException(Exception e) {
        exceptionHandler.add(new ExceptionRecord(e.getClass().getName(), e.getMessage()));
    }

    // exit point of puppet
    protected void complete(Status result_status) { complete(result_status, null); }
    protected void complete(Status result_status, Object data) {
        status = State.COMPLETED;
        TicketResult result = new TicketResult(exceptionHandler, associated_ticket, result_status, data);
        future.complete(result);
    }

    // getters
    public State getStatus() {
        return status;
    }

    public CompletableFuture<TicketResult> getFuture() {
        return future;
    }

    public Ticket getTicket() {
        return associated_ticket;
    }

    // setters
    public void setStatus(State new_status) {
        status = new_status;
    }

}
