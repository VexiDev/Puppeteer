package gg.vexi.TicketSystem.Worker;

import java.util.Arrays;
import java.util.List;

import gg.vexi.TicketSystem.TicketManager;
import gg.vexi.TicketSystem.Enums.StatusEnum;
import gg.vexi.TicketSystem.Factory.AbstractWorker;
import gg.vexi.TicketSystem.Tickets.Ticket;

public class WorldCreationWorker extends AbstractWorker {
    public WorldCreationWorker(TicketManager ticketManager) {
        super(ticketManager);
    }

    @Override
    public void processTicket(Ticket ticket) {
        try {
            updateStatus(ticket, StatusEnum.PROCESSING);
            notifyCustomer(ticket, "World creation started");

            // Simulate world creation steps
            for (int i = 0; i < 5; i++) {
                updateProgress(ticket, i * 20);
                Thread.sleep(1000);
            }

            // Request a decision
            List<String> options = Arrays.asList("Normal", "Flat", "Void");
            String defaultOption = "Normal";
            String decision = requestDecision(ticket, "Choose world type", options, defaultOption, 30); // 30 seconds timeout

            ticket.updateTicketData("worldType", decision);
            notifyCustomer(ticket, "World type set to: " + decision);

            // Complete the world creation
            updateProgress(ticket, 100);
            logAction(ticket, "World created successfully");
            ticketManager.completeTicket(ticket, true);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ticketManager.completeTicket(ticket, false);
            
        } catch (IllegalArgumentException e) {
            logAction(ticket, "Error in decision request: " + e.getMessage());
            ticketManager.completeTicket(ticket, false);
        }
    }
}