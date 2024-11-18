package gg.vexi.Puppeteer.Ticket;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;

public class Result {

    private final ProblemHandler problemsHandler;
    private final Ticket ticket;
    private final ResultStatus resultStatus;
    private final Object data;

    public Result(
        ProblemHandler problemHandler,
        Ticket ticket,
        ResultStatus resultStatus,
        Object data) {
        this.problemsHandler = problemHandler;
        this.ticket = ticket;
        this.resultStatus = resultStatus;
        this.data = data;
    }

    public boolean isSuccessful() {
        return resultStatus == ResultStatus.SUCCESS;
    }

    public boolean hasExceptions() {
        return !problemsHandler.isEmpty();
    }

    // getters
    public Ticket ticket() {
        return ticket;
    }

    public Object data() {
        return data;
    }

    public ResultStatus status() {
        return resultStatus;
    }

    public ProblemHandler problemHandler() {
        return problemsHandler;
    }
}
