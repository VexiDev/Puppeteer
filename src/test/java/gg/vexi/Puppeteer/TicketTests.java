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

    private Ticket<String> ticket;

    @BeforeEach
    public void setup() {
        Map<String, Object> parameters = new ConcurrentHashMap<>();
        parameters.put("setting_example", true);
        ticket = new Ticket<>("test_action", TicketPriority.NORMAL, parameters, new CompletableFuture<>());
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
        assertNotNull(ticket.id(), "Ticket ID is Null");
        
        // verify type
        assertNotNull(ticket.puppet(), "Ticket has no action type");
        assertTrue(ticket.puppet() instanceof String, "Ticket does not have a valid action type");

        // verify priority
        assertNotNull(ticket.priority(), "Ticket has no priority level");
        assertTrue(ticket.priority() instanceof TicketPriority, "Ticket does not have a valid priority level");

        // verify parameters
        assertNotNull(ticket.parameters(), "Ticket has no action type");
        assertMapEquals(expected_parameters, ticket.parameters());

        // verify future
        assertNotNull(ticket.future(), "Ticket has no future");
        assertTrue(ticket.future() instanceof CompletableFuture<Result<String>>);

    }

    // Result is the object returned by a puppet to Puppeteer to finish processing the ticket
    @Nested
    class _Result { 
        
        @Test
        public void test_init() {

            Result<String> Result = new Result<>(null, ResultStatus.SUCCESS, new ProblemHandler());

            assertNotNull(Result, "Result is null");
            assertNotNull(Result.status(), "Result status enum (status code) is null");
            assertNotNull(Result.problemHandler(), "Result ProblemHandler is null");
        }

        @Test
        // @Disabled("Test not implemented") 
        public void test_isSuccessful() {

            Result<String> Result = new Result<>(null, ResultStatus.SUCCESS, new ProblemHandler());

            boolean isSuccessful = Result.isSuccessful();

            assertNotNull(isSuccessful, "Result returned null when checking status");
            assertTrue(isSuccessful, "Result was not successful");

        }

        @Test
        // @Disabled("Test not implemented")
        public void test_hasExceptions() {

            Result<String> Result = new Result<>(null, ResultStatus.FAILED, new ProblemHandler());

            boolean hasExceptions = Result.hasExceptions();

            assertNotNull(hasExceptions, "Result returned null when checking exceptions");
            assertFalse(hasExceptions, "Result has exceptions");
        }
    }
}
