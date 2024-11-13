package gg.vexi.Puppeteer.Core;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.Result;

public class Ticket implements Comparable<Ticket> {

    private final UUID Id;
    private final String type;
    private final TicketPriority priority;
    private final JsonObject parameters;
    private final CompletableFuture<Result> future;

    public Ticket(
                String ActionType,
                TicketPriority TicketPriority, 
                JsonObject Parameters, 
                CompletableFuture<Result> TicketFuture) 
    {
        Id = UUID.randomUUID();
        type = ActionType.toLowerCase();
        priority = TicketPriority;
        parameters = Parameters;
        future = TicketFuture;
    }

    @Override
    public int compareTo(Ticket other) {
        return Integer.compare(other.priority.ordinal(), this.priority.ordinal());
    }

    public String getType() {
        return type;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public JsonObject getParameters() {
        return parameters;
    }

    public UUID getId() {
        return Id;
    }

    public CompletableFuture<Result> getFuture() {
        return future;
    }

}
