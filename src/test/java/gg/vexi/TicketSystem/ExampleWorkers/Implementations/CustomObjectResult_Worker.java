package gg.vexi.TicketSystem.ExampleWorkers.Implementations;

import gg.vexi.TicketSystem.Core.AbstractWorker;
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.Exceptions.ExceptionRecord;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.annotations.RegisterWorker;

@RegisterWorker
public class CustomObjectResult_Worker extends AbstractWorker {

    private ExceptionRecord data;

    public CustomObjectResult_Worker(Ticket Ticket) {
        super(Ticket);
    }

    @Override
    public void main() {

        try {
            Thread.sleep(200);
            data = new ExceptionRecord("CustomObjectResult", "This is an instance of ExceptionRecord to test worker's returning custom objects");
            super.complete(Status.SUCCESS, data);
        } catch (InterruptedException e) {
            super.recordException(new ExceptionRecord("InterruptedException", e.getMessage()));
        }
    }
}
