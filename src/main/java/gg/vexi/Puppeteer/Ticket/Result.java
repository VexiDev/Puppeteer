package gg.vexi.Puppeteer.Ticket;

import gg.vexi.Puppeteer.Core.ResultStatus;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;

public class Result<T> {

    private final ProblemHandler problemsHandler;
    private final ResultStatus resultStatus;
    private final T data;

    public Result(T data, ResultStatus resultStatus, ProblemHandler problemHandler) {
        this.problemsHandler = problemHandler;
        this.resultStatus = resultStatus;
        this.data = data;
    }

    public static <T> Result<T> success(T value, ResultStatus status, ProblemHandler problemHandler) {
        return new Result<>(value, status, problemHandler);
    }

    public static <T> Result<T> failed(ResultStatus status, ProblemHandler problemHandler) {
        return new Result<>(null, status, problemHandler);
    }

    public boolean isSuccessful() { return this.resultStatus == ResultStatus.SUCCESS; }

    public boolean hasExceptions() { return !this.problemsHandler.isEmpty(); }

    // getters
    public T data() { return this.data; }

    public ResultStatus status() { return this.resultStatus; }

    public ProblemHandler problemHandler() { return this.problemsHandler; }
}
