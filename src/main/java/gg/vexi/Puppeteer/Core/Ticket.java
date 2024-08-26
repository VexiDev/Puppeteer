package gg.vexi.Puppeteer.Core;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.TicketResult;

public class Ticket implements Comparable<Ticket> {

    private final UUID Id;
    private final String type;
    private final TicketPriority priority; // may be made non-final if we implement a way to change ticket priority later on
    private final JsonObject parameters;
    private final CompletableFuture<TicketResult> future;

    public Ticket(
                String ActionType,
                TicketPriority TicketPriority, 
                JsonObject Parameters, 
                CompletableFuture<TicketResult> TicketFuture) 
    {
        Id = UUID.randomUUID();
        type = ActionType.toLowerCase();
        priority = TicketPriority;
        parameters = Parameters;
        future = TicketFuture;
    }


    // compare method for automatic sorting when in queues
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

    public CompletableFuture<TicketResult> getFuture() {
        return future;
    }

}
