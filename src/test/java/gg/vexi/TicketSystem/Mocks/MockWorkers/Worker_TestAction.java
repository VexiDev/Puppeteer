package gg.vexi.TicketSystem.Mocks.MockWorkers;

import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.annotations.AssociatedActionType;
import gg.vexi.TicketSystem.core.AbstractWorker;
import gg.vexi.TicketSystem.ticket.Ticket;

@AssociatedActionType("test_action")
public class Worker_TestAction extends AbstractWorker {

    public Worker_TestAction(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        super.setStatus(Status.PROCESSING);
    }
}
