package gg.vexi.Puppeteer.ErrorHandling;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import gg.vexi.Puppeteer.TestUtils;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler.Problem;

//TODO: Make tests have good/bad path to better cover edge case handling

class _ProblemHandler {

    @Test
    public void testInit() {
        ProblemHandler ph = new ProblemHandler();
        assertEquals(1000, ph.maxSize(), "Size mismatch, default max size is not 1000");
        ph = new ProblemHandler(1);
        assertEquals(1, ph.maxSize(), "Size mismatch, maxSize does not match passed value");
    }

    @Test
    public void testHandle() {
        ProblemHandler ph = new ProblemHandler(1);
        Exception testException = new RuntimeException("rt_exception");

        Problem problem;
        String actual;

        // throw an exception and handle it in catch
        try {
            if (true)
                throw testException;
        } catch (Exception e) {
            ph.handle(e);
        }

        assertEquals(1, ph.size(), "Record list size mismatch");

        problem = ph.getAll().get(0);

        actual = problem.getId().split("-")[1];
        assertEquals(testException.hashCode(), Integer.parseInt(actual), "Hash mismatch");

        actual = problem.getThrowable().getMessage();
        assertEquals("rt_exception", actual, "Message mismatch");

        // TODO: Add tests for overflow culling in handle method

    }

    @Test
    public void testExecute() {
        ProblemHandler ph = new ProblemHandler();
        Exception exception = null;
        Problem problem;
        String actual;

        try {
            String result = ph.execute(() -> "test");
        } catch (Exception e) {
            exception = e;
        }

        assertEquals(1, ph.size(), "Record list size mismatch");
        problem = ph.getAll().get(0);

        actual = problem.getId().split("-")[1];

        assertNotNull(exception, "Null exception");
        assertEquals(exception.hashCode(), Integer.parseInt(actual), "Hash mismatch");

        actual = problem.getThrowable().getMessage();
        assertEquals("rt_exception", actual, "Message mismatch");
    }

    @Test
    public void testAttempt() {
        ProblemHandler ph = new ProblemHandler();
        Optional<String> result = ph.attempt(() -> "test");

        assertEquals(1, ph.size(), "Record list size mismatch");

        String value = result.orElse("default");

        assertEquals("default", value, "Value mismatch");
    }

    @Test
    public void testAttemptWithDefault() {
        ProblemHandler ph = new ProblemHandler();
        String result = ph.attemptOrElse(() -> "test", "default");

        assertEquals(1, ph.size(), "Record list size mismatch");

        assertEquals("default", result, "Value mismatch");
    }

    @Test
    public void testAttemptWithCustomHandler() {
        ProblemHandler ph = new ProblemHandler();

        String result = ph.attemptWith(
                () -> "test",
                record -> {
                    return "error-value";
                });

        assertEquals("error-value", result, "Value mismatch");
    }

    @Test
    public void testGetRecent() {
        ProblemHandler ph = new ProblemHandler();

        int count = 5;

        for (int i = 1; i <= count; i++) {
            ph.handle(new RuntimeException("rt_exception_" + i));
        }

        List<Problem> recents = ph.getRecent(count - 1);

        assertEquals(count - 1, recents.size(), "Size mismatch");

        for (int i = 1; i <= count - 1; i++) {
            Instant previous_time = recents.get(i - 1).getTimestamp();
            Instant current_time = recents.get(i).getTimestamp();
            assertTrue(0 >= previous_time.compareTo(current_time));
        }

    }

    @Test
    public void testGetByType() {
        ProblemHandler ph = new ProblemHandler();

        // count must be greater than 3 & divisible by 3
        int count = 9;

        assertFalse(count <= 3, "count is <= 3");
        assertTrue(count % 3 == 0, "count is not divisible by 3");

        for (int i = 1; i <= count / 3; i++) {
            ph.handle(new RuntimeException("rt_exception_" + i));
            ph.handle(new NullPointerException("np_exception_" + i));
            ph.handle(new StackOverflowError("so_error_" + i));
            if (i % 3 == 0)
                ph.handle(new IllegalAccessException("ia_exception_" + i));
        }

        assertEquals(count / 3 + ((count / 3) / 3), ph.size(), "Size mismatch");

        List<Problem> typeList = ph.getByType(RuntimeException.class);

        assertEquals(count / 3, typeList.size(), "Size mismatch");

        typeList = ph.getByType(IllegalAccessException.class);

        assertEquals((count / 3 / 3), typeList.size(), "Size mismatch");

    }

    @Nested
    class _ProblemTests {

        @Test
        public void testInit() {
            Throwable t = new Throwable("Testing ThrowableRecord");
            Problem problem = new Problem(t, "test-throwable");
            
            // id
            assertNotNull(problem.getId(), "ID is null");
            assertEquals(problem.hashCode(), problem.getId(), "Value mismatch");
            
            // throwable
            assertNotNull(problem.getThrowable(), "Throwable is null"); 
            assertEquals(problem.getThrowable(), t, "Value mismatch");

            // timestamp
            assertNotNull(problem.getTimestamp(), "Timestamp cannot be null");
            // the exact timestamp is hard to validate so 
            // we do a Epoch day check on the two intervals
            // EPOCH_DAY is based on the Java epoch of 1970-01-01 (ISO)
            Instant now = Instant.now();
            Instant actual = problem.getTimestamp();
            assertEquals(now.get(ChronoField.EPOCH_DAY), actual.get(ChronoField.EPOCH_DAY),
                        "Epoch day mistmatch (try running the build again just in case!)");
            
            // threadname
            assertNotNull(problem.getThreadName(), "Threadname is null");//     ,???
            // idk how to test this one tbh idk what the threadname would be ;-;
            // gotta do some research

            // location
            assertNotNull(problem.getLocation(), "Location is null");
            assertEquals(problem.getLocation(), "_ProblemTests.testInit.205");

        }

    }

}
