package gg.vexi.TicketSystem.Ticket;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;

public class TicketResult {

    private final Ticket target_ticket;
    private final JsonObject data = new JsonObject();
    private final boolean status = true;

    public TicketResult(Ticket ticket) { target_ticket = ticket; }

    public Ticket getTicket() { return target_ticket; }
    public JsonObject getData() { return data; }

    public boolean getStatus() { return status; }
    public CaughtExceptions getExceptions() { return new CaughtExceptions(); }
}
