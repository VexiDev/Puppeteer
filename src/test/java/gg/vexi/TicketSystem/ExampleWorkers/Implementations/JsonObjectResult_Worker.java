package gg.vexi.TicketSystem.ExampleWorkers.Implementations;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Core.AbstractWorker;
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.Exceptions.ExceptionRecord;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.annotations.AssociatedActionType;

@AssociatedActionType("JsonObjectResult_Worker")
public class JsonObjectResult_Worker extends AbstractWorker {

    private JsonObject data;

    public JsonObjectResult_Worker(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        super.setStatus(Status.PROCESSING);

        try {
            Thread.sleep(200);
            data = new JsonObject();
                data.addProperty("exampleProperty", 1987);
            super.complete(Status.SUCCESS, data);
        } catch (InterruptedException e) {
            super.recordException(new ExceptionRecord("InterruptedException", e.getMessage()));
        }
    }
}
