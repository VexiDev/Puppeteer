package gg.vexi.TicketSystem.Exceptions;

import java.util.concurrent.atomic.AtomicLong;

public class ExceptionRecord {

    final private long id = new AtomicLong(System.currentTimeMillis()).getAndIncrement();
    final private String type;
    final private String message;

    public ExceptionRecord(String error_type, String error_msg) {
        type = error_type;
        message = error_msg;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

}
