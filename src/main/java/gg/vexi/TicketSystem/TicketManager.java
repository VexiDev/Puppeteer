package gg.vexi.TicketSystem;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
import gg.vexi.TicketSystem.annotations.AssociatedActionType;
import gg.vexi.TicketSystem.annotations.scanner.AnnotationScanner;
import gg.vexi.TicketSystem.core.AbstractWorker;
import gg.vexi.TicketSystem.ticket.Ticket;
import gg.vexi.TicketSystem.ticket.TicketPriority;
import gg.vexi.TicketSystem.ticket.TicketResult;

public class TicketManager {

    private final WorkerRegistry workerRegistry = new WorkerRegistry();
    private final Map<String, PriorityBlockingQueue<Ticket>> actionQueues = new ConcurrentHashMap<>();
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();

    public TicketManager() {
        try {
            autoRegisterWorkeres("gg.vexi.TicketSystem");  // Replace with your package name
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to auto-register processes", e);
        }
    }

    private void autoRegisterWorkeres(String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> workerClasses = AnnotationScanner.findAnnotatedClasses(packageName, AssociatedActionType.class);
        
        for (Class<?> workerClass : workerClasses) {
            AssociatedActionType annotation = workerClass.getAnnotation(AssociatedActionType.class);
            String actionType = annotation.value();
            registerWorker(workerClass, actionType);
            // create a queue for this actiontype
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


    protected void addTicketToQueue(Ticket ticket) {
        actionQueues.get(ticket.getType()).offer(ticket);
    }

    public Ticket createTicket(String action_type, TicketPriority ticket_priority, JsonObject ticket_parameters) {

        CompletableFuture<TicketResult> ticket_future = new CompletableFuture<>();
        Ticket ticket = new Ticket(action_type, ticket_priority, ticket_parameters, ticket_future); // create our ticket

        return ticket;
    }




    // overload if customer already created ticket themselves with createTicket()
    public void queueTicket(Ticket ticket) {

        addTicketToQueue(ticket);
        tryExecuteNextTicket(ticket.getType());
    
    }




    public Ticket queueTicket(String action_type, TicketPriority ticket_priority, JsonObject ticket_parameters) {

        Ticket ticket = createTicket(action_type, ticket_priority, ticket_parameters); // create ticket

        addTicketToQueue(ticket);
        tryExecuteNextTicket(action_type);

        return ticket;
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
