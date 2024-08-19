package gg.vexi.TicketSystem.Mocks.MockWorkers;

import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.core.AbstractWorker;
import gg.vexi.TicketSystem.ticket.Ticket;

public class Worker_jsonGeneric extends AbstractWorker {

    public Worker_jsonGeneric(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        super.setStatus(Status.PROCESSING);
    }
}
