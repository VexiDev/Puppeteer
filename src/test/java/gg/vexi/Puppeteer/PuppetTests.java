package gg.vexi.Puppeteer;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static gg.vexi.Puppeteer.TestUtils.this_method_does_nothing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ExamplePuppets.Implementations.CustomObjectResult_Puppet;
import gg.vexi.Puppeteer.ExamplePuppets.Implementations.JsonObjectResult_Puppet;
import gg.vexi.Puppeteer.ExamplePuppets.Implementations.PrimitiveTypeResult_Puppet;
import gg.vexi.Puppeteer.ExamplePuppets.Implementations.VoidResult_Puppet;
import gg.vexi.Puppeteer.Exceptions.ExceptionHandler;
import gg.vexi.Puppeteer.Exceptions.ExceptionRecord;
import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.Result;

class _Puppet {

    Ticket ticket;

    @BeforeEach
    public void setup() {
        ticket = new Ticket("test_action", TicketPriority.NORMAL, new JsonObject(), new CompletableFuture<>());
    }

    // Since puppets are just going to be implementations of an abstract puppet class
    // As per my current understanding of testing abstract classes:
    // we should just mock what a puppet could be and ensure its has all expected attributes and methods
    @Test
    public void test_init() {

        // initialize mock puppets
        VoidResult_Puppet puppet_noGeneric = new VoidResult_Puppet(ticket);
        JsonObjectResult_Puppet puppet_jsonGeneric = new JsonObjectResult_Puppet(ticket);
        CustomObjectResult_Puppet puppet_objectGeneric = new CustomObjectResult_Puppet(ticket);
        PrimitiveTypeResult_Puppet puppet_primitiveGeneric = new PrimitiveTypeResult_Puppet(ticket);

        List<Puppet> puppets = List.<Puppet>of(
                puppet_noGeneric,
                puppet_jsonGeneric,
                puppet_objectGeneric,
                puppet_primitiveGeneric
        );

        // the test loop for puppet types
        for (Puppet puppet : puppets) {

            // verify default puppet attributes of explicit type are not null
            assertNotNull(puppet.getTicket(), "MockPuppet associated Ticket is null");
            assertNotNull(puppet.getFuture(), "MockPuppet future is null");
            assertNotNull(puppet.getStatus(), "MockPuppet status is null");

            // verify default status for puppet is ready by default
            assertEquals(PuppetStatus.READY, puppet.getStatus(), "MockPuppet default status is not READY");
        }

    }

    @Test
    public void test_start() throws InterruptedException {

        VoidResult_Puppet puppet = new VoidResult_Puppet(ticket);

        CountDownLatch latch = new CountDownLatch(1);

        CompletableFuture.runAsync(() -> {
            puppet.start();
            latch.countDown();
        });

        latch.await(500, TimeUnit.MILLISECONDS);
        assertEquals(PuppetStatus.PROCESSING, puppet.getStatus());

        assertEquals(PuppetStatus.PROCESSING, puppet.getStatus(), "Void Puppet status is not PROCESSING after start");
    }

    // tests the complete() method which takes a result status and data to build the TicketResult and completes its future with it
    @Test
    // @Disabled("Test not implemented yet")
    public void test_complete() {

        VoidResult_Puppet puppet = new VoidResult_Puppet(ticket);
        CompletableFuture<Result> puppetFuture = puppet.getFuture();

        puppetFuture.thenAccept(actualResult -> {
            // build expected result
            Result expectedResult = new Result(new ExceptionHandler(), ticket, ResultStatus.SUCCESS, null);
            assertEquals(expectedResult, actualResult, "Result mismatch");
        });

        // run the puppet
        puppet.main();
        
        assertTimeoutPreemptively(
            Duration.ofSeconds(5),
            () -> {
                puppetFuture.join();
                assertEquals(PuppetStatus.COMPLETED, puppet.getStatus(), "Puppet Status is not COMPLETED after complete()");
            },
            "Puppet future took too long to complete (>5 seconds)"
        );
    }

    // tests the recordException() method which is just a wrapper of CaughtExceptions.add()
    @Test
    public void test_recordException() throws NoSuchFieldException, IllegalArgumentException, IllegalArgumentException, IllegalAccessException {
        
        VoidResult_Puppet puppet = new VoidResult_Puppet(ticket);

        Field exception_object_field = puppet.getClass().getSuperclass().getDeclaredField("exceptionHandler");
        exception_object_field.setAccessible(true);
        ExceptionHandler exceptions_holder = (ExceptionHandler) exception_object_field.get(puppet);

        assertTrue(exceptions_holder.getAll().isEmpty(), "Exceptions list is not empty on init");

        ExceptionRecord record = new ExceptionRecord("ErrorTest_001", "This is a test record for the test_recordExceptions() method");
        puppet.recordException(record);

        assertFalse(exceptions_holder.getAll().isEmpty(), "Exceptions list is empty after using recordException()");
        assertTrue(exceptions_holder.getAll().size() == 1, "Exceptions list has incorrect list size");
        assertEquals("ErrorTest_001", exceptions_holder.getAll().get(0).getType(), "Mismatched type value in record at index 0");
    }

    @Test
    public void test_resultGenericType() {

        CustomObjectResult_Puppet puppet = new CustomObjectResult_Puppet(ticket);
        ExceptionRecord expected_data = new ExceptionRecord("ExampleRecord_06302005", "This is an example record for the test_completeSuccess() unit test");

        puppet.getFuture().thenAccept((ticketResult) -> {

            assertTrue(ticketResult.getData() instanceof ExceptionRecord, "TicketResult data is not an instance of ExceptionRecord");
            ExceptionRecord ticket_result_data = (ExceptionRecord) ticketResult.getData();
            assertTrue(ticket_result_data.getType().equals("ExampleRecord_06302005"), "TicketResult ExceptionRecord Object does not have the correct exception type");

        });

        puppet.main();

        // this should not be possible because complete() should only be accessible from within the puppet
        this_method_does_nothing(expected_data); // <-- temp
        // puppet.complete(Status.SUCCESS, expected_data); // <-- errors with "protected access" (need to learn more about the __protected__ access level)

    }

}
