
package gg.vexi.Puppeteer.Exceptions;

import java.time.Instant;
import java.util.Optional;

public class Problem implements Comparable<Problem> {
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
                // TODO: Find a better way to filter out non-framework
                String puppeteer = ".Puppeteer.Puppeteer."; // remove puppeteer
                String jdk = "jdk."; // remove java jdk
                return java.util.Arrays.stream(stack)
                    .filter(element -> (!element.getClassName().contains(puppeteer) &&
                                        !element.getClassName().contains(jdk)))
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

    public String id() {
        return id;
    }

    public Throwable get() {
        return throwable;
    }

    public Instant instant() {
        return timestamp;
    }

    public String threadName() {
        return threadName;
    }

    public String location() {
        return location;
    }

    @Override
    public int compareTo(Problem other) {
        return this.instant().compareTo(other.instant());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s in \"%s\" at %s: %s",
            id,
            throwable.getClass().getSimpleName(),
            threadName,
            location,
            throwable.getMessage());
    }
}
