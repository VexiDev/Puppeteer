package gg.vexi.TicketSystem.Mocks.MockWorkers;

import gg.vexi.TicketSystem.AbstractWorker;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.Ticket.Ticket;

public class Worker_primitiveGeneric extends AbstractWorker<int> {

    public Worker_primitiveGeneric(Ticket ticket) { super(ticket); }

    @Override
    public void start() {
        super.setStatus(Status.PROCESSING);
    }
}