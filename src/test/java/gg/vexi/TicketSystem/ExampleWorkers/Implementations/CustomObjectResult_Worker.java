package gg.vexi.TicketSystem.ExampleWorkers.Implementations;

import gg.vexi.TicketSystem.Core.AbstractWorker;
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.annotations.AssociatedActionType;

@AssociatedActionType("CustomObjectResult_Worker")
public class CustomObjectResult_Worker extends AbstractWorker {

    public CustomObjectResult_Worker(Ticket Ticket) {
        super(Ticket);
    }

    @Override
    public void main() {
        super.setStatus(Status.PROCESSING);
    }
}
