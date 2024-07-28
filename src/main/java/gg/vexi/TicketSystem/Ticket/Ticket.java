package gg.vexi.TicketSystem.Ticket;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;

public class Ticket {

    private final UUID Id;
    private final ActionType type;
    private final TicketPriority priority;
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
