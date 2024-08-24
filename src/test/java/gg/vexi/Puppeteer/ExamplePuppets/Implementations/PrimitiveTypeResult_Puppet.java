package gg.vexi.Puppeteer.ExamplePuppets.Implementations;

import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.Core.AbstractPuppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;

@RegisterPuppet
public class PrimitiveTypeResult_Puppet extends AbstractPuppet {

    private int data;

    public PrimitiveTypeResult_Puppet(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {

        try {
            Thread.sleep(200);
            data = 3;
            super.complete(Status.SUCCESS, data);
        } catch (InterruptedException e) {
            super.recordException(new ExceptionRecord("InterruptedException", e.getMessage()));
        }
    }

}
