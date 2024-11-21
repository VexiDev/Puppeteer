package gg.vexi.Puppeteer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Core.ResultStatus;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.Exceptions.PuppetNotFound;
import gg.vexi.Puppeteer.Ticket.Result;
import gg.vexi.Puppeteer.Ticket.TicketPriority;

public class Puppeteer {                       // New tickets:
    public final byte SHUTDOWN_STATE = 0;      // <- CANNOT be queued
    public final byte SHUTTING_DOWN_STATE = 1; // <- CANNOT be queued
    public final byte CLOSED_STATE = 2;        // <- CANNOT be queued
    public final byte OPEN_STATE = 3;          // <- CAN be queued
    private final AtomicInteger state = new AtomicInteger(CLOSED_STATE);

    private final Registry registry;
    private final ProblemHandler pHandler;
    private final Map<String, Puppet<?>> activePuppets;
    private final Map<String, PriorityBlockingQueue<Ticket<?>>> ticketQueues;

    private final boolean verbose;

    public Puppeteer() { this(false); }

    public Puppeteer(boolean verbose) {
        this.verbose = verbose;
        printDebug("Initializing puppeteer!");
        if ( verbose ) pHandler = new ProblemHandler(verbose);
        else pHandler = new ProblemHandler();
        registry = new Registry();
        ticketQueues = new ConcurrentHashMap<>();
        activePuppets = new ConcurrentHashMap<>();
        this.state.compareAndSet(CLOSED_STATE, OPEN_STATE);
    }

    public final void printDebug(String msg) {
        if ( verbose ) {
            System.out.println(msg);
            System.out.flush();
        }
    }

    public synchronized final <T> void registerPuppet(String name, Class<? extends Puppet<T>> puppetClass) {
        registry.registerPuppet(name, puppetClass);
        printDebug(
            String.format("Registered puppet with name %s [RegistrySize: %d]", name, registry.all().keySet().size())
        );

        ticketQueues.put(name, new PriorityBlockingQueue<>());
        printDebug(String.format("Created new queue for puppets with name -> %s", name));
    }

    /*
     *
     */
    public final <T> Ticket<T> createTicket(
        String puppet, TicketPriority ticket_priority, Map<String, Object> ticket_parameters
    ) {
        if ( !registry.contains(puppet) )
            throw new PuppetNotFound(String.format("\"%s\" is not a registered puppet", puppet));
        return pHandler.attemptOrElse(() -> {
            CompletableFuture<Result<T>> future = new CompletableFuture<>();
            Ticket<T> ticket = new Ticket<>(puppet, ticket_priority, ticket_parameters, future);
            return ticket;
        }, null);
    }

    public final <T> Ticket<T> createTicket(String puppet) {
        return createTicket(puppet, TicketPriority.NORMAL, new ConcurrentHashMap<>());
    }

    public final <T> Ticket<T> createTicket(String puppet, TicketPriority ticket_priority) {
        return createTicket(puppet, TicketPriority.NORMAL, new ConcurrentHashMap<>());
    }

    public final <T> Ticket<T> createTicket(String puppet, Map<String, Object> ticket_parameters) {
        return createTicket(puppet, TicketPriority.NORMAL, ticket_parameters);
    }

    /*
     *
     */
    public final void queueTicket(Ticket<?> ticket) {
        if ( this.state.get() < OPEN_STATE )
            throw new IllegalStateException(String.format("Cannot queue new tickets, state is %d", this.state.get()));
        if ( ticket == null ) throw new NullPointerException("Ticket cannot be null");
        pHandler.attempt(() -> {
            if ( !registry.contains(ticket.puppet()) )
                throw new PuppetNotFound(String.format("\"%s\" is not a registered puppet", ticket.puppet()));
            addTicketToQueue(ticket);
            tryExecuteNextTicket(ticket.puppet());
        }, problem -> {
            ProblemHandler ph = new ProblemHandler();
            ph.handle(new Exception(problem.get()));
            ticket.future().complete(Result.complete(ResultStatus.ERROR_FAILED, ph));
        });
    }

    public final <T> Ticket<T> queueTicket(
        String puppet, TicketPriority ticket_priority, Map<String, Object> ticket_parameters
    ) {
        Ticket<T> ticket = createTicket(puppet, ticket_priority, ticket_parameters);
        queueTicket(ticket);
        return ticket;
    }

    public final <T> Ticket<T> queueTicket(String puppet) { return queueTicket(puppet, new ConcurrentHashMap<>()); }

    public final <T> Ticket<T> queueTicket(String puppet, TicketPriority priority) {
        return queueTicket(puppet, priority, new ConcurrentHashMap<>());
    }

    public final <T> Ticket<T> queueTicket(String puppet, Map<String, Object> ticket_parameters) {
        return queueTicket(puppet, TicketPriority.NORMAL, ticket_parameters);
    }

    protected synchronized void addTicketToQueue(Ticket<?> ticket) {
        printDebug(String.format(
            "Adding a ticket to queue %s [Queue length is currently %d]",
            ticket.puppet(),
            ticketQueues.get(ticket.puppet()).size()
        ));
        ticketQueues.get(ticket.puppet()).offer(ticket);
        printDebug(String.format("Queue size is now %d", ticketQueues.get(ticket.puppet()).size()));
    }

    private synchronized Ticket<?> pollForTicket(String puppet) {
        // printDebug("Polling for ticket of type "+type);
        return ticketQueues.get(puppet).poll();
    }

    protected final Ticket<?> nextTicket(String puppet) {
        if ( !isActive(puppet) ) {
            // printDebug("Queue size when polling for type "+type+":
            // "+actionQueues.get(type).size());
            Ticket<?> next_ticket = pollForTicket(puppet);
            printDebug(String.format("Polling... [Queue size is now %d]", ticketQueues.get(puppet).size()));
            return next_ticket;
        } else {
            printDebug(String.format("A ticket is already being processed for type %s", puppet));
            return null;
        }
    }

    protected final void tryExecuteNextTicket(String puppet) {
        Ticket<?> next_ticket = nextTicket(puppet);
        if ( next_ticket == null ) {
            // we should check to see if isEmpty() returns true
            printDebug(String.format(
                "nextTicket for type %s returned null! "
                    + "(a ticket might be active or the queue is empty)",
                puppet
            ));
            return;
        }
        executeTicket(next_ticket);
    }

    protected final <T> void executeTicket(final Ticket<?> ticket) {
        // initialise the relevant puppet and get the puppet future
        Puppet<T> puppet = this.registry.retreive(ticket);

        // set puppet active
        activePuppets.putIfAbsent(ticket.puppet(), puppet);

        // get puppet future
        CompletableFuture<Result<T>> puppetFuture = puppet.getFuture();

        // run puppet
        // - (eventually using a custom thread executor instead of CmplFutr)
        CompletableFuture.runAsync(() -> puppet.start());
        // printDebug("Processing a ticket for type "+ticket.getType()+" | args:
        // "+ticket.getParameters().toString());
        // printDebug("Queue size after starting ticket ("+ticket.getType()+"):
        // "+actionQueues.get(ticket.getType()).size());

        // pass result to ticket future when compelte (we may do more stuff here later)
        puppetFuture.thenAccept(result -> { completeTicket(ticket, result); });
    }

    @SuppressWarnings("unchecked")
    protected final <T> void completeTicket(Ticket<?> ticket, Result<?> result) {
        printDebug(String.format("A %s puppet has completed", ticket.puppet()));
        // this method preserves the type relationship between ticket and result
        ((Ticket<T>) ticket).future().complete((Result<T>) result);
        activePuppets.remove(ticket.puppet());
        printDebug(String.format("Attempting to execute next ticket for puppet %s", ticket.puppet()));
        tryExecuteNextTicket(ticket.puppet());
    }

    // Default to soft shutdown
    public final boolean shutdown() { return shutdown(false); }

    /*
     *  Returns true if is shutdown
     *  Returns false if already shutting down
     */
    public final boolean shutdown(boolean hard) {
        int s = this.state.get();
        if ( s == 0 ) return true;
        else if ( s == 1 ) return false;

        this.state.compareAndSet(s, SHUTTING_DOWN_STATE);

        // Note:
        // SOFT shutdown risks long lock times if running puppets take a long time to complete!
        cancelQueued();
        // if (hard) cancelActive(); //not implemented
        while ( anyActive() ) continue;

        this.state.compareAndSet(SHUTTING_DOWN_STATE, SHUTDOWN_STATE);
        return true;
    }

    // Currently since we use CmplFtr.runAsync(() -> puppet.start()); to run puppets we don't
    // hold references to the thread running a given puppet. Until we do keep this reference
    //(probably via a custom executor design) we cannot interrupt puppets early (this is bad)
    @SuppressWarnings("unused")
    private void cancelActive() {
        // TODO: Implement ability for puppeteer to interrupt active puppets
        // something like:
        //  activeTickets.values().stream().forEach(p -> p::interrupt);
    }

    private synchronized void cancelQueued() {
        ticketQueues.values().stream().forEach(ticketQ -> {
            while ( ticketQ.peek() != null ) ticketQ.poll().cancel(new ProblemHandler());
        });
    }

    // getters:

    public synchronized final boolean isPerforming() { return (anyActive() || anyQueued()); }

    // clang-format off
    private final boolean anyActive() {
        return (activePuppets.keySet().stream()
                    .filter(key -> (activePuppets.get(key) != null))
                    .count() != 0);
    }

    private final boolean anyQueued() {
        return (ticketQueues.keySet().stream()
                    .filter(key -> !ticketQueues.get(key).isEmpty())
                    .count() != 0);
    } // clang-format on

    public synchronized final ProblemHandler getProblemHandler() { return pHandler; }

    public synchronized final Map<String, PriorityBlockingQueue<Ticket<?>>> getAllQueues() { return ticketQueues; }

    public synchronized final PriorityBlockingQueue<Ticket<?>> getQueue(String puppet) {
        return ticketQueues.get(puppet);
    }

    protected synchronized final boolean isActive(String puppet) { return activePuppets.containsKey(puppet); }

    public synchronized final Ticket<?> getActive(String puppet) {
        Puppet<?> p = activePuppets.get(puppet);
        return (p == null) ? null : p.getTicket();
    }

    public synchronized final Map<String, Ticket<?>> getAllActive() {
        return activePuppets.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTicket())
        );
    }
}
