package gg.vexi.TicketSystem.Mocks.MockWorkers;

import gg.vexi.TicketSystem.AbstractWorker;
import gg.vexi.TicketSystem.Exceptions.ExceptionRecord;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.Ticket.Ticket;

// using ExceptionRecord as our object just cause I need a good example object
public class Worker_objectGeneric extends AbstractWorker<ExceptionRecord> {

    public Worker_objectGeneric(Ticket ticket) { super(ticket); }

    @Override
    public void start() {
        super.setStatus(Status.PROCESSING);
    }
}