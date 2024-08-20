package gg.vexi.TicketSystem.ExampleWorkers.Implementations;

import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.Core.AbstractWorker;
import gg.vexi.TicketSystem.annotations.AssociatedActionType;
import gg.vexi.TicketSystem.ticket.Ticket;

@AssociatedActionType("JsonObjectResult_Worker")
public class JsonObjectResult_Worker extends AbstractWorker {

    public JsonObjectResult_Worker(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        super.setStatus(Status.PROCESSING);
    }
}
