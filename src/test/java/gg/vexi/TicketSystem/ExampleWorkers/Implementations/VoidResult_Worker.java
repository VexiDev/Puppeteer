package gg.vexi.TicketSystem.ExampleWorkers.Implementations;

import gg.vexi.TicketSystem.Exceptions.ExceptionRecord;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.annotations.AssociatedActionType;
import gg.vexi.TicketSystem.core.AbstractWorker;
import gg.vexi.TicketSystem.ticket.Ticket;

@AssociatedActionType("VoidResult_Worker")
public class VoidResult_Worker extends AbstractWorker {

    private Object data;

    public VoidResult_Worker(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        super.setStatus(Status.PROCESSING);

        try {
            Thread.sleep(200);
            data = null;
            super.complete(Status.SUCCESS, data);
        } catch (InterruptedException e) {
            super.recordException(new ExceptionRecord("InterruptedException", e.getMessage()));
        }
    }
}
