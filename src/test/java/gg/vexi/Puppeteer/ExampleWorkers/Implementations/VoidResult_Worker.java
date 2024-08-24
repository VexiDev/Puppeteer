package gg.vexi.Puppeteer.ExampleWorkers.Implementations;

import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.Core.AbstractWorker;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.annotations.RegisterWorker;

@RegisterWorker
public class VoidResult_Worker extends AbstractWorker {

    private Object data;

    public VoidResult_Worker(Ticket ticket) {
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
