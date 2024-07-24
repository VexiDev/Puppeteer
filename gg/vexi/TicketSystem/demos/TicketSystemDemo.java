package gg.vexi.TicketSystem.demos;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import gg.vexi.TicketSystem.TicketManager;
import gg.vexi.TicketSystem.Enums.ActionType;
import gg.vexi.TicketSystem.Factory.WorkerFactory;

import java.util.ArrayList;

public class TicketSystemDemo {
    private static final Random random = new Random();

    public static void main(String[] args) {
        TicketManager ticketManager = new TicketManager(4);
        WorkerFactory.setTicketManager(ticketManager);
        

        // demo array to track all our example futures
        List<CompletableFuture<TicketManager.TicketResult>> futures = new ArrayList<>();

        futures.add(ticketManager.scheduleTicket(
            ActionType.WORLD_CREATION, 
            new HashMap<>(), 
            createCustomerCallback("Customer 1", ticketManager)
        ));

        futures.add(ticketManager.scheduleTicket(
            ActionType.WORLD_LOAD, 
            new HashMap<>(), 
            createCustomerCallback("Customer 2", ticketManager)
        ));

        futures.add(ticketManager.scheduleTicket(
            ActionType.WORLD_CREATION, 
            new HashMap<>(), 
            createCustomerCallback("Customer 3", ticketManager)
        ));

        // 4Demo: destroy ticketmanager when all tickets are completed
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            System.out.println("All tickets completed");
            ticketManager.shutdown();
        }).join();

        printResults(futures);
    }

    private static TicketManager.CustomerCallback createCustomerCallback(String customerName, TicketManager ticketManager) {
        return new TicketManager.CustomerCallback() {
            @Override
            public void onNotification(UUID ticketId, String message) {
                System.out.println(customerName + " - Notification for ticket " + ticketId + ": " + message);
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

    private static void printResults(List<CompletableFuture<TicketManager.TicketResult>> futures) {
        for (int i = 0; i < futures.size(); i++) {
            try {
                TicketManager.TicketResult result = futures.get(i).get();
                System.out.println("Customer " + (i + 1) + " (ID: " + result.customerId + ") ticket " + (result.success ? "succeeded" : "failed"));
            } catch (Exception e) {
                System.out.println("Error getting result for Customer " + (i + 1) + ": " + e.getMessage());
            }
        }
    }
}