package gg.vexi.TicketSystem.Ticket;

import java.util.UUID;

import com.google.gson.JsonObject;

public class Ticket {

    private final UUID Id;
    private final ActionType type;
    private final TicketPriority priority;
    private final JsonObject parameters;

    public Ticket(ActionType ActionType, TicketPriority TicketPriority, JsonObject Parameters) {
        Id = UUID.randomUUID();
        type = ActionType;
        priority = TicketPriority;
        parameters = Parameters;
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

}
