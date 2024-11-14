package gg.vexi.Puppeteer.Exceptions;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

public class ProblemHandler {

    private final ConcurrentHashMap<String, Problem> problems;
    private final int maxSize;

    public ProblemHandler(int maxSize) {
        this.problems = new ConcurrentHashMap<>();
        this.maxSize = maxSize;
    }

    public ProblemHandler() {
        this(1000); // Default maxSize of 1000
    }


//TODO: Create genID() and handleOverflow() tests before implementing

    public Problem handle(Throwable t) {
        // TODO: Move this to genID(Throwable t) method?
        // Note: allows for custom ids if wanted
        // Create simple ID based on timestamp and hash
        String id = String.format("%d-%d",
                System.currentTimeMillis(),
                System.identityHashCode(t));

        Problem problem = new Problem(t, id);

        // TODO: Explore moving overflow handling to its own method?
        // Note: moving this to its own method could make it a good place to
        //       hook into and have it do different things when maxSize is reached
        // If we're at capacity, remove oldest entry
        if (problems.size() >= this.maxSize) {
            Optional<String> oldest = problems.entrySet().stream()
                    .min((e1, e2) -> e1.getValue().getTimestamp()
                            .compareTo(e2.getValue().getTimestamp()))
                    .map(e -> e.getKey());
            oldest.ifPresent(problems::remove);
        }

        problems.put(id, problem);
        return problem;
    }

    // Convenience methods to wrap risky code

    // Handles exceptions and rethrows them for handling at higher level
    public <T> T execute(ThrowableSupplier<T> supplier) throws Exception {
        try {
            return supplier.get();
        } catch (Throwable t) {
            handle(t);
            if (t instanceof Exception) {
                throw (Exception) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            }
            throw new RuntimeException(t);
        }
    }

    // Handles and returns Optional for handling throwables functionally
    public <T> Optional<T> attempt(ThrowableSupplier<T> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Throwable t) {
            handle(t);
            return Optional.empty();
        }
    }

    // Handles and returns a simple default value
    public <T> T attemptOrElse(ThrowableSupplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            handle(t);
            return defaultValue;
        }
    }

    // Handles with custom error handling logic
    public <T> T attemptWith(ThrowableSupplier<T> supplier,
            ErrorHandler<T> errorHandler) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            Problem problem = handle(t);
            return errorHandler.handleError(problem);
        }
    }

    @FunctionalInterface
    public interface ErrorHandler<T> {
        T handleError(Problem problem);
    }

    @FunctionalInterface
    public interface ThrowableSupplier<T> {
        T get() throws Throwable;
    }

    // Getters

    public final boolean isEmpty() {
        return problems.isEmpty();
    }

    public final List<Problem> getAll() {
        return new ArrayList<>(problems.values());
    }

    public final List<Problem> getRecent(int count) {
        return problems.values().stream()
                .sorted((r1, r2) -> r2.getTimestamp().compareTo(r1.getTimestamp()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public final List<Problem> getByType(Class<? extends Throwable> type) {
        return problems.values().stream()
                .filter(r -> type.isInstance(r.getThrowable()))
                .collect(Collectors.toList());
    }

    public final Optional<Problem> get(String id) {
        return Optional.ofNullable(problems.get(id));
    }

    public final void clear() {
        problems.clear();
    }

    public final int size() {
        return problems.size();
    }

    public final int maxSize() {
        return this.maxSize;
    }
    
    public static class Problem {
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
                    String className = "."+this.getClass().getSimpleName();
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
}
