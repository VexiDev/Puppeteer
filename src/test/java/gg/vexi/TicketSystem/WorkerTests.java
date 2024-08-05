package gg.vexi.TicketSystem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Exceptions.ExceptionRecord;
import gg.vexi.TicketSystem.Mocks.MockWorkers.Worker_jsonGeneric;
import gg.vexi.TicketSystem.Mocks.MockWorkers.Worker_noGeneric;
import gg.vexi.TicketSystem.Mocks.MockWorkers.Worker_objectGeneric;
import gg.vexi.TicketSystem.Mocks.MockWorkers.Worker_primitiveGeneric;
import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketPriority;

class _Worker {

    Ticket ticket;

    @BeforeEach
    public void setup() {
        ticket = new Ticket(ActionType.ACTION, TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
    }

    // Since workers are just going to be implementations of an abstract worker class
    // As per my current understanding of testing abstract classes:
    // we should just mock what a worker could be and ensure its has all expected attributes and methods
    @Test
    public void test_init() {

        // initialize mock workers
        Worker_noGeneric worker_noGeneric = new Worker_noGeneric(ticket);
        Worker_jsonGeneric worker_jsonGeneric = new Worker_jsonGeneric(ticket);
        Worker_objectGeneric worker_objectGeneric = new Worker_objectGeneric(ticket);
        Worker_primitiveGeneric worker_primitiveGeneric = new Worker_primitiveGeneric(ticket);

        List<AbstractWorker<?>> workers = List.<AbstractWorker<?>>of(
            worker_noGeneric,
            worker_jsonGeneric,
            worker_objectGeneric,
            worker_primitiveGeneric
        );

        // the test loop for worker types
        for (AbstractWorker<?> worker : workers) {
            
            // verify default worker attributes of explicit type are not null
            assertNotNull(worker.getTicket(), "MockWorker associated Ticket is null");
            assertNotNull(worker.getFuture(), "MockWorker future is null");
            assertNotNull(worker.getStatus(), "MockWorker status is null");
            
            // verify default status for worker is ready by default
            assertEquals(Status.READY, worker.getStatus(), "MockWorker default status is not READY");
        }

    }

    @Test
    public void test_start() {

        Worker_noGeneric worker = new Worker_noGeneric(ticket);

        // start the worker
        worker.start();

        assertEquals(Status.PROCESSING, worker.getStatus(), "MockWorker status is not PROCESSING after start");    
    }

    @Test
    public void test_resultGenericType() {

        Worker_objectGeneric worker = new Worker_objectGeneric(ticket);
        ExceptionRecord expected_data = new ExceptionRecord("ExampleRecord_06302005", "This is an example record for the test_completeSuccess() unit test");

        worker.getFuture().thenAccept((ticketResult) -> {

            assertTrue(ticketResult.getData() instanceof ExceptionRecord, "TicketResult data is not an instance of ExceptionRecord");
            assertTrue(ticketResult.getData().getType().equals("ExampleRecord_06302005"), "TicketResult ExceptionRecord Object does not have the correct exception type");

        });

        worker.start();
        worker.completeSuccess(expected_data);

    }

}
