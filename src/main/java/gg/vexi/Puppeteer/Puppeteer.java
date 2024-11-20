package gg.vexi.Puppeteer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.ProblemHandler;
import gg.vexi.Puppeteer.Exceptions.PuppetNotFound;
import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.Result;

public class Puppeteer {

    private final Registry registry;
    private final ProblemHandler pHandler;
    private final Map<String, PriorityBlockingQueue<Ticket<?>>> ticketQueues;
    private final Map<String, Ticket<?>> activeTickets;

    private final boolean verbose;

    public Puppeteer(boolean verbose) {
        this.verbose = verbose;
        printDebug("Initializing puppeteer!");
        if (verbose) pHandler = new ProblemHandler(verbose);
        else pHandler = new ProblemHandler();
        registry = new Registry();
        ticketQueues = new ConcurrentHashMap<>();
        activeTickets = new ConcurrentHashMap<>();
    }

    public Puppeteer() {
        this(false);
    }

    public final void printDebug(String msg) {
        if (verbose) {
            System.out.println(msg);
            System.out.flush();

        }
    }

    public synchronized final <T> void registerPuppet(
        String name,
        Class<? extends Puppet<T>> puppetClass) {
        registry.registerPuppet(name, puppetClass);
        printDebug(String.format(
            "Registered puppet with name %s [RegistrySize: %d]",
            name, registry.all().keySet().size()));

        puppetQueues.put(name, new PriorityBlockingQueue<>());
        printDebug(String.format("Created new queue for puppets with name -> %s", name));
    }


    public <T> Ticket<T> createTicket(
        String puppet,
        TicketPriority ticket_priority,
        Map<String, Object> ticket_parameters) {

        if (!registry.contains(puppet))
            throw new PuppetNotFound(
                String.format("\"%s\" is not a registered puppet", puppet));

        return pHandler.attemptOrElse(() -> {
            CompletableFuture<Result<T>> future = new CompletableFuture<>();
            Ticket<T> ticket = new Ticket<>(puppet, ticket_priority, ticket_parameters, future);
            return ticket;
        }, null);
    }

    public <T> Ticket<T> createTicket(String puppet) {
        return createTicket(puppet, TicketPriority.NORMAL, new ConcurrentHashMap<>());
    }

    public <T> Ticket<T> createTicket(String puppet, TicketPriority ticket_priority) {
        return createTicket(puppet, TicketPriority.NORMAL, new ConcurrentHashMap<>());
    }

    public <T> Ticket<T> createTicket(String puppet, Map<String, Object> ticket_parameters) {
        return createTicket(puppet, TicketPriority.NORMAL, ticket_parameters);
    }

    /*
     *
     */
    // overloads if customer has not already created the ticket themselves with
    // createTicket()
    public void queueTicket(Ticket<?> ticket) {
        if (ticket == null) throw new NullPointerException("Ticket cannot be null");
        pHandler.attempt(() -> {
            if (!registry.contains(ticket.puppet()))
                throw new PuppetNotFound(
                    String.format("\"%s\" is not a registered puppet", ticket.puppet()));
            addTicketToQueue(ticket);
            tryExecuteNextTicket(ticket.puppet());
        }, problem -> {
            ProblemHandler ph = new ProblemHandler();
            ph.handle(new Exception(problem.get()));
            ticket.future().complete(Result.complete(ResultStatus.ERROR_FAILED, ph));
        });
    }

    public <T> Ticket<T> queueTicket(String puppet, TicketPriority ticket_priority,
        Map<String, Object> ticket_parameters) {
        Ticket<T> ticket = createTicket(puppet, ticket_priority, ticket_parameters);
        queueTicket(ticket);
        return ticket;
    }

    public <T> Ticket<T> queueTicket(String puppet) {
        return queueTicket(puppet, new ConcurrentHashMap<>());
    }

    public <T> Ticket<T> queueTicket(String puppet, TicketPriority priority) {
        return queueTicket(puppet, priority, new ConcurrentHashMap<>());
    }

    public <T> Ticket<T> queueTicket(String puppet, Map<String, Object> ticket_parameters) {
        return queueTicket(puppet, TicketPriority.NORMAL, ticket_parameters);
    }

    protected synchronized void addTicketToQueue(Ticket<?> ticket) {
        printDebug(String.format("Adding a ticket to queue %s [Queue length is currently %d]",
            ticket.puppet(), ticketQueues.get(ticket.puppet()).size()));
        ticketQueues.get(ticket.puppet()).offer(ticket);
        printDebug(
            String.format("Queue size is now %d", ticketQueues.get(ticket.puppet()).size()));
    }

    private synchronized Ticket<?> pollForTicket(String puppet) {
        // printDebug("Polling for ticket of type "+type);
        return ticketQueues.get(puppet).poll();
    }

    protected final Ticket<?> nextTicket(String puppet) {
        if (!isActive(puppet)) {
            // printDebug("Queue size when polling for type "+type+":
            // "+actionQueues.get(type).size());
            Ticket<?> next_ticket = pollForTicket(puppet);
            printDebug(String.format("Polling... [Queue size is now %d]",
                ticketQueues.get(puppet).size()));
            return next_ticket;
        } else {
            printDebug(String.format("A ticket is already being processed for type %s", puppet));
            return null;
        }
    }

    protected final void tryExecuteNextTicket(String puppet) {
        Ticket<?> next_ticket = nextTicket(puppet);
        if (next_ticket == null) {
            // we should check to see if isEmpty() returns true
            printDebug(String.format(
                "nextTicket for type %s returned null! " +
                    "(a ticket might be active or the queue is empty)",
                puppet));
            return;
        }
        executeTicket(next_ticket);
    }

    protected final <T> void executeTicket(final Ticket<?> ticket) {
        // make ticket active
        activeTickets.putIfAbsent(ticket.puppet(), ticket);

        // initialise the relevant puppet and get the puppet future
        Puppet<T> puppet = this.registry.retreive(ticket);

        CompletableFuture<Result<T>> puppetFuture = puppet.getFuture();

        // run puppet
        // - (eventually using a custom thread executor instead of CmplFutr)
        CompletableFuture.runAsync(() -> puppet.start());
        // printDebug("Processing a ticket for type "+ticket.getType()+" | args:
        // "+ticket.getParameters().toString());
        // printDebug("Queue size after starting ticket ("+ticket.getType()+"):
        // "+actionQueues.get(ticket.getType()).size());

        // pass result to ticket future when compelte (we may do more stuff here later)
        puppetFuture.thenAccept(result -> {

            completeTicket(ticket, result);

        });
    }

    @SuppressWarnings("unchecked")
    protected final <T> void completeTicket(Ticket<?> ticket, Result<?> result) {
        printDebug(String.format("A %s puppet has completed", ticket.puppet()));
        // this method preserves the type relationship between ticket and result
        ((Ticket<T>) ticket).future().complete((Result<T>) result);
        activeTickets.remove(ticket.puppet());
        printDebug(
            String.format("Attempting to execute next ticket for puppet %s", ticket.puppet()));
        tryExecuteNextTicket(ticket.puppet());
    }

    public synchronized final <T> void registerPuppet(
        String name,
        Class<? extends Puppet<T>> puppetClass) {
        registry.registerPuppet(name, puppetClass);
        printDebug(String.format(
            "Registered puppet with name %s [RegistrySize: %d]",
            name, registry.all().keySet().size()));

        puppetQueues.put(name, new PriorityBlockingQueue<>());
        printDebug(String.format("Created new queue for puppets with name -> %s", name));
    }

    // getters:

    // if any tickets are queued or active puppeteer is "performing"
    public synchronized final boolean isPerforming() {
        return (activeTickets.keySet().stream()
            .filter(key -> (activeTickets.get(key) != null))
            .count() != 0
            || ticketQueues.keySet().stream()
                .filter(key -> !ticketQueues.get(key).isEmpty())
                .count() != 0);
    }

    protected synchronized final boolean isActive(String puppet) {
        return activeTickets.containsKey(puppet);
    }

    public synchronized final ProblemHandler getProblemHandler() {
        return pHandler;
    }

    public synchronized final PriorityBlockingQueue<Ticket<?>> getQueue(String puppet) {
        return ticketQueues.get(puppet);
    }

    public synchronized final Map<String, PriorityBlockingQueue<Ticket<?>>> getAllQueues() {
        return ticketQueues;
    }

    public synchronized final Ticket<?> getActive(String puppet) {
        return activeTickets.get(puppet);
    }

    public synchronized final Map<String, Ticket<?>> getAllActive() {
        return activeTickets;
    }

    // setters
    // TODO: Make private and make tests use reflection to access
    protected synchronized final void setActive(Ticket<?> ticket) {
        activeTickets.put(ticket.puppet(), ticket);
    }

}
