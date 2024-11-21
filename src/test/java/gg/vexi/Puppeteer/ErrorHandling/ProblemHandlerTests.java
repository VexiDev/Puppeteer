package gg.vexi.Puppeteer.ErrorHandling;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import gg.vexi.Puppeteer.Exceptions.Problem;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

//TODO: Make tests have good/bad path to better cover edge case handling

class _ProblemHandler {

    @Nested
    class _ProblemTests {

        @Test
        public void testInit() {
            Throwable t = new Throwable("Testing ThrowableRecord");
            Problem problem = new Problem(t, "test_problem");

            // id
            assertNotNull(problem.id(), "ID is null");
            assertEquals("test_problem", problem.id(), "Value mismatch");

            // throwable
            assertNotNull(problem.get(), "Throwable is null");
            assertEquals(problem.get(), t, "Value mismatch");

            // timestamp
            assertNotNull(problem.instant(), "Timestamp cannot be null");
            // the exact timestamp is hard to validate so we just check if the
            // time difference is within 10ms and if it is its probably correct
            long now = Instant.now().toEpochMilli();
            long actual = problem.instant().toEpochMilli();
            assertTrue(now - actual < 10, "Epoch milli range too large");

            // threadname ,???
            // idk how to test this one tbh idk what the threadname would be T-T
            // gotta do some research
            assertNotNull(problem.threadName(), "Threadname is null");

            // location - where the throwable was created
            assertNotNull(problem.location(), "Location is null");
            // only check that its the correct method to avoid code locational tests
            // normally includes line numbers
            assertEquals(
                problem.location().substring(0, problem.location().indexOf(":")),
                "gg.vexi.Puppeteer.ErrorHandling._ProblemHandler$_ProblemTests.testInit",
                "Location mismatch"
            );
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
        ProblemHandler ph = new ProblemHandler(2);
        Exception testException1 = new RuntimeException("rt_exception");
        Exception testException2 = new RuntimeException("rt_exception_2");

        Problem problem;
        String actual;

        // throw exception 1 & 2 and handle them in catch

        try {
            if ( true ) throw testException1;
        } catch ( Exception e ) { ph.handle(e); }

        assertEquals(1, ph.size(), "Record list size mismatch");
        problem = ph.getAll().get(0);

        actual = problem.id().split("-")[1];
        assertEquals(testException1.hashCode(), Integer.parseInt(actual), "Hash mismatch");

        actual = problem.get().getMessage();
        assertEquals("rt_exception", actual, "Message mismatch");

        // TODO: Add tests for overflow culling in handle method
        Exception testException3 = new RuntimeException("rt_exception_3");

        try {
            if ( true ) throw testException2;
        } catch ( Exception e ) { ph.handle(e); }

        try {
            // delay 3rd exception so index 0 is always 2nd
            Thread.sleep(100);
            if ( true ) throw testException3;
        } catch ( Exception e ) { ph.handle(e); }

        for ( Problem p : ph.getRecent(2) ) {
            System.out.println(p);
            System.out.println(p.instant());
        }

        assertEquals(2, ph.size(), "Record list size mismatch");

        // most recent would be index 0 and should be exception 2 since exception 1 got purged
        problem = ph.getRecent(1).get(0);
        assertEquals(testException2, problem.get(), "Object mismatch");
    }

    @Test
    public void testExecute() {
        ProblemHandler ph = new ProblemHandler();
        Exception exception = null;
        Problem problem;
        String actual;

        try {
            ph.execute(() -> { throw new RuntimeException("rt_exception"); });
        } catch ( Exception e ) { exception = e; }

        assertEquals(1, ph.size(), "Record list size mismatch");
        problem = ph.getAll().get(0);

        actual = problem.id().split("-")[1];

        assertNotNull(exception, "Null exception");
        assertEquals(exception.hashCode(), Integer.parseInt(actual), "Hash mismatch");

        actual = problem.get().getMessage();
        assertEquals("rt_exception", actual, "Message mismatch");
    }

    @Test
    public void testAttempt() {
        ProblemHandler ph = new ProblemHandler();

        String result = ph.attempt(() -> { throw new RuntimeException("rt_exception"); }, record -> {
            return "error-value";
        });

        assertEquals("error-value", result, "Value mismatch");
    }

    @Test
    public void testAttemptOptional() {
        ProblemHandler ph = new ProblemHandler();
        Optional<String> result = ph.attemptOptional(() -> { throw new RuntimeException("rt_exception"); });

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
    public void testGetRecent() {
        ProblemHandler ph = new ProblemHandler();

        int count = 5;

        for ( int i = 1; i <= count; i++ ) { ph.handle(new RuntimeException("rt_exception_" + i)); }

        List<Problem> recents = ph.getRecent(count - 1);

        assertEquals(count - 1, recents.size(), "Size mismatch");

        for ( int i = 1; i < count - 1; i++ ) {
            Instant previous_time = recents.get(i - 1).instant();
            Instant current_time = recents.get(i).instant();
            assertTrue(previous_time.compareTo(current_time) <= 0, "Timing mismatch");
        }
    }

    @Test
    public void testGetByType() {
        ProblemHandler ph = new ProblemHandler();

        for ( int i = 1; i <= 3; i++ ) {
            ph.handle(new IndexOutOfBoundsException("iob_exception_" + i));
            ph.handle(new NullPointerException("np_exception_" + i));
            ph.handle(new StackOverflowError("so_error_" + i));
            if ( i % 3 == 0 ) ph.handle(new IllegalAccessException("ia_exception_" + i));
        }

        assertEquals(10, ph.size(), "Size mismatch");

        List<Problem> typeList = ph.getByType(IndexOutOfBoundsException.class);

        assertEquals(3, typeList.size(), "Size mismatch");

        typeList = ph.getByType(IllegalAccessException.class);

        assertEquals(1, typeList.size(), "Size mismatch");
    }
}
