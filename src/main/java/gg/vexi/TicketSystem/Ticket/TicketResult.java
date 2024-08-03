package gg.vexi.TicketSystem.Ticket;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
import gg.vexi.TicketSystem.Status;

public class TicketResult<T> {

    private final Ticket target_ticket;
    private final Status status;
    private final CaughtExceptions exceptions;
    private final T data;

    public TicketResult(Ticket ticket, Status result_status, T Data, CaughtExceptions caughtExceptions) {
        target_ticket = ticket;
        status = result_status;
        data = Data;
        exceptions = caughtExceptions;
    }


    public Ticket getTicket() {
        return target_ticket;
    }

    public T getData() {
        return data;
    }

    public Status getStatus() {
        return status;
    }

    public CaughtExceptions getExceptions() {
        return exceptions;
    }
}
