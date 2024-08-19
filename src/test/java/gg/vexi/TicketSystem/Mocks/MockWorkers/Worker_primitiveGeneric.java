package gg.vexi.TicketSystem.Mocks.MockWorkers;

import gg.vexi.TicketSystem.AbstractWorker;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.ticket.Ticket;

// <T> does not allow primitives so this is to test auto-unboxing
public class Worker_primitiveGeneric extends AbstractWorker {

    public Worker_primitiveGeneric(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        super.setStatus(Status.PROCESSING);
    }
}
