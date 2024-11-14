package gg.vexi.Puppeteer.ExamplePuppets.Implementations;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ExamplePuppets.ExampleObject;

public class CustomObjectResult_Puppet extends Puppet {

    private ExampleObject data;

    public CustomObjectResult_Puppet(Ticket Ticket) {
        super(Ticket);
    }

    @Override
    public void main() {

        try {
            Thread.sleep(200);
            data = new ExampleObject("Showcase");
            super.complete(ResultStatus.SUCCESS, data);
        } catch (InterruptedException e) {
            super.complete(ResultStatus.ERROR_FAILED, null); 
        }
    }
}
