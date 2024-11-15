package gg.vexi.Puppeteer;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ExamplePuppets.ExampleObject;
import gg.vexi.Puppeteer.ExamplePuppets.Implementations.CustomObjectResult_Puppet;
import gg.vexi.Puppeteer.ExamplePuppets.Implementations.MapResult_Puppet;
import gg.vexi.Puppeteer.ExamplePuppets.Implementations.PrimitiveTypeResult_Puppet;
import gg.vexi.Puppeteer.ExamplePuppets.Implementations.VoidResult_Puppet;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.Result;

class _Puppet {

    Ticket ticket;

    @BeforeEach
    public void setup() {
        ticket = new Ticket("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());
    }

    // Since puppets are just going to be implementations of an abstract puppet class
    // As per my current understanding of testing abstract classes:
    // we should just mock what a puppet could be and ensure its has all expected attributes and methods
    @Test
    public void test_init() {

        // initialize mock puppets
        VoidResult_Puppet puppet_noGeneric = new VoidResult_Puppet(ticket);
        MapResult_Puppet puppet_jsonGeneric = new MapResult_Puppet(ticket);
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
            Result expectedResult = new Result(new ProblemHandler(), ticket, ResultStatus.SUCCESS, null);
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
    

    @Test
    public void test_resultGenericType() {
         
        CustomObjectResult_Puppet puppet = new CustomObjectResult_Puppet(ticket);

        puppet.getFuture().thenAccept((ticketResult) -> {

            assertTrue(ticketResult.getData() instanceof ExampleObject, "TicketResult data is not an instance of ExampleObject");
            // TODO: Implement generics typesafety and remove need to cast result data
            ExampleObject ticket_result_data = (ExampleObject) ticketResult.getData();
            assertEquals("Showcase", ticket_result_data.data, "Value mismatch");
        });

        puppet.main();

    }

}
