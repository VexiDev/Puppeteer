package gg.vexi.Puppeteer.Core;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import gg.vexi.Puppeteer.Core.ResultStatus;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.PuppetStatus;
import gg.vexi.Puppeteer.Ticket.Result;

public abstract class Puppet<T> {
    private PuppetStatus status = PuppetStatus.CREATED;
    private final CompletableFuture<Result<T>> future;
    private final Ticket<T> ticket;
    protected final ProblemHandler problemHandler;
    // TODO: Consider making parameter map read only?
    protected final Map<String, Object> parameters;

    public Puppet(Ticket<T> ticket) {
        this.ticket = ticket;
        this.problemHandler = new ProblemHandler();
        this.future = new CompletableFuture<>();
        this.parameters = ticket.parameters();
        this.status = PuppetStatus.READY;
    }

    // entry point to puppet
    public abstract void main();

    // start point of puppet (run by puppeteer)
    // This allows for automatic unhandled exception handling using the ProblemHandler in case one is missed
    public final void start() {
        this.problemHandler.attempt(() -> {
            setStatus(PuppetStatus.PROCESSING);
            main();
        }, problem -> { completeExceptionally(); });
    }

    // exit point of puppet
    protected void complete(ResultStatus result_status) { complete(result_status, null); }

    protected final void complete(ResultStatus result_status, T data) {
        this.status = PuppetStatus.COMPLETED;
        Result<T> result = Result.complete(data, result_status, this.problemHandler);
        this.future.complete(result);
    }

    protected final void completeExceptionally() {
        this.status = PuppetStatus.ERROR;
        this.future.complete(Result.complete(null, ResultStatus.ERROR_FAILED, this.problemHandler));
    }

    public Ticket<T> getTicket() { return this.ticket; }

    public PuppetStatus getStatus() { return this.status; }

    public CompletableFuture<Result<T>> getFuture() { return this.future; }

    public void setStatus(PuppetStatus status) { this.status = status; }
}
