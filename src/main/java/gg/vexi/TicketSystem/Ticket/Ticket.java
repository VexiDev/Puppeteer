package gg.vexi.TicketSystem.Ticket;

import com.google.gson.JsonObject;

public class Ticket {

    private final ActionType type;
    private final TicketPriority priority;
    private final JsonObject parameters;

    public Ticket(ActionType ActionType, TicketPriority TicketPriority, JsonObject Parameters) {
        type = ActionType;
        priority = TicketPriority;
        parameters = Parameters;
    }

    public ActionType getType() { return type; }
    public TicketPriority getPriority() { return priority; }
    public JsonObject getParameters() { return parameters; }

}
