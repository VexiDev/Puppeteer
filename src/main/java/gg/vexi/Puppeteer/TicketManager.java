package gg.vexi.Puppeteer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import com.google.gson.JsonObject;

import gg.vexi.Puppeteer.Core.AbstractWorker;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.CaughtExceptions;
import gg.vexi.Puppeteer.Ticket.TicketPriority;
import gg.vexi.Puppeteer.Ticket.TicketResult;
import gg.vexi.Puppeteer.annotations.RegisterWorker;
import gg.vexi.Puppeteer.annotations.Scanner.AnnotationScanner;

public class TicketManager {

    private final WorkerRegistry workerRegistry = new WorkerRegistry();
    private final Map<String, PriorityBlockingQueue<Ticket>> actionQueues = new ConcurrentHashMap<>();
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();


    // Should we use the top parent package where the user initialized ticketmanager?
    public TicketManager() {
        refreshRegistry();
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
        tryExecuteNextTicket(action_type);
        
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

        // get ticket type
        String ticket_action = ticket.getType();

        // make ticket active
        activeTickets.putIfAbsent(ticket_action, ticket);

        // this is where we create and run our worker! (AbstractWorker worker = workerRegistry.getWorker(ticket_action);)
        // • we then wait for the worker to be done 
        // • mark ticket as complete (+remove from active)
        // • then poll the next ticket.
        // for now simulate work being done
        CompletableFuture.runAsync(() -> waitThenCompleteFuture(ticket));
    }




    protected void completeTicket(Ticket ticket) {

        ticket.getFuture().complete(new TicketResult(new CaughtExceptions(), ticket, Status.FAILED, null));

        activeTickets.remove(ticket.getType());
    
        tryExecuteNextTicket(ticket.getType());
    }




    protected void waitThenCompleteFuture(Ticket ticket) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            System.out.println("waitThenCompleteFuture interrupted!");
        }

        completeTicket(ticket);
    }



    // UNSAFE!
    public final void refreshRegistry() {
        try {
            autoRegisterWorkeres("gg.vexi.Puppeteer");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to refresh worker registry", e);
        }
    }

    private void autoRegisterWorkeres(String packageName) throws IOException, ClassNotFoundException {

        
        List<Class<?>> workerClasses = AnnotationScanner.findAnnotatedClasses(packageName, RegisterWorker.class);
        
        for (Class<?> workerClass : workerClasses) {
            
            RegisterWorker annotation = workerClass.getAnnotation(RegisterWorker.class);
            String actionType = annotation.value().toLowerCase();
            if (actionType.isEmpty()) { actionType = workerClass.getSimpleName().toLowerCase(); }

            // add worker to registry and create a queue for it
            registerWorker(workerClass, actionType);
            actionQueues.put(actionType, new PriorityBlockingQueue<>());
        }
    }

    private void registerWorker(Class<?> workerClass, String actionType) {
        workerRegistry.registerWorker(actionType, () -> {
            try {
                actionQueues.put(actionType, new PriorityBlockingQueue<>());
                return (AbstractWorker) workerClass.getDeclaredConstructor().newInstance();
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                throw new RuntimeException("Failed to instantiate worker class", e);
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
