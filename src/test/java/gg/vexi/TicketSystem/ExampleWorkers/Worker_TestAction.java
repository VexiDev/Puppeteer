package gg.vexi.TicketSystem.ExampleWorkers;

import gg.vexi.TicketSystem.Core.AbstractWorker;
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.Exceptions.ExceptionRecord;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.annotations.AssociatedActionType;

// THIS IS THE PLACEHOLDER WORKER FOR ALL GENERAL UNIT TESTS

// IMPLEMENTATION TESTS USE OUR EXAMPLE WORKERS

@AssociatedActionType("test_action")
public class Worker_TestAction extends AbstractWorker {

    private String data;

    public Worker_TestAction(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        super.setStatus(Status.PROCESSING);

        try {
            Thread.sleep(200);
            data = "Test_Action Worker Data";
            super.complete(Status.SUCCESS, data);
        } catch (InterruptedException e) {
            super.recordException(new ExceptionRecord("InterruptedException", e.getMessage()));
        }
    }
}
