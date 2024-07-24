package gg.vexi.TicketSystem.Factory;

import java.util.List;

import gg.vexi.TicketSystem.TicketManager;
import gg.vexi.TicketSystem.Enums.StatusEnum;
import gg.vexi.TicketSystem.TicketManager.DecisionRequest;
import gg.vexi.TicketSystem.Tickets.Ticket;

public abstract class AbstractWorker {
    protected TicketManager ticketManager;

    public AbstractWorker(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }

    public abstract void processTicket(Ticket ticket);

    protected void notifyCustomer(Ticket ticket, String message) {
        ticketManager.notifyCustomer(ticket, message);
    }

    protected void updateProgress(Ticket ticket, int progress) {
        ticketManager.updateTicketProgress(ticket, progress);
    }

    protected void updateStatus(Ticket ticket, StatusEnum status) {
        ticketManager.updateTicketStatus(ticket, status);
    }

    protected void logAction(Ticket ticket, String action) {
        ticketManager.logTicketAction(ticket, action);
    }

    protected <T> T requestDecision(Ticket ticket, String question, List<T> options, T defaultOption, long timeoutSeconds) 
            throws IllegalArgumentException, InterruptedException {
        if (!options.contains(defaultOption)) {
            throw new IllegalArgumentException("Default option must be one of the provided options");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }

        DecisionRequest<T> request = new DecisionRequest<>(question, options, defaultOption, timeoutSeconds);
        return ticketManager.requestDecision(ticket, request);
    }
}