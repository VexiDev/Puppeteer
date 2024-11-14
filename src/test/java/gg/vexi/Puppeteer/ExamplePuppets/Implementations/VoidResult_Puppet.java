package gg.vexi.Puppeteer.ExamplePuppets.Implementations;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;

@RegisterPuppet
public class VoidResult_Puppet extends Puppet {

    private Object data;

    public VoidResult_Puppet(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        super.problemHandler.attempt(() -> {
            Thread.sleep(1000);
            data = null;
            if (data == null) throw new RuntimeException("test");
            super.complete(ResultStatus.SUCCESS, data);
        }, problem -> {
            super.complete(ResultStatus.ERROR_FAILED, null);
        });
    }


}
