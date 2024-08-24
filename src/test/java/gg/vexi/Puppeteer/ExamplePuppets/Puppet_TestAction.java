package gg.vexi.Puppeteer.ExamplePuppets;

import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.Core.AbstractPuppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;

// THIS IS THE PLACEHOLDER WORKER FOR ALL GENERAL UNIT TESTS

// IMPLEMENTATION TESTS USE OUR EXAMPLE WORKERS

@RegisterPuppet("test_action")
public class Puppet_TestAction extends AbstractPuppet {

    private String data;

    public Puppet_TestAction(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {

        try {
            Thread.sleep(200);
            data = "Test_Action Worker Data";
            super.complete(Status.SUCCESS, data);
        } catch (InterruptedException e) {
            super.recordException(new ExceptionRecord("InterruptedException", e.getMessage()));
        }
    }
}
