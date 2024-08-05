package gg.vexi.TicketSystem.Mocks.MockWorkers;

import gg.vexi.TicketSystem.AbstractWorker;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.Ticket.Ticket;

public class Worker_objectGeneric extends AbstractWorker<ExceptionRecord> {

    public Worker_objectGeneric(Ticket ticket) { super(ticket); }

    @Override
    public void start() {
        super.setStatus(Status.PROCESSING);
    }
}