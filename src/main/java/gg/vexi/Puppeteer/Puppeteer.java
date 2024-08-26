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
    private final Map<String, PriorityBlockingQueue<Ticket>> actionQueues = new ConcurrentHashMap<>();
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();


    // Should we use the top parent package where the user initialized ticketmanager?
    public Puppeteer(String puppetPackage) {
        refreshRegistry(puppetPackage);
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
    
    
    protected void addTicketToQueue(Ticket ticket) {
        actionQueues.get(ticket.getType()).offer(ticket);
    }
    
    
    public Ticket queueTicket(String action_type, TicketPriority ticket_priority, JsonObject ticket_parameters) {
        
        Ticket ticket = createTicket(action_type, ticket_priority, ticket_parameters); // create ticket
        
        addTicketToQueue(ticket);
        tryExecuteNextTicket(ticket.getType());
        
        return ticket;
    }
    // overload if customer already created ticket themselves with createTicket()
    public void queueTicket(Ticket ticket) {
        addTicketToQueue(ticket);
        tryExecuteNextTicket(ticket.getType());   
    }
    
    


    protected Ticket nextTicket(String type) {

        if (getActive(type) == null) {
            
            return actionQueues.get(type).poll();

        } else {

            return null;
        }
    }




    protected void tryExecuteNextTicket(String action_type) {

        Ticket next_ticket = nextTicket(action_type);

        if (next_ticket == null) { return; }

        executeTicket(next_ticket);
    }




    protected void executeTicket(Ticket ticket) {

        // make ticket active
        activeTickets.putIfAbsent(ticket.getType(), ticket);
        
        // initialise the relevant puppet and get the puppet future
        AbstractPuppet puppet = puppetRegistry.getPuppet(ticket);
        CompletableFuture<TicketResult> puppetFuture = puppet.getFuture();

        // begin performance
        CompletableFuture.runAsync(() -> puppet.start());
        
        // pass result to ticket future when compelte (we will do more stuff here later)
        puppetFuture.thenAccept(result -> {
        
            completeTicket(ticket, result);
        
        });

    }

    protected void completeTicket(Ticket ticket, TicketResult result) {
        
        ticket.getFuture().complete(result);

        activeTickets.remove(ticket.getType());
    
        tryExecuteNextTicket(ticket.getType());
    }



    // UNSAFE!
    public final void refreshRegistry(String packageName) {
        try {
            autoRegisterPuppetes(packageName);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to refresh puppet registry", e);
        }
    }

    private void autoRegisterPuppetes(String packageName) throws IOException, ClassNotFoundException {

        List<Class<?>> puppetClasses = AnnotationScanner.findAnnotatedClasses(packageName, RegisterPuppet.class);

        for (Class<?> puppetClass : puppetClasses) {
            
            RegisterPuppet annotation = puppetClass.getAnnotation(RegisterPuppet.class);
            String actionType = annotation.value().toLowerCase();
            if (actionType.isEmpty()) { actionType = puppetClass.getSimpleName().toLowerCase(); }

            // add puppet to registry and create a queue for it
            registerPuppet(puppetClass, actionType);
            actionQueues.put(actionType, new PriorityBlockingQueue<>());
        }
    }

    private void registerPuppet(Class<?> puppetClass, String type) {
        puppetRegistry.registerPuppet(type, (ticket) -> {
            try {
                actionQueues.put(type, new PriorityBlockingQueue<>());
                return (AbstractPuppet) puppetClass.getDeclaredConstructor(Ticket.class).newInstance(ticket);
            } catch (IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate class", e);
            }
        });
    }


    // getters
    public PriorityBlockingQueue<Ticket> getQueue(String type) {
        return actionQueues.get(type);
    }

    public Map<String, PriorityBlockingQueue<Ticket>> getAllQueues() {
        return actionQueues;
    }

    public Ticket getActive(String type) {
        return activeTickets.get(type);
    }

    public Map<String, Ticket> getAllActive() {
        return activeTickets;
    }

    // setters
    public void setActive(Ticket ticket) {
        activeTickets.put(ticket.getType(), ticket);
    }

}
