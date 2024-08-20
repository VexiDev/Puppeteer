package gg.vexi.TicketSystem.ExampleWorkers;

import gg.vexi.TicketSystem.Core.AbstractWorker;
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.annotations.AssociatedActionType;

// THIS IS THE PLACEHOLDER WORKER FOR ALL GENERAL UNIT TESTS

// IMPLEMENTATION TESTS USE OUR EXAMPLE WORKERS

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
