package gg.vexi.Puppeteer.Ticket;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.CaughtExceptions;

public class Result {

    private final CaughtExceptions exceptions;
    private final Ticket target_ticket;
    private final ResultStatus status;
    private final Object data;

    public Result(CaughtExceptions caughtExceptions, Ticket ticket, ResultStatus result_status, Object Data) {
        exceptions = caughtExceptions;
        target_ticket = ticket;
        status = result_status;
        data = Data;
    }

    public boolean isSuccessful() {
        return status == ResultStatus.SUCCESS;
    }

    public boolean hasExceptions() {
        return exceptions.any();
    }

    // getters
    public Ticket getTicket() {
        return target_ticket;
    }

    public Object getData() {
        return data;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public CaughtExceptions getExceptions() {
        return exceptions;
    }
}
