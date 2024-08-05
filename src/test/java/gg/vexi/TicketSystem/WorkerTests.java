package gg.vexi.TicketSystem;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
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

        // run the worker
        worker.main();

        assertEquals(Status.PROCESSING, worker.getStatus(), "MockWorker status is not PROCESSING after start");
    }

    // tests the complete() method which takes a result status, data, caughtexceptions and the original ticket to build the TicketResult
    @Test
    public void test_complete() {
        fail("TEST NOT IMPLEMENTED");
    }

    // tests the recordException() method which is just a wrapper of CaughtExceptions.add()
    @Test
    public void test_recordException() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {
        

        Worker_noGeneric worker = new Worker_noGeneric(ticket);

        Field exception_object_field = worker.getClass().getSuperclass().getDeclaredField("exceptionHandler");
        exception_object_field.setAccessible(true);
        CaughtExceptions exceptions_holder = (CaughtExceptions) exception_object_field.get(worker);

        assertTrue(exceptions_holder.getAll().isEmpty(), "Exceptions list is not empty on init");

        ExceptionRecord record = new ExceptionRecord("ErrorTest_001", "This is a test record for the recordExceptions() method");
        worker.recordException(record);

        assertFalse(exceptions_holder.getAll().isEmpty(), "Exceptions list is empty after using recordException()");
        assertTrue(exceptions_holder.getAll().size() == 1, "Exceptions list has incorrect list size");
        assertEquals("ErrorTest_001", exceptions_holder.getAll().get(0).getType(), "Mismatched type value in record at index 0");

    }

    @Test
    public void test_resultGenericType() {

        Worker_objectGeneric worker = new Worker_objectGeneric(ticket);
        ExceptionRecord expected_data = new ExceptionRecord("ExampleRecord_06302005", "This is an example record for the test_completeSuccess() unit test");

        worker.getFuture().thenAccept((ticketResult) -> {

            assertTrue(ticketResult.getData() instanceof ExceptionRecord, "TicketResult data is not an instance of ExceptionRecord");
            assertTrue(ticketResult.getData().getType().equals("ExampleRecord_06302005"), "TicketResult ExceptionRecord Object does not have the correct exception type");

        });

        worker.main();

        // this should not be possible because complete() should only be accessible from within the worker
        worker.complete(Status.SUCCESS, expected_data);

    }

}
