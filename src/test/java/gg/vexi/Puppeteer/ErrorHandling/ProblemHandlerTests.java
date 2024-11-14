package gg.vexi.Puppeteer.ErrorHandling;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler.Problem;

//TODO: Make tests have good/bad path to better cover edge case handling

class _ProblemHandler {
    
    @Nested
    class _ProblemTests {

        @Test
        // NOTE: THIS TEST IS LOCATIONAL! 
        // - Moving the throwable definition line WILL break the location assertion
        //   since the line is hardcoded
        public void testInit() { 
            Throwable t = new Throwable("Testing ThrowableRecord");
            Problem problem = new Problem(t, "test_problem");
            
            // id
            assertNotNull(problem.getId(), "ID is null");
            assertEquals("test_problem", problem.getId(), "Value mismatch");
            
            // throwable
            assertNotNull(problem.getThrowable(), "Throwable is null"); 
            assertEquals(problem.getThrowable(), t, "Value mismatch");

            // timestamp
            assertNotNull(problem.getTimestamp(), "Timestamp cannot be null");
            // the exact timestamp is hard to validate so we just check if the
            // time difference is within 50ms and if it is its probably correct
            long now = Instant.now().toEpochMilli();
            long actual = problem.getTimestamp().toEpochMilli();
            assertTrue(now - actual < 50, "Epoch milli range too large");
            
            // threadname                                                       ,???
            // idk how to test this one tbh idk what the threadname would be T-T
            // gotta do some research
            assertNotNull(problem.getThreadName(), "Threadname is null");

            // location - where the throwable was created
            assertNotNull(problem.getLocation(), "Location is null");
            assertEquals(problem.getLocation(), 
                "gg.vexi.Puppeteer.ErrorHandling._ProblemHandler$_ProblemTests.testInit:27", 
                "Location mismatch");
        }

    }
    
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
            ph.execute(() -> { throw new RuntimeException("rt_exception"); });
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
        Optional<String> result = ph.attempt(() -> { throw new RuntimeException("rt_exception"); });

        assertEquals(1, ph.size(), "Record list size mismatch");

        String value = result.orElse("default");

        assertEquals("default", value, "Value mismatch");
    }

    @Test
    public void testAttemptWithDefault() {
        ProblemHandler ph = new ProblemHandler();
        String result = ph.attemptOrElse(() -> { throw new RuntimeException("rt_exception"); }, "default");

        assertEquals(1, ph.size(), "Record list size mismatch");

        assertEquals("default", result, "Value mismatch");
    }

    @Test
    public void testAttemptWithCustomHandler() {
        ProblemHandler ph = new ProblemHandler();

        String result = ph.attemptWith(
                () -> { throw new RuntimeException("rt_exception"); },
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

        for (int i = 1; i < count-1; i++) {
            Instant previous_time = recents.get(i - 1).getTimestamp();
            Instant current_time = recents.get(i).getTimestamp();
            assertTrue(0 <= previous_time.compareTo(current_time), "Timing mismatch");
        }

    }

    @Test
    public void testGetByType() {
        ProblemHandler ph = new ProblemHandler();

        for (int i = 1; i <= 3 ; i++) {
            ph.handle(new IndexOutOfBoundsException("iob_exception_" + i));
            ph.handle(new NullPointerException("np_exception_" + i));
            ph.handle(new StackOverflowError("so_error_" + i));
            if (i % 3 == 0)
                ph.handle(new IllegalAccessException("ia_exception_" + i));
        }

        assertEquals(10, ph.size(), "Size mismatch");

        List<Problem> typeList = ph.getByType(IndexOutOfBoundsException.class);

        assertEquals(3, typeList.size(), "Size mismatch");

        typeList = ph.getByType(IllegalAccessException.class);

        assertEquals(1, typeList.size(), "Size mismatch");

    }

}
