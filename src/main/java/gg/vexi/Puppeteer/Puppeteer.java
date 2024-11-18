package gg.vexi.Puppeteer;

import java.lang.reflect.InvocationTargetException;
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

    private final Registry registry = new Registry();
    private final ProblemHandler pHandler;
    private final Map<String, PriorityBlockingQueue<Ticket>> puppetQueues;
    private final Map<String, Ticket> activeTickets;

    private final boolean verbose;

    public Puppeteer(boolean verbose) {
        this.verbose = verbose;
        printDebug("Initializing puppeteer!");
        if (verbose) pHandler = new ProblemHandler(verbose);
        else pHandler = new ProblemHandler();
        puppetQueues = new ConcurrentHashMap<>();
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

    public Ticket createTicket(
        String puppet,
        TicketPriority ticket_priority,
        Map<String, Object> ticket_parameters) {

        if (!registry.contains(puppet))
            throw new PuppetNotFound(
                String.format("\"%s\" is not registered", puppet));

        return pHandler.attemptOrElse(() -> {
            CompletableFuture<Result> ticket_future = new CompletableFuture<>();
            Ticket ticket =
                new Ticket(puppet, ticket_priority, ticket_parameters, ticket_future);
            return ticket;
        }, null);
    }

    // overloads for create ticket (default priority NORMAL, default parameters Empty Map
    public Ticket createTicket(String puppet) {
        return createTicket(puppet, TicketPriority.NORMAL, new ConcurrentHashMap<>());
    }

    public Ticket createTicket(String puppet, TicketPriority ticket_priority) {
        return createTicket(puppet, TicketPriority.NORMAL, new ConcurrentHashMap<>());
    }

    public Ticket createTicket(String puppet, Map<String, Object> ticket_parameters) {
        return createTicket(puppet, TicketPriority.NORMAL, ticket_parameters);
    }

    public void queueTicket(Ticket ticket) {
        if (ticket == null) throw new NullPointerException("Ticket cannot be null");
        pHandler.attempt(() -> {
            if (!registry.contains(ticket.puppet()))
                throw new PuppetNotFound(
                    String.format("\"%s\" is not registered", ticket.puppet()));
            addTicketToQueue(ticket);
            tryExecuteNextTicket(ticket.puppet());
        }, problem -> {
            ProblemHandler ph = new ProblemHandler();
            ph.handle(new Exception(problem.get()));
            Result failedResult = new Result(
                ph,
                ticket,
                ResultStatus.ERROR_FAILED,
                null);
            ticket.future().complete(failedResult);
        });
    }

    // overloads if customer has not already created the ticket themselves with
    // createTicket()
    public Ticket queueTicket(String puppet, TicketPriority ticket_priority,
        Map<String, Object> ticket_parameters) {
        Ticket ticket = createTicket(puppet, ticket_priority, ticket_parameters);
        queueTicket(ticket);
        return ticket;
    }

    public Ticket queueTicket(String puppet) {
        return queueTicket(puppet, new ConcurrentHashMap<>());
    }

    public Ticket queueTicket(String puppet, TicketPriority priority) {
        return queueTicket(puppet, priority, new ConcurrentHashMap<>());
    }

    public Ticket queueTicket(String puppet, Map<String, Object> ticket_parameters) {
        return queueTicket(puppet, TicketPriority.NORMAL, ticket_parameters);
    }

    protected synchronized void addTicketToQueue(Ticket ticket) {
        printDebug("Adding a ticket to queue " + ticket.puppet() +
            " [Queue length is currently " + puppetQueues.get(ticket.puppet()).size() + "]");
        puppetQueues.get(ticket.puppet()).offer(ticket);
        printDebug("Queue size is now " + puppetQueues.get(ticket.puppet()).size());
    }

    private synchronized Ticket pollForTicket(String puppet) {
        // printDebug("Polling for ticket of type "+type);
        return puppetQueues.get(puppet).poll();
    }

    protected final Ticket nextTicket(String puppet) {
        if (!isActive(puppet)) {
            // printDebug("Queue size when polling for type "+type+":
            // "+actionQueues.get(type).size());
            Ticket next_ticket = pollForTicket(puppet);
            printDebug("Polling... [Queue size is now " + puppetQueues.get(puppet).size() + "]");
            return next_ticket;
        } else {
            printDebug("A ticket is already being processed for type " + puppet);
            return null;
        }
    }

    protected final void tryExecuteNextTicket(String puppet) {
        Ticket next_ticket = nextTicket(puppet);
        if (next_ticket == null) {
            // we should check to see if isEmpty() returns true
            printDebug("nextTicket for type " + puppet
                + " returned null! (a ticket might be active or the queue is empty)");
            return;
        }
        executeTicket(next_ticket);
    }

    protected final void executeTicket(final Ticket ticket) {
        // make ticket active
        activeTickets.putIfAbsent(ticket.puppet(), ticket);

        // initialise the relevant puppet and get the puppet future
        Puppet puppet = registry.retreive(ticket);

        CompletableFuture<Result> puppetFuture = puppet.getFuture();

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

    protected final void completeTicket(Ticket ticket, Result result) {
        ticket.future().complete(result);
        activeTickets.remove(ticket.puppet());
        printDebug(System.currentTimeMillis() + " - A Ticket of type " + ticket.puppet()
            + " has completed! Attempting to execute next ticket!");
        tryExecuteNextTicket(ticket.puppet());
    }

    public synchronized final void registerPuppet(Class<?> puppetClass, String name) {
        registry.registerPuppet(name, (ticket) -> {
            try {
                return (Puppet) puppetClass.getDeclaredConstructor(Ticket.class)
                    .newInstance(ticket);
            } catch (IllegalArgumentException | InstantiationException | NoSuchMethodException
                | SecurityException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate puppet class", e);
            }
        });
        printDebug("Registered puppet with type " + name + " [RegistrySize: "
            + registry.all().keySet().size() + "]");
        printDebug("Creating new queue for puppets with type -> " + name);
        puppetQueues.put(name, new PriorityBlockingQueue<>());
    }

    // getters:

    public synchronized final boolean isPerforming() {
        return (activeTickets.keySet().stream()
            .filter(key -> (activeTickets.get(key) != null))
            .count() != 0
            || puppetQueues.keySet().stream()
                .filter(key -> !puppetQueues.get(key).isEmpty())
                .count() != 0);
    }

    protected synchronized final boolean isActive(String type) {
        return activeTickets.containsKey(type);
    }

    public synchronized final ProblemHandler getProblemHandler() {
        return pHandler;
    }

    public synchronized final PriorityBlockingQueue<Ticket> getQueue(String type) {
        return puppetQueues.get(type);
    }

    public synchronized final Map<String, PriorityBlockingQueue<Ticket>> getAllQueues() {
        return puppetQueues;
    }

    public synchronized final Ticket getActive(String type) {
        return activeTickets.get(type);
    }

    public synchronized final Map<String, Ticket> getAllActive() {
        return activeTickets;
    }

    // setters
    // TODO: Make private and make tests use reflection to access
    // Protected for simplicity
    protected synchronized final void setActive(Ticket ticket) {
        activeTickets.put(ticket.puppet(), ticket);
    }

}
