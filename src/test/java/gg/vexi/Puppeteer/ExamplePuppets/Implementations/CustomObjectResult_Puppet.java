package gg.vexi.Puppeteer.ExamplePuppets.Implementations;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;

@RegisterPuppet
public class CustomObjectResult_Puppet extends Puppet {

    private ExceptionRecord data;

    public CustomObjectResult_Puppet(Ticket Ticket) {
        super(Ticket);
    }

    @Override
    public void main() {

        try {
            Thread.sleep(200);
            data = new ExceptionRecord("CustomObjectResult", "This is an instance of ExceptionRecord to test worker's returning custom objects");
            super.complete(ResultStatus.SUCCESS, data);
        } catch (InterruptedException e) {
            super.recordException(new ExceptionRecord("InterruptedException", e.getMessage()));
        }
    }
}
