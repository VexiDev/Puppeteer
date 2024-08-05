package gg.vexi.TicketSystem.Mocks.MockWorkers;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.AbstractWorker;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.Ticket.Ticket;

public class Worker_jsonGeneric extends AbstractWorker<JsonObject> {

    public Worker_jsonGeneric(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        super.setStatus(Status.PROCESSING);
    }
}
