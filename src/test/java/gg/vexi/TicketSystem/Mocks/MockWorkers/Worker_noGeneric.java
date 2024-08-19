package gg.vexi.TicketSystem.Mocks.MockWorkers;

import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.core.AbstractWorker;
import gg.vexi.TicketSystem.ticket.Ticket;

public class Worker_noGeneric extends AbstractWorker {

    public Worker_noGeneric(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        super.setStatus(Status.PROCESSING);
    }
}
