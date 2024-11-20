package gg.vexi.Puppeteer.Core;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.Ticket.Result;

public class Ticket<T> implements Comparable<Ticket<?>> {

    private final UUID id;
    private final Instant timestamp;
    private final String puppet;
    private final TicketPriority priority;
    private final Map<String, Object> parameters;
    private final CompletableFuture<Result<T>> future;
    
    //TODO: Add overloads

    public Ticket(
        String puppet,
        TicketPriority priority,
        // TODO: Convert parameters to be generic instead of Object
        Map<String, Object> parameters,
        CompletableFuture<Result<T>> future) {
        // arguemnts are all required
        Objects.requireNonNull(puppet, "Ticket puppet cannot be null");
        Objects.requireNonNull(priority, "Ticket priority cannot be null");
        Objects.requireNonNull(parameters, "Ticket parameters cannot be null");
        Objects.requireNonNull(future, "Ticket future cannot be null");

        id = UUID.randomUUID();
        timestamp = Instant.now();
        this.puppet = puppet.toLowerCase();
        this.priority = priority;
        this.parameters = parameters;
        this.future = future;
    }

    // Sorts from highest to lowest priority then from oldest to newest timestamp
    // Allows for a consistent FIFO queue even for equal priorities
    @Override
    public int compareTo(Ticket<?> other) {
        int p = Integer.compare(other.priority.ordinal(), this.priority.ordinal());
        return (p != 0) ? p : this.timestamp().compareTo(other.timestamp);
    }

    public String puppet() {
        return this.puppet;
    }

    public TicketPriority priority() {
        return this.priority;
    }

    public synchronized Map<String, Object> parameters() {
        return this.parameters;
    }

    public UUID id() {
        return this.id;
    }

    public Instant timestamp() {
        return this.timestamp;
    }

    public CompletableFuture<Result<T>> future() {
        return this.future;
    }

    public final void completeExceptionally(ProblemHandler problemHandler) {
        this.future.complete(
            Result.complete(null, ResultStatus.ERROR_FAILED, problemHandler));
    }

}
