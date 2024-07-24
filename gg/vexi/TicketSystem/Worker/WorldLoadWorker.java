package gg.vexi.TicketSystem.Worker;

import gg.vexi.TicketSystem.TicketManager;
import gg.vexi.TicketSystem.Enums.StatusEnum;
import gg.vexi.TicketSystem.Factory.AbstractWorker;
import gg.vexi.TicketSystem.Tickets.Ticket;

public class WorldLoadWorker extends AbstractWorker {
    public WorldLoadWorker(TicketManager ticketManager) {
        super(ticketManager);
    }

    @Override
    public void processTicket(Ticket ticket) {
        try {
            updateStatus(ticket, StatusEnum.PROCESSING);
            notifyCustomer(ticket, "World loading started");

            // Simulate world loading steps
            for (int i = 0; i <= 3; i++) {
                updateProgress(ticket, i * 33);
                Thread.sleep(400);
            }

            // Complete the world creation
            updateProgress(ticket, 100);
            logAction(ticket, "World loaded successfully");
            ticketManager.completeTicket(ticket, true);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ticketManager.completeTicket(ticket, false);
            
        }
    }
}