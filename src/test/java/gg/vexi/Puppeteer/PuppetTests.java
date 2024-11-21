package gg.vexi.Puppeteer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ExamplePuppets.ExampleObject;
import gg.vexi.Puppeteer.ExamplePuppets.ExamplePuppet_Object;
import gg.vexi.Puppeteer.ExamplePuppets.ExamplePuppet_String;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.Ticket.Result;
import gg.vexi.Puppeteer.Ticket.TicketPriority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class _Puppet {

    Ticket<String> ticket;

    @BeforeEach
    public void setup() {
        ticket =
            new Ticket<>("test_action", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>());
    }

    @Test
    @Disabled
    public void test_init() {
        //TODO: Implement tests for verifying Puppet initialization
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
            Result<String> expectedResult =
                new Result<>("Test_Action Worker Data", ResultStatus.SUCCESS, new ProblemHandler());
            assertEquals(expectedResult, actualResult, "Result mismatch");
        });

        // run the puppet
        puppet.main();

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            puppetFuture.join();
            assertEquals(
                PuppetStatus.COMPLETED, puppet.getStatus(), "Puppet Status is not COMPLETED after complete()"
            );
        }, "Puppet future took too long to complete (>5 seconds)");
    }

    @Test
    public void test_resultGenericType() {

        Ticket<ExampleObject> t = new Ticket<>(
            "example_object_puppet", TicketPriority.NORMAL, new ConcurrentHashMap<>(), new CompletableFuture<>()
        );

        ExamplePuppet_Object puppet = new ExamplePuppet_Object(t);

        puppet.getFuture().thenAccept((ticketResult) -> {
            ExampleObject ticket_result_data = ticketResult.data();
            assertEquals("Showcase", ticket_result_data.data, "Value mismatch");
        });

        puppet.main();
    }
}
