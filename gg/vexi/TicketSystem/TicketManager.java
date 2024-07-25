package gg.vexi.TicketSystem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

import gg.vexi.TicketSystem.Enums.ActionType;
import gg.vexi.TicketSystem.Enums.StatusEnum;
import gg.vexi.TicketSystem.Factory.AbstractWorker;
import gg.vexi.TicketSystem.Factory.TicketFactory;
import gg.vexi.TicketSystem.Factory.WorkerFactory;
import gg.vexi.TicketSystem.Tickets.Ticket;

public class TicketManager {
    private final Map<ActionType, Queue<Ticket>> actionQueues;
    private final ExecutorService executorService;
    private final Map<UUID, CompletableFuture<?>> decisionFutures;
    private final Map<UUID, CustomerCallback> customerCallbacks;
    private final Map<ActionType, Ticket> processingTickets;
    private final Map<UUID, Ticket> allTickets;

    public TicketManager(int threadPoolSize) {
        actionQueues = new ConcurrentHashMap<>();
        for (ActionType type : ActionType.values()) {
            actionQueues.put(type, new ConcurrentLinkedQueue<>());
        }
        executorService = Executors.newFixedThreadPool(threadPoolSize);
        decisionFutures = new ConcurrentHashMap<>();
        customerCallbacks = new ConcurrentHashMap<>();
        processingTickets = new ConcurrentHashMap<>();
        allTickets = new ConcurrentHashMap<>();
    }

    public CompletableFuture<TicketResult> scheduleTicket(ActionType actionType, Map<String, Object> parameters, CustomerCallback callback) {
        UUID customerId = UUID.randomUUID();
        customerCallbacks.put(customerId, callback);

        Ticket ticket = TicketFactory.createTicket(customerId, actionType, parameters);
        CompletableFuture<Boolean> completionFuture = new CompletableFuture<>();
        ticket.setCompletionFuture(completionFuture);
        allTickets.put(ticket.getId(), ticket);

        synchronized (actionQueues.get(actionType)) {
            int queuePosition = actionQueues.get(actionType).size();
            if (queuePosition == 0 && !processingTickets.containsKey(actionType)) {
                processingTickets.put(actionType, ticket);
                ticket.setStatus(StatusEnum.PROCESSING);
                executorService.submit(() -> executeTicket(ticket));
            } else {
                ticket.setStatus(StatusEnum.QUEUED);
                ticket.updateTicketData("QueueStatus", Map.of("position", queuePosition));
                actionQueues.get(actionType).offer(ticket);
                notifyCustomer(ticket, "\033[35mTicket queued at position " + queuePosition + "\033[39m");
            }
        }

        return completionFuture.thenApply(success -> new TicketResult(customerId, success));
    }

    private void processNextTicket(ActionType actionType) {
        synchronized (actionQueues.get(actionType)) {
            Ticket nextTicket = actionQueues.get(actionType).poll();
            if (nextTicket != null) {
                processingTickets.put(actionType, nextTicket);
                nextTicket.setStatus(StatusEnum.PROCESSING);
                nextTicket.updateTicketData("QueueStatus", Map.of("position", 0));
                notifyCustomer(nextTicket, "Processing started");
                executorService.submit(() -> executeTicket(nextTicket));
            } else {
                processingTickets.remove(actionType);
            }
        }
    }

    private void executeTicket(Ticket ticket) {
        AbstractWorker worker = WorkerFactory.createWorker(ticket.getActionType());
        worker.start(ticket);
    }

    public CompletableFuture<Void> updateTicketStatus(Ticket ticket, StatusEnum status) {
        return CompletableFuture.runAsync(() -> {
            ticket.setStatus(status);
            notifyCustomer(ticket, "Status updated to " + status);
        });
    }

    public CompletableFuture<Void> updateTicketProgress(Ticket ticket, int progress) {
        return CompletableFuture.runAsync(() -> {
            ticket.updateStatusData("progress", progress);
            notifyCustomer(ticket, "Progress updated to " + progress + "%");
        });
    }

    public CompletableFuture<Void> notifyCustomer(Ticket ticket, String message) {
        return CompletableFuture.runAsync(() -> {
            CustomerCallback callback = customerCallbacks.get(ticket.getCustomerId());
            if (callback != null) {
                callback.onNotification(ticket.getId(), message);
            }
        });
    }

    public CompletableFuture<Void> logTicketAction(Ticket ticket, String action) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("Log: Ticket " + ticket.getId() + " - " + action);
        });
    }

    public <T> T requestDecision(Ticket ticket, DecisionRequest<T> request) throws InterruptedException {
        CompletableFuture<T> decisionFuture = new CompletableFuture<>();
        decisionFutures.put(ticket.getId(), decisionFuture);

        CustomerCallback callback = customerCallbacks.get(ticket.getCustomerId());
        if (callback != null) {
            callback.onDecisionRequest(ticket.getId(), request.question, request.options);
        }

        try {
            T result = decisionFuture.get(request.timeoutSeconds, TimeUnit.SECONDS);
            if (request.options.contains(result)) {
                return result;
            } else {
                throw new IllegalArgumentException("Provided decision is not a valid option");
            }
        } catch (TimeoutException e) {
            return request.defaultOption;
        } catch (ExecutionException e) {
            throw new RuntimeException("Error while waiting for decision", e);
        } finally {
            decisionFutures.remove(ticket.getId());
        }
    }

    public void provideDecision(UUID ticketId, Object decision) {
        CompletableFuture<?> future = decisionFutures.get(ticketId);
        if (future != null && !future.isDone()) {
            try {
                Method completeMethod = future.getClass().getMethod("complete", Object.class);
                completeMethod.invoke(future, decision);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to complete decision future", e);
            }
        }
    }

    public CompletableFuture<Void> completeTicket(Ticket ticket, boolean success) {
        return CompletableFuture.runAsync(() -> {
            if (success) {
                updateTicketStatus(ticket, StatusEnum.COMPLETED);
            } else {
                updateTicketStatus(ticket, StatusEnum.FAILED);
            }
            ticket.getCompletionFuture().complete(success);
            customerCallbacks.remove(ticket.getCustomerId());
            processingTickets.remove(ticket.getActionType());
            allTickets.remove(ticket.getId());
            processNextTicket(ticket.getActionType());
        });
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    public Ticket getTicket(UUID ticketId) {
        return allTickets.get(ticketId);
    }

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(allTickets.values());
    }

    public List<Ticket> getQueuedTickets(ActionType actionType) {
        return new ArrayList<>(actionQueues.get(actionType));
    }

    public Ticket getProcessingTicket(ActionType actionType) {
        return processingTickets.get(actionType);
    }

    public boolean removeTicket(UUID ticketId) {
        Ticket ticket = allTickets.get(ticketId);
        if (ticket == null) {
            return false;
        }

        if (processingTickets.containsValue(ticket)) {
            return false; // Can't remove a ticket that's being processed
        }

        actionQueues.get(ticket.getActionType()).remove(ticket);
        allTickets.remove(ticketId);
        customerCallbacks.remove(ticket.getCustomerId());
        return true;
    }

    public interface CustomerCallback {
        void onNotification(UUID ticketId, String message);
        <T> void onDecisionRequest(UUID ticketId, String question, List<T> options);
    }

    public static class TicketResult {
        public final UUID customerId;
        public final boolean success;

        public TicketResult(UUID customerId, boolean success) {
            this.customerId = customerId;
            this.success = success;
        }
    }

    public static class DecisionRequest<T> {
        public final String question;
        public final List<T> options;
        public final T defaultOption;
        public final long timeoutSeconds;

        public DecisionRequest(String question, List<T> options, T defaultOption, long timeoutSeconds) {
            this.question = question;
            this.options = options;
            this.defaultOption = defaultOption;
            this.timeoutSeconds = timeoutSeconds;
        }
    }
}