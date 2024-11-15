package gg.vexi.Puppeteer;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.Result;

public class Puppeteer {

    private final Registry puppetRegistry = new Registry();
    private final Map<String, PriorityBlockingQueue<Ticket>> actionQueues;
    private final Map<String, Ticket> activeTickets;

    private final boolean debug;

    // annotation based registration should be optional
    public Puppeteer() {
        this(false);
    }

    public Puppeteer(boolean send_debug) {
        debug = send_debug;
        printDebug("Initializing puppeteer!");
        actionQueues = new ConcurrentHashMap<>();
        activeTickets = new ConcurrentHashMap<>();
    }

    public final void printDebug(String msg) {
        if (debug) {
            System.out.println(msg);
            System.out.flush();
        }
    }

    public Ticket createTicket(String action_type, TicketPriority ticket_priority,
            Map<String, Object> ticket_parameters) {
        // compare method for automatic sorting when in queues
        CompletableFuture<Result> ticket_future = new CompletableFuture<>();
        Ticket ticket = new Ticket(action_type, ticket_priority, ticket_parameters, ticket_future);
        return ticket;
    }

    // overloads for create ticket (default priority NORMAL, default parameters Empty Map
    public Ticket createTicket(String action_type) {
        return createTicket(action_type, TicketPriority.NORMAL, new ConcurrentHashMap<>());
    }

    public Ticket createTicket(String action_type, TicketPriority ticket_priority) {
        return createTicket(action_type, TicketPriority.NORMAL, new ConcurrentHashMap<>());
    }

    public Ticket createTicket(String action_type, Map<String, Object> ticket_parameters) {
        return createTicket(action_type, TicketPriority.NORMAL, ticket_parameters);
    }

    public synchronized void queueTicket(Ticket ticket) {
        addTicketToQueue(ticket);
        tryExecuteNextTicket(ticket.getType());
    }

    // overloads if customer has not already created the ticket themselves with
    // createTicket()
    public synchronized Ticket queueTicket(String action_type, TicketPriority ticket_priority,
            Map<String, Object> ticket_parameters) {
        Ticket ticket = createTicket(action_type, ticket_priority, ticket_parameters);
        queueTicket(ticket);
        return ticket;
    }

    public synchronized Ticket queueTicket(String action_type) {
        return queueTicket(action_type, new ConcurrentHashMap<>());
    }

    public synchronized Ticket queueTicket(String action_type, TicketPriority priority) {
        return queueTicket(action_type, priority, new ConcurrentHashMap<>());
    }

    public synchronized Ticket queueTicket(String action_type, Map<String, Object> ticket_parameters) {
        return queueTicket(action_type, TicketPriority.NORMAL, ticket_parameters);
    }

    protected final void addTicketToQueue(Ticket ticket) {
        printDebug("Adding a ticket to queue " + ticket.getType() + " [Queue length is currently "
                + actionQueues.get(ticket.getType()).size() + "]");
        // this should handle key not found exceptions and return an error to the ticket
        // future!
        // - this is caused when a user attempts to queue a ticket for a non existent
        // puppet
        actionQueues.get(ticket.getType()).offer(ticket);
        printDebug("Queue size is now " + actionQueues.get(ticket.getType()).size());
    }

    private synchronized Ticket pollForTicket(String type) {
        // printDebug("Polling for ticket of type "+type);
        return actionQueues.get(type).poll();
    }

    protected final Ticket nextTicket(String type) {
        if (!isActive(type)) {
            // printDebug("Queue size when polling for type "+type+":
            // "+actionQueues.get(type).size());
            Ticket next_ticket = pollForTicket(type);
            printDebug("Polling... [Queue size is now " + actionQueues.get(type).size() + "]");
            return next_ticket;
        } else {
            printDebug("A ticket is already being processed for type " + type);
            return null;
        }
    }

    protected final void tryExecuteNextTicket(String action_type) {
        Ticket next_ticket = nextTicket(action_type);
        if (next_ticket == null) {
            // we should check to see if isEmpty() returns true
            printDebug("nextTicket for type " + action_type
                    + " returned null! (a ticket might be active or the queue is empty)");
            return;
        }
        executeTicket(next_ticket);
    }

    protected final void executeTicket(Ticket ticket) {
        // make ticket active
        activeTickets.putIfAbsent(ticket.getType(), ticket);

        // initialise the relevant puppet and get the puppet future
        Puppet puppet = puppetRegistry.getPuppet(ticket);

        CompletableFuture<Result> puppetFuture = puppet.getFuture();

        // begin performance
        // printDebug("Queue size before starting ticket ("+ticket.getType()+"):
        // "+actionQueues.get(ticket.getType()).size());
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
        ticket.getFuture().complete(result);
        activeTickets.remove(ticket.getType());
        printDebug(System.currentTimeMillis() + " - A Ticket of type " + ticket.getType()
                + " has completed! Attempting to execute next ticket!");
        tryExecuteNextTicket(ticket.getType());
    }

    public synchronized final void registerPuppet(Class<?> puppetClass, String type) {
        puppetRegistry.registerPuppet(type, (ticket) -> {
            try {
                return (Puppet) puppetClass.getDeclaredConstructor(Ticket.class).newInstance(ticket);
            } catch (IllegalArgumentException | InstantiationException
                    | NoSuchMethodException | SecurityException
                    | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate puppet class", e);
            }
        });
        printDebug("Registered puppet with type " + type + " [RegistrySize: "
                + puppetRegistry.getAllActionTypes().size() + "]");
        printDebug("Creating new queue for puppets with type -> " + type);
        actionQueues.put(type, new PriorityBlockingQueue<>());
    }

    protected synchronized final boolean isActive(String type) {
        return activeTickets.containsKey(type);
    }

    // getters:
    // - protected grants access for our tests which are in the same package.
    // - should non essentials be removed/private for prod release?)
    protected synchronized final PriorityBlockingQueue<Ticket> getQueue(String type) {
        return actionQueues.get(type);
    }

    protected synchronized final Map<String, PriorityBlockingQueue<Ticket>> getAllQueues() {
        return actionQueues;
    }

    protected synchronized final Ticket getActive(String type) {
        return activeTickets.get(type);
    }

    protected synchronized final Map<String, Ticket> getAllActive() {
        return activeTickets;
    }

    // setters
    protected synchronized final void setActive(Ticket ticket) {
        activeTickets.put(ticket.getType(), ticket);
    }

}
