package gg.vexi.Puppeteer.ExamplePuppets.Implementations;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.Core.AbstractPuppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;

@RegisterPuppet
public class JsonObjectResult_Puppet extends AbstractPuppet {

    private JsonObject data;

    public JsonObjectResult_Puppet(Ticket ticket) {
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
