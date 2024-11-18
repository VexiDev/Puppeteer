package gg.vexi.Puppeteer.Exceptions;

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
    // Overload for handling void operations 
    public void execute(ThrowableRunnable runnable) throws Exception {
        try {
            runnable.run();
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
    public <T> Optional<T> attemptOptional(ThrowableSupplier<T> supplier) {
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
    public <T> T attempt(ThrowableSupplier<T> supplier,
            ErrorHandler<T> errorHandler) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            Problem problem = handle(t);
            return errorHandler.handleError(problem);
        }
    }
    // Overload for handling void operations
    public void attempt(ThrowableRunnable runnable, VoidErrorHandler errorHandler) {
        try {
            runnable.run();
        } catch (Throwable t) {
            Problem problem = handle(t);
            errorHandler.handleError(problem);
        }
    }
    
    // For operations with return values
    @FunctionalInterface
    public interface ErrorHandler<T> {
        T handleError(Problem problem);
    }
    @FunctionalInterface
    public interface ThrowableSupplier<T> {
        T get() throws Throwable;
    }
    
    // For operations without void return values
    @FunctionalInterface
    public interface ThrowableRunnable {
        void run() throws Throwable;
    }
    @FunctionalInterface
    public interface VoidErrorHandler {
        void handleError(Problem problem);
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
    
    }
