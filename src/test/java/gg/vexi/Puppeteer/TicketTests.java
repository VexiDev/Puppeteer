package gg.vexi.Puppeteer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static gg.vexi.Puppeteer.TestUtils.assertMapEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.Result;

class _Ticket {

    private Ticket ticket;

    @BeforeEach
    public void setup() {
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put("setting_example", true);
        ticket = new Ticket("test_action", TicketPriority.NORMAL, parameters, new CompletableFuture<>());
    }

    @Test
    public void test_init() {
        // check if ticket is initialized correctly
        
        // create expected objects
        Map<String, Object>  expected_parameters = new ConcurrentHashMap<>();
        expected_parameters.put("setting_example", true);

        // verify ticket exists
        assertNotNull(ticket, "Ticket is Null");

        // verify id exists
        assertNotNull(ticket.getId(), "Ticket ID is Null");
        
        // verify type
        assertNotNull(ticket.getType(), "Ticket has no action type");
        assertTrue(ticket.getType() instanceof String, "Ticket does not have a valid action type");

        // verify priority
        assertNotNull(ticket.getPriority(), "Ticket has no priority level");
        assertTrue(ticket.getPriority() instanceof TicketPriority, "Ticket does not have a valid priority level");

        // verify parameters
        assertNotNull(ticket.getParameters(), "Ticket has no action type");
        assertMapEquals(expected_parameters, ticket.getParameters());

        // verify future
        assertNotNull(ticket.getFuture(), "Ticket has no future");
        assertTrue(ticket.getFuture() instanceof CompletableFuture<Result>);

    }

    // Result is the object returned by a puppet to Puppeteer to finish processing the ticket
    @Nested
    class _Result { 
        
        @Test
        public void test_init() {

            Result Result = new Result(new ProblemHandler(), ticket, ResultStatus.SUCCESS, null);

            assertNotNull(Result, "Result is null");
            assertNotNull(Result.getTicket(), "Result associated ticket is null");
            assertNotNull(Result.getResultStatus(), "Result status enum (status code) is null");
            assertNotNull(Result.getProblemsHandler(), "Result ProblemHandler is null");
        }

        @Test
        // @Disabled("Test not implemented") 
        public void test_isSuccessful() {

            Result Result = new Result(new ProblemHandler(), ticket, ResultStatus.SUCCESS, null);

            boolean isSuccessful = Result.isSuccessful();

            assertNotNull(isSuccessful, "Result returned null when checking status");
            assertTrue(isSuccessful, "Result was not successful");

        }

        @Test
        // @Disabled("Test not implemented")
        public void test_hasExceptions() {

            Result Result = new Result(new ProblemHandler(), ticket, ResultStatus.FAILED, null);

            boolean hasExceptions = Result.hasExceptions();

            assertNotNull(hasExceptions, "Result returned null when checking exceptions");
            assertFalse(hasExceptions, "Result has exceptions");
        }
    }
}
