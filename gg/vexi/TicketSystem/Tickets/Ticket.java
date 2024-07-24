package gg.vexi.TicketSystem.Tickets;

import java.util.*;
import java.util.concurrent.*;

import gg.vexi.TicketSystem.Enums.ActionType;
import gg.vexi.TicketSystem.Enums.StatusEnum;

public class Ticket implements Comparable<Ticket> {
    private UUID id;
    private UUID customerId;
    private ActionType actionType;
    private Map<String, Object> parameters;
    private TicketStatus status;
    private CompletableFuture<Boolean> completionFuture;
    private Map<String, Object> ticketData;
    private int priority;

    public Ticket(UUID customerId, ActionType actionType, Map<String, Object> parameters) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.actionType = actionType;
        this.parameters = parameters;
        this.status = new TicketStatus(StatusEnum.QUEUED);
        this.completionFuture = new CompletableFuture<>();
        this.ticketData = new ConcurrentHashMap<>();
        this.priority = 0;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public ActionType getActionType() { return actionType; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(StatusEnum status) { this.status.setStatus(status); }
    public CompletableFuture<Boolean> getCompletionFuture() { return completionFuture; }
    public void setCompletionFuture(CompletableFuture<Boolean> future) { this.completionFuture = future; }

    public void updateStatusData(String key, Object value) {
        status.setStatusData(key, value);
    }

    public Object getStatusData(String key) {
        return status.getStatusData(key);
    }

    public void updateTicketData(String key, Object value) {
        ticketData.put(key, value);
    }

    public Object getTicketData(String key) {
        return ticketData.get(key);
    }

    public Map<String, Object> getTicketDataMap() {
        return new HashMap<>(ticketData);
    }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    @Override
    public int compareTo(Ticket other) {
        return Integer.compare(other.priority, this.priority);
    }
}