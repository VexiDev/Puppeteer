package gg.vexi.Puppeteer.ExampleWorkers.Implementations;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.Core.AbstractWorker;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.annotations.RegisterWorker;

@RegisterWorker
public class JsonObjectResult_Worker extends AbstractWorker {

    private JsonObject data;

    public JsonObjectResult_Worker(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {

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
