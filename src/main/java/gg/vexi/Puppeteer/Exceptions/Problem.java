
package gg.vexi.Puppeteer.Exceptions;

import java.time.Instant;
import java.util.Optional;

public class Problem {
    private final String id;
    private final Throwable throwable;
    private final Instant timestamp;
    private final String threadName;
    private final String location;

    public Problem(Throwable throwable, String id) {
        this.id = id;
        this.throwable = throwable;
        this.timestamp = Instant.now();
        this.threadName = Thread.currentThread().getName();

        // Get the first non-framework stack trace element
        // This will be the throwable location
        this.location = Optional.ofNullable(throwable.getStackTrace())
            .filter(stack -> stack.length > 0)
            .map(stack -> {
                //TODO: Find a better way to filter Puppeteer out
                String className = ".Puppeteer.Puppeteer."; 
                return java.util.Arrays.stream(stack)
                    .filter(element -> !element.getClassName()
                        .contains(className))
                    .findFirst()
                    .orElse(null); // null forces Unknown location but
                                   // this will never happen
                                   // (said every dev ever)
            })
            .map(element -> element.getClassName() + "." +
                element.getMethodName() + ":" +
                element.getLineNumber())
            .orElse("Unknown location");
    }

    public String getId() {
        return id;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Throwable get() {
        return getThrowable();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s in %s at %s: %s",
            id,
            throwable.getClass().getSimpleName(),
            threadName,
            location,
            throwable.getMessage());
    }
}
