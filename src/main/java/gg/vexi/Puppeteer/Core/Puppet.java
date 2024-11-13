package gg.vexi.Puppeteer.Core;

import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.Exceptions.CaughtExceptions;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.PuppetStatus;
import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Ticket.Result;

public abstract class Puppet {

    private PuppetStatus status = PuppetStatus.CREATED;
    private final CompletableFuture<Result> future;
    private final Ticket associated_ticket;
    private final CaughtExceptions exceptionHandler;
    protected final JsonObject ticket_parameters;

    public Puppet(Ticket ticket) {
        associated_ticket = ticket;
        exceptionHandler = new CaughtExceptions();
        future = new CompletableFuture<>();
        ticket_parameters = ticket.getParameters();
        status = PuppetStatus.READY;

    }
    
    // entry point to puppet
    public abstract void main();


    // start point of puppet (run by puppeteer)
    public final void start() {
        try {
            setStatus(PuppetStatus.PROCESSING);
            main();
        } catch (Exception e) {
            StringBuilder stackString = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) { 
                // only appends stack traces the puppet generated (this should be a toggle like showFullPuppetTrace or something)
                if (!element.toString().contains("gg.vexi.Puppeteer")) { 
                    stackString.append(element.toString()).append("\n"); 
                } else break; }
            recordException(new ExceptionRecord(String.format("\033[1;31mUNHANDLED\033[0m (%s)", e.getClass().getSimpleName()), String.format("%s\n%s", e.getMessage(), stackString.toString())));
            status = PuppetStatus.ERROR;
            complete(ResultStatus.FAILED, null);
        }
    }

    public final void recordException(ExceptionRecord record) { exceptionHandler.add(record); }
    public final void recordException(Exception e) {
        StringBuilder stackString = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) { stackString.append(element.toString()).append("\n"); } 
        recordException(new ExceptionRecord(e.getClass().getSimpleName(), String.format("%s\n%s", e.getMessage(), stackString.toString())));
        exceptionHandler.add(new ExceptionRecord(e.getClass().getName(), e.getMessage()));
    }

    // exit point of puppet
    protected void complete(ResultStatus result_status) { complete(result_status, null); }
    protected void complete(ResultStatus result_status, Object data) {
        status = PuppetStatus.COMPLETED;
        Result result = new Result(exceptionHandler, associated_ticket, result_status, data);
        future.complete(result);
    }

    // getters
    public PuppetStatus getStatus() {
        return status;
    }

    public CompletableFuture<Result> getFuture() {
        return future;
    }

    public Ticket getTicket() {
        return associated_ticket;
    }

    // setters
    public void setStatus(PuppetStatus new_status) {
        status = new_status;
    }

}
