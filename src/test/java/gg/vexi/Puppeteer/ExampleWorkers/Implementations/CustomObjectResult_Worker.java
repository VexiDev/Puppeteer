package gg.vexi.Puppeteer.ExampleWorkers.Implementations;

import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.Core.AbstractWorker;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.annotations.RegisterWorker;

@RegisterWorker
public class CustomObjectResult_Worker extends AbstractWorker {

    private ExceptionRecord data;

    public CustomObjectResult_Worker(Ticket Ticket) {
        super(Ticket);
    }

    @Override
    public void main() {

        try {
            Thread.sleep(200);
            data = new ExceptionRecord("CustomObjectResult", "This is an instance of ExceptionRecord to test worker's returning custom objects");
            super.complete(Status.SUCCESS, data);
        } catch (InterruptedException e) {
            super.recordException(new ExceptionRecord("InterruptedException", e.getMessage()));
        }
    }
}
