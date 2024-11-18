package gg.vexi.Puppeteer.Core;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.Result;

public class Ticket implements Comparable<Ticket> {

    private final UUID id;
    private final String puppet;
    private final TicketPriority priority;
    private final Map<String, Object> parameters;
    private final CompletableFuture<Result> future;

    public Ticket(
                String puppet,
                TicketPriority priority, 
                Map<String, Object> parameters, 
                CompletableFuture<Result> future) 
    {   
        id = UUID.randomUUID();
        this.puppet = puppet.toLowerCase();
        this.priority = priority;
        this.parameters = parameters;
        this.future = future;
    }

    @Override
    public int compareTo(Ticket other) {
        return Integer.compare(other.priority.ordinal(), this.priority.ordinal());
    }

    public String getPuppet() {
        return puppet;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public synchronized Map<String, Object> getParameters() {
        return parameters;
    }

    public UUID getId() {
        return id;
    }

    public CompletableFuture<Result> getFuture() {
        return future;
    }

}
