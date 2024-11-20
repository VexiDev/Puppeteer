package gg.vexi.Puppeteer.ExamplePuppets;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;

public class ExamplePuppet_Object extends Puppet<ExampleObject> {

    private ExampleObject data;

    public ExamplePuppet_Object(Ticket<ExampleObject> Ticket) {
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
