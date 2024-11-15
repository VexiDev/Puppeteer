package gg.vexi.Puppeteer.Core;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.PuppetStatus;
import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Ticket.Result;

public abstract class Puppet {

    private PuppetStatus status = PuppetStatus.CREATED;
    private final CompletableFuture<Result> future;
    private final Ticket ticket;
    protected final ProblemHandler problemHandler;
    // TODO: Consider making parameter map read only?
    protected final Map<String, Object> parameters;

    public Puppet(Ticket ticket) {
        this.ticket = ticket;
        problemHandler = new ProblemHandler();
        future = new CompletableFuture<>();
        parameters = ticket.getParameters();
        status = PuppetStatus.READY;

    }

    // entry point to puppet
    public abstract void main();

    // start point of puppet (run by puppeteer)
    // This allows for automatic unhandled exception handling
    // using the ProblemHandler in case one is missed
    public final void start() {
        problemHandler.attempt(() -> {
            setStatus(PuppetStatus.PROCESSING);
            main();
        }, problem -> {
            status = PuppetStatus.ERROR;
            complete(ResultStatus.FAILED, null);
        }); 
    }

    // exit point of puppet
    protected void complete(ResultStatus result_status) {
        complete(result_status, null);
    }

    protected void complete(ResultStatus result_status, Object data) {
        status = PuppetStatus.COMPLETED;
        Result result = new Result(problemHandler, ticket, result_status, data);

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
        return ticket;
    }

    // setters
    public void setStatus(PuppetStatus status) {
        this.status = status;
    }

}
