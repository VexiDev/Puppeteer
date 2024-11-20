package gg.vexi.Puppeteer.ExamplePuppets;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;

// THIS IS THE PLACEHOLDER WORKER FOR ALL GENERAL UNIT TESTS

// IMPLEMENTATION TESTS USE OUR EXAMPLE WORKERS

public class ExamplePuppet_String extends Puppet<String> {

    private String data;

    public ExamplePuppet_String(Ticket<String> ticket) {
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