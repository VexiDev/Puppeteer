package gg.vexi.TicketSystem.Mocks.MockWorkers;

import gg.vexi.TicketSystem.AbstractWorker;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.Ticket.Ticket;

public class Worker_noGeneric extends AbstractWorker {

    public Worker_noGeneric(Ticket ticket) { super(ticket); }

    @Override
    public void start() {
        super.setStatus(Status.PROCESSING);
    }
}