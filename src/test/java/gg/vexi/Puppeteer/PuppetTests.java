package gg.vexi.Puppeteer;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ExamplePuppets.ExampleObject;
import gg.vexi.Puppeteer.ExamplePuppets.ExamplePuppet_String;
import gg.vexi.Puppeteer.ExamplePuppets.ExamplePuppet_Object;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.Result;

class _Puppet {

    Ticket<String> ticket;

    @BeforeEach
    public void setup() {
        ticket = new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());
    }

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
         

        ExamplePuppet_String puppet = new ExamplePuppet_String(ticket);

        CountDownLatch latch = new CountDownLatch(1);

        CompletableFuture.runAsync(() -> {
            puppet.start();
            latch.countDown();
        });

        latch.await(50, TimeUnit.MILLISECONDS); // wait for puppet to start before checking
        assertEquals(PuppetStatus.PROCESSING, puppet.getStatus());

        assertEquals(PuppetStatus.PROCESSING, puppet.getStatus(), "Puppet status is not PROCESSING after start");
    }

    // tests the complete() method which takes a result status and data to build the TicketResult and completes its future with it
    @Test
    // @Disabled("Test not implemented yet")
    public void test_complete() {

        ExamplePuppet_String puppet = new ExamplePuppet_String(ticket);
        CompletableFuture<Result<String>> puppetFuture = puppet.getFuture();

        puppetFuture.thenAccept(actualResult -> {
            // build expected result
            Result<String> expectedResult = new Result<>("Test_Action Worker Data", ResultStatus.SUCCESS, new ProblemHandler());
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
        
        Ticket<ExampleObject> t = new Ticket<>("example_object_puppet", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());

        ExamplePuppet_Object puppet = new ExamplePuppet_Object(t);

        puppet.getFuture().thenAccept((ticketResult) -> {

            ExampleObject ticket_result_data = ticketResult.data();
            assertEquals("Showcase", ticket_result_data.data, "Value mismatch");
        });

        puppet.main();

    }

}
