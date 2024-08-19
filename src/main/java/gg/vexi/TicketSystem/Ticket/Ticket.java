package gg.vexi.TicketSystem.ticket;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

public class Ticket implements Comparable<Ticket> {

    private final UUID Id;
    private final ActionType type;
    private final TicketPriority priority; // may be made non-final if we implement a way to change ticket priority later on
    private final JsonObject parameters;
    private final CompletableFuture<TicketResult> future;

    public Ticket(
                ActionType ActionType, 
                TicketPriority TicketPriority, 
                JsonObject Parameters, 
                CompletableFuture<TicketResult> TicketFuture) 
    {
        Id = UUID.randomUUID();
        type = ActionType;
        priority = TicketPriority;
        parameters = Parameters;
        future = TicketFuture;
    }


    // compare method for automatic sorting when in queues
    @Override
    public int compareTo(Ticket other) {
        return Integer.compare(other.priority.ordinal(), this.priority.ordinal());
    }

    public ActionType getType() {
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
