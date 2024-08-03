package gg.vexi.TicketSystem.Mock;

import gg.vexi.TicketSystem.AbstractWorker;
import gg.vexi.TicketSystem.Status;
import gg.vexi.TicketSystem.Ticket.Ticket;

public class MockWorker extends AbstractWorker {

    public MockWorker(Ticket ticket) { super(ticket); }

    @Override
    public void start() {
        super.setStatus(Status.PROCESSING);
    }

}
