package gg.vexi.TicketSystem.Mock;

import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Worker.AbstractWorker;
import gg.vexi.TicketSystem.Worker.WorkerInterface;
import gg.vexi.TicketSystem.Worker.WorkerStatus;

public class MockWorker extends AbstractWorker implements WorkerInterface {

    public MockWorker(Ticket ticket) { super(ticket); }

    @Override
    public void start() {
        super.setStatus(WorkerStatus.PROCESSING);
    }

}
