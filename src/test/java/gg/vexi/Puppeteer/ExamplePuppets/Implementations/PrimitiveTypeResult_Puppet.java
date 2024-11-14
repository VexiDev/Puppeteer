package gg.vexi.Puppeteer.ExamplePuppets.Implementations;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;

public class PrimitiveTypeResult_Puppet extends Puppet {

    private int data;

    public PrimitiveTypeResult_Puppet(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {

        try {
            Thread.sleep(200);
            data = 3;
            super.complete(ResultStatus.SUCCESS, data);
        } catch (InterruptedException e) {
            super.complete(ResultStatus.ERROR_FAILED, null); 
        }
    }

}
