package gg.vexi.Puppeteer.ExamplePuppets.Implementations;

import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.Core.AbstractPuppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;

@RegisterPuppet
public class VoidResult_Puppet extends AbstractPuppet {

    private Object data;

    public VoidResult_Puppet(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {

        try {
            Thread.sleep(1000);
            data = null;
            super.complete(Status.SUCCESS, data);
        } catch (InterruptedException e) {
            super.recordException(new ExceptionRecord("InterruptedException", e.getMessage()));
        }
    }


}
