package gg.vexi.TicketSystem.ticket;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
import gg.vexi.TicketSystem.Status;

public class TicketResult {

    private final CaughtExceptions exceptions;
    private final Ticket target_ticket;
    private final Status status;
    private final Object data;

    public TicketResult(CaughtExceptions caughtExceptions, Ticket ticket, Status result_status, Object Data) {
        exceptions = caughtExceptions;
        target_ticket = ticket;
        status = result_status;
        data = Data;
    }


    public Ticket getTicket() {
        return target_ticket;
    }

    public Object getData() {
        return data;
    }

    public Status getStatus() {
        return status;
    }

    public CaughtExceptions getExceptions() {
        return exceptions;
    }
}
