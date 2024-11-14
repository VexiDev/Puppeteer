package gg.vexi.Puppeteer.ExamplePuppets.Implementations;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;

@RegisterPuppet
public class JsonObjectResult_Puppet extends Puppet {

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
            super.complete(ResultStatus.SUCCESS, data);
        } catch (InterruptedException e) {
            super.complete(ResultStatus.ERROR_FAILED, null); 
        }
    }
}
