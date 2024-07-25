package gg.vexi.TicketSystem.demos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.TicketManager;
import gg.vexi.TicketSystem.Enums.ActionType;
import gg.vexi.TicketSystem.Enums.StatusEnum;
import gg.vexi.TicketSystem.Factory.WorkerFactory;
import gg.vexi.TicketSystem.Tickets.Ticket;

public class NewTicketSystemDemo {
    private static final Random random = new Random();

    public static void main(String[] args) throws Exception {
        TicketManager ticketManager = new TicketManager(4);
        WorkerFactory.setTicketManager(ticketManager);

        // Schedule 2 creation tickets to fill up the queue
        ticketManager.scheduleTicket(ActionType.WORLD_LOAD, new JsonObject(), createCustomerCallback("Customer 1", ticketManager));
        ticketManager.scheduleTicket(ActionType.WORLD_LOAD, new JsonObject(), createCustomerCallback("Customer 2", ticketManager));

        // Start timer
        long startTime = System.currentTimeMillis();

        // Schedule the 3rd creation ticket (our ticket of interest)
        AtomicReference<Ticket> trackedTicket = new AtomicReference<>();
        CompletableFuture<TicketManager.TicketResult> future = ticketManager.scheduleTicket(
            ActionType.WORLD_LOAD,
            new JsonObject(),
            createCustomerCallback("Customer 3", ticketManager, trackedTicket)
        );

        // Monitor queue position
        while (trackedTicket.get() == null || trackedTicket.get().getStatus().getStatus() == StatusEnum.QUEUED) {
            if (trackedTicket.get() != null) {
                Map<String, Object> queueStatus = trackedTicket.get().getTicketDataMap();
                if (queueStatus.containsKey("QueueStatus")) {
                    Map<String, Object> queueStatusData = (Map<String, Object>) queueStatus.get("QueueStatus");
                    System.out.println("Current queue position: " + queueStatusData.get("position"));
                }
            }
            Thread.sleep(500);
        }

        // Wait for the ticket to complete
        TicketManager.TicketResult result = future.get();

        // Stop timer
        long endTime = System.currentTimeMillis();

        // Print ticket summary
        printTicketSummary(trackedTicket.get(), result, endTime - startTime);

        // Shutdown the ticket manager
        ticketManager.shutdown();
    }

    private static TicketManager.CustomerCallback createCustomerCallback(String customerName, TicketManager ticketManager, AtomicReference<Ticket> trackedTicket) {
        return new TicketManager.CustomerCallback() {
            @Override
            public void onNotification(UUID ticketId, String message) {
                System.out.println(customerName + " - Notification for ticket " + ticketId + ": " + message);
                // Update the tracked ticket when we receive a notification
                if (trackedTicket.get() == null || trackedTicket.get().getId().equals(ticketId)) {
                    trackedTicket.set(ticketManager.getTicket(ticketId));
                }
            }

            @Override
            public <T> void onDecisionRequest(UUID ticketId, String question, List<T> options) {
                System.out.println(customerName + " - Decision required for ticket " + ticketId + ": " + question);
                System.out.println("Options: " + String.join(", ", options.stream().map(Object::toString).toList()));
                
                T randomDecision = options.get(random.nextInt(options.size()));
                System.out.println(customerName + " - Randomly selected decision: " + randomDecision);
                
                try {
                    Thread.sleep(random.nextInt(5000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                ticketManager.provideDecision(ticketId, randomDecision);
            }
        };
    }

    private static TicketManager.CustomerCallback createCustomerCallback(String customerName, TicketManager ticketManager) {
        return createCustomerCallback(customerName, ticketManager, new AtomicReference<>());
    }

    private static void printTicketSummary(Ticket ticket, TicketManager.TicketResult result, long timeTaken) {
        System.out.println("\nTicket Summary:");
        System.out.println("Customer ID: " + result.customerId);
        System.out.println("Ticket ID: " + ticket.getId());
        System.out.println("Status: " + (result.success ? "Succeeded" : "Failed"));
        System.out.println("Time taken: " + timeTaken + " ms");
        System.out.println("World Type: " + ticket.getTicketData("worldType"));
        System.out.println("Final Progress: " + ticket.getStatusData("progress") + "%");
    }
}