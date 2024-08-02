package gg.vexi.TicketSystem;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class WorkerTests {

    private MockWorker worker;

    @BeforeEach
    public void setup() {
        worker = new MockWorker();
    }

    // Since workers are just going to be implementations of an abstract worker class
    // As per my current understanding of testing abstract classes:
    // we should just mock what a worker could be and ensure its has all expected attributes and methods
    @Test
    public void test_init() {

        // verify default worker attributes are set
        assertNotNull(worker.getTicket(), "Worker associated Ticket is null");
        assertNotNull(worker.getFuture(), "Worker future is null");
        assertNotNull(worker.getStatus(), "Worker status is null");

        // verify default status for worker is ready
        assertEquals(WorkerStatus.READY, worker.getStatus(), "Worker default status is not READY");

    }

}
