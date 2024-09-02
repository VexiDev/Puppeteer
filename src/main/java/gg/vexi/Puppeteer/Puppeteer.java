package gg.vexi.Puppeteer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.Core.AbstractPuppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.TicketResult;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;
import gg.vexi.Puppeteer.annotations.Scanner.AnnotationScanner;

public class Puppeteer {

    private final PuppetRegistry puppetRegistry = new PuppetRegistry();
    private final Map<String, PriorityBlockingQueue<Ticket>> actionQueues;
    private final Map<String, Ticket> activeTickets;

    private final boolean debug;

    // auto registration should be optional and the registerPuppet() method should be public
    public Puppeteer(String puppetPackage) { this(puppetPackage, false); }
    public Puppeteer(String puppetPackage, boolean send_debug) {
        printDebug("Initializing puppeteer!");
        actionQueues = new ConcurrentHashMap<>();
        activeTickets = new ConcurrentHashMap<>();
        autoRegisterPuppets(puppetPackage);
        debug = send_debug;
    }

    public final void printDebug(String msg) {
        if (debug) {
            System.out.println(msg);
        }
    }
    
    
    public Ticket createTicket(String action_type, TicketPriority ticket_priority, JsonObject ticket_parameters) {
        
        CompletableFuture<TicketResult> ticket_future = new CompletableFuture<>();
        Ticket ticket = new Ticket(action_type, ticket_priority, ticket_parameters, ticket_future); // create our ticket
        
        return ticket;
    }
    //overloads for create ticket (default priority NORMAL, default parameters empty JsonObject)
    public Ticket createTicket(String action_type) { return createTicket(action_type, TicketPriority.NORMAL, new JsonObject()); }
    public Ticket createTicket(String action_type, TicketPriority ticket_priority) { return createTicket(action_type, TicketPriority.NORMAL, new JsonObject()); }
    public Ticket createTicket(String action_type, JsonObject ticket_parameters) { return createTicket(action_type, TicketPriority.NORMAL, ticket_parameters); }
    
    
    
    // overload if customer has not already created the ticket themselves with createTicket()
    public synchronized Ticket queueTicket(String action_type, TicketPriority ticket_priority, JsonObject ticket_parameters) {
        Ticket ticket = createTicket(action_type, ticket_priority, ticket_parameters);
        queueTicket(ticket);
        return ticket;
    }
    public synchronized void queueTicket(Ticket ticket) {
        addTicketToQueue(ticket);
        tryExecuteNextTicket(ticket.getType());   
    }
    
    protected final void addTicketToQueue(Ticket ticket) {
        printDebug("Adding a ticket to queue "+ticket.getType()+" [Queue length is currently "+actionQueues.get(ticket.getType()).size()+"]");
        // this should handle key not found exceptions and return an error to the ticket future!
        actionQueues.get(ticket.getType()).offer(ticket);
        printDebug("Queue size is now "+actionQueues.get(ticket.getType()).size());
    }
    




    private synchronized Ticket pollForTicket(String type) { 
        // printDebug("Polling for ticket of type "+type);
        return actionQueues.get(type).poll(); 
    }

    protected final Ticket nextTicket(String type) {

        if (!isActive(type)) {
            // printDebug("Queue size when polling for type "+type+": "+actionQueues.get(type).size());
            Ticket next_ticket = pollForTicket(type);
            printDebug("Polling... [Queue size is now "+actionQueues.get(type).size()+"]");
            return next_ticket;
            
        } else { 
            printDebug("A ticket is already being processed for type "+type);
            return null; }
    }


    protected final void tryExecuteNextTicket(String action_type) {

        Ticket next_ticket = nextTicket(action_type);

        if (next_ticket == null) { 
            printDebug("nextTicket for type "+action_type+" returned null! (a ticket might be active or the queue is empty)");
            return;
        } 


        executeTicket(next_ticket);
    }

    protected final void executeTicket(Ticket ticket) {

        // make ticket active
        activeTickets.putIfAbsent(ticket.getType(), ticket);
        
        // initialise the relevant puppet and get the puppet future
        AbstractPuppet puppet = puppetRegistry.getPuppet(ticket);

        CompletableFuture<TicketResult> puppetFuture = puppet.getFuture();
        
        // begin performance
        // printDebug("Queue size before starting ticket ("+ticket.getType()+"): "+actionQueues.get(ticket.getType()).size());
        CompletableFuture.runAsync(() -> puppet.start());
        // printDebug("Processing a ticket for type "+ticket.getType()+" | args: "+ticket.getParameters().toString());
        // printDebug("Queue size after starting ticket ("+ticket.getType()+"): "+actionQueues.get(ticket.getType()).size());
        
        // pass result to ticket future when compelte (we will do more stuff here later)
        puppetFuture.thenAccept(result -> {
        
            completeTicket(ticket, result);
        
        });
    }

    protected final void completeTicket(Ticket ticket, TicketResult result) {
        
        ticket.getFuture().complete(result);
        
        activeTickets.remove(ticket.getType());
        
        printDebug(System.currentTimeMillis()+" - A Ticket of type "+ticket.getType()+" has completed! Attempting to execute next ticket!");
        tryExecuteNextTicket(ticket.getType());
    }





    private void autoRegisterPuppets(String packageName) {
        try {
            runAnnotationScan(packageName);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to refresh puppet registry", e);
        }
    }

    private void runAnnotationScan(String packageName) throws IOException, ClassNotFoundException {

        List<Class<?>> puppetClasses = AnnotationScanner.findAnnotatedClasses(packageName, RegisterPuppet.class);

        for (Class<?> puppetClass : puppetClasses) {
            
            RegisterPuppet annotation = puppetClass.getAnnotation(RegisterPuppet.class);
            String actionType = annotation.value().toLowerCase();
            if (actionType.isEmpty()) { actionType = puppetClass.getSimpleName().toLowerCase(); }

            // add puppet to registry
            registerPuppetConstructor(puppetClass, actionType);

            // create a queue for the puppet
            printDebug("Creating new queue for puppets with type -> "+actionType);
            actionQueues.put(actionType, new PriorityBlockingQueue<>());
        }
    }

    private void registerPuppetConstructor(Class<?> puppetClass, String type) {
        puppetRegistry.registerPuppet(type, (ticket) -> {
            try {
                return (AbstractPuppet) puppetClass.getDeclaredConstructor(Ticket.class).newInstance(ticket);
            } catch (IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate class", e);
            }
        });
    }


    // getters
    protected synchronized final PriorityBlockingQueue<Ticket> getQueue(String type) {
        return actionQueues.get(type);
    }

    protected synchronized final Map<String, PriorityBlockingQueue<Ticket>> getAllQueues() {
        return actionQueues;
    }

    protected synchronized final Ticket getActive(String type) {
        return activeTickets.get(type);
    }

    protected synchronized final boolean isActive(String type) {
        return activeTickets.containsKey(type);
    }

    protected synchronized final Map<String, Ticket> getAllActive() {
        return activeTickets;
    }

    // setters
    protected synchronized final void setActive(Ticket ticket) {
        activeTickets.put(ticket.getType(), ticket);
    }

}
