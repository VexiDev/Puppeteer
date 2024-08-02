package gg.vexi.TicketSystem;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Mock.MockWorker;
import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketPriority;
import gg.vexi.TicketSystem.Worker.WorkerStatus;

class WorkerTests {

    private MockWorker worker;

    @BeforeEach
    public void setup() {
        Ticket ticket = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
        worker = new MockWorker(ticket);
    }

    // Since workers are just going to be implementations of an abstract worker class
    // As per my current understanding of testing abstract classes:
    // we should just mock what a worker could be and ensure its has all expected attributes and methods
    @Test
    public void test_init() {

        // verify default worker attributes are set
        assertNotNull(worker.getTicket(), "MockWorker associated Ticket is null");
        assertNotNull(worker.getFuture(), "MockWorker future is null");
        assertNotNull(worker.getStatus(), "MockWorker status is null");

        // verify default status for worker is ready
        assertEquals(WorkerStatus.READY, worker.getStatus(), "MockWorker default status is not READY");
    }


    @Test
    public void test_start() {

        // start the worker
        worker.start();

        assertEquals(WorkerStatus.PROCESSING, worker.getStatus(), "MockWorker status is not PROCESSING after start");    
    }

}
