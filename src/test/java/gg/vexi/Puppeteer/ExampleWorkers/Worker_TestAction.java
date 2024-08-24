package gg.vexi.Puppeteer.ExampleWorkers;

import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.Core.AbstractWorker;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.annotations.RegisterWorker;

// THIS IS THE PLACEHOLDER WORKER FOR ALL GENERAL UNIT TESTS

// IMPLEMENTATION TESTS USE OUR EXAMPLE WORKERS

@RegisterWorker("test_action")
public class Worker_TestAction extends AbstractWorker {

    private String data;

    public Worker_TestAction(Ticket ticket) {
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
