package gg.vexi.TicketSystem;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Core.AbstractWorker;
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.ExampleWorkers.Implementations.CustomObjectResult_Worker;
import gg.vexi.TicketSystem.ExampleWorkers.Implementations.JsonObjectResult_Worker;
import gg.vexi.TicketSystem.ExampleWorkers.Implementations.PrimitiveTypeResult_Worker;
import gg.vexi.TicketSystem.ExampleWorkers.Implementations.VoidResult_Worker;
import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
import gg.vexi.TicketSystem.Exceptions.ExceptionRecord;
import static gg.vexi.TicketSystem.TestUtils.this_method_does_nothing;
import gg.vexi.TicketSystem.Ticket.TicketPriority;
import gg.vexi.TicketSystem.Ticket.TicketResult;

class _Worker {

    Ticket ticket;

    @BeforeEach
    public void setup() {
        ticket = new Ticket("test_action", TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
    }

    // Since workers are just going to be implementations of an abstract worker class
    // As per my current understanding of testing abstract classes:
    // we should just mock what a worker could be and ensure its has all expected attributes and methods
    @Test
    public void test_init() {

        // initialize mock workers
        VoidResult_Worker worker_noGeneric = new VoidResult_Worker(ticket);
        JsonObjectResult_Worker worker_jsonGeneric = new JsonObjectResult_Worker(ticket);
        CustomObjectResult_Worker worker_objectGeneric = new CustomObjectResult_Worker(ticket);
        PrimitiveTypeResult_Worker worker_primitiveGeneric = new PrimitiveTypeResult_Worker(ticket);

        List<AbstractWorker> workers = List.<AbstractWorker>of(
                worker_noGeneric,
                worker_jsonGeneric,
                worker_objectGeneric,
                worker_primitiveGeneric
        );

        // the test loop for worker types
        for (AbstractWorker worker : workers) {

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

        VoidResult_Worker worker = new VoidResult_Worker(ticket);

        // run the worker
        worker.main();

        assertEquals(Status.PROCESSING, worker.getStatus(), "MockWorker status is not PROCESSING after start");
    }

    // tests the complete() method which takes a result status and data to build the TicketResult and completes its future with it
    @Test
    // @Disabled("Test not implemented yet")
    public void test_complete() {

        VoidResult_Worker worker = new VoidResult_Worker(ticket);
        CompletableFuture<TicketResult> workerFuture = worker.getFuture();

        workerFuture.thenAccept(actualResult -> {
            // build expected result
            TicketResult expectedResult = new TicketResult(new CaughtExceptions(), ticket, Status.SUCCESS, null);
            assertEquals(expectedResult, actualResult, "Result mismatch");
        });

        // run the worker
        worker.main();
        
        assertTimeoutPreemptively(
            Duration.ofSeconds(5),
            () -> {
                workerFuture.join();
                assertEquals(Status.COMPLETED, worker.getStatus(), "Worker Status is not COMPLETED after complete()");
            },
            "Worker future took too long to complete (>5 seconds)"
        );
    }

    // tests the recordException() method which is just a wrapper of CaughtExceptions.add()
    @Test
    public void test_recordException() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {
        
        VoidResult_Worker worker = new VoidResult_Worker(ticket);

        Field exception_object_field = worker.getClass().getSuperclass().getDeclaredField("exceptionHandler");
        exception_object_field.setAccessible(true);
        CaughtExceptions exceptions_holder = (CaughtExceptions) exception_object_field.get(worker);

        assertTrue(exceptions_holder.getAll().isEmpty(), "Exceptions list is not empty on init");

        ExceptionRecord record = new ExceptionRecord("ErrorTest_001", "This is a test record for the test_recordExceptions() method");
        worker.recordException(record);

        assertFalse(exceptions_holder.getAll().isEmpty(), "Exceptions list is empty after using recordException()");
        assertTrue(exceptions_holder.getAll().size() == 1, "Exceptions list has incorrect list size");
        assertEquals("ErrorTest_001", exceptions_holder.getAll().get(0).getType(), "Mismatched type value in record at index 0");
    }

    @Test
    public void test_resultGenericType() {

        CustomObjectResult_Worker worker = new CustomObjectResult_Worker(ticket);
        ExceptionRecord expected_data = new ExceptionRecord("ExampleRecord_06302005", "This is an example record for the test_completeSuccess() unit test");

        worker.getFuture().thenAccept((ticketResult) -> {

            assertTrue(ticketResult.getData() instanceof ExceptionRecord, "TicketResult data is not an instance of ExceptionRecord");
            ExceptionRecord ticket_result_data = (ExceptionRecord) ticketResult.getData();
            assertTrue(ticket_result_data.getType().equals("ExampleRecord_06302005"), "TicketResult ExceptionRecord Object does not have the correct exception type");

        });

        worker.main();

        // this should not be possible because complete() should only be accessible from within the worker
        this_method_does_nothing(expected_data); // <-- temp
        // worker.complete(Status.SUCCESS, expected_data); // <-- errors with "protected access" (need to learn more about the __protected__ access level)

    }

}
