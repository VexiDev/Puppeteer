package gg.vexi.Puppeteer.ExamplePuppets;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;

// THIS IS THE PLACEHOLDER WORKER FOR ALL GENERAL UNIT TESTS

// IMPLEMENTATION TESTS USE OUR EXAMPLE WORKERS

@RegisterPuppet("test_action")
public class Puppet_TestAction extends Puppet {

    private String data;

    public Puppet_TestAction(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {

        try {
            Thread.sleep(200);
            data = "Test_Action Worker Data";
            super.complete(ResultStatus.SUCCESS, data);
        } catch (InterruptedException e) {
            super.complete(ResultStatus.ERROR_FAILED, null); 
        }
    }
}
