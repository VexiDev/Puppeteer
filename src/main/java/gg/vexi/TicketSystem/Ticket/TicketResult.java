package gg.vexi.TicketSystem.Ticket;

import com.google.gson.JsonObject;

public class TicketResult {

    private final Ticket target_ticket;
    private final JsonObject data = new JsonObject();

    public TicketResult(Ticket ticket) { target_ticket = ticket; }

    public Ticket getTicket() { return target_ticket; }
    public JsonObject getData() { return data; }
}
