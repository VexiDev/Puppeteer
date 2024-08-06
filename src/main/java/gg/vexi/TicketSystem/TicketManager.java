package gg.vexi.TicketSystem;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Exceptions.CaughtExceptions;
import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketPriority;
import gg.vexi.TicketSystem.Ticket.TicketResult;

public class TicketManager {

    private final ConcurrentHashMap<ActionType, PriorityBlockingQueue<Ticket>> actionQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ActionType, Ticket> activeTickets = new ConcurrentHashMap<>();

    public TicketManager() {
        for (ActionType type : ActionType.values()) {
            actionQueues.put(type, new PriorityBlockingQueue<>());
        }
    }

    protected void addTicketToQueue(Ticket ticket) {

        actionQueues.get(ticket.getType()).offer(ticket);
    }

    public Ticket createTicket(ActionType action_type, TicketPriority ticket_priority, JsonObject ticket_parameters) {

        CompletableFuture<TicketResult> ticket_future = new CompletableFuture<>();
        Ticket ticket = new Ticket(action_type, ticket_priority, ticket_parameters, ticket_future); // create our ticket

        return ticket;
    }

    // overload if customer already created ticket themselves with createTicket()
    public void queueTicket(Ticket ticket) {

        addTicketToQueue(ticket);
        tryExecuteNextTicket(ticket.getType());
    
    }

    public Ticket queueTicket(ActionType action_type, TicketPriority ticket_priority, JsonObject ticket_parameters) {

        Ticket ticket = createTicket(action_type, ticket_priority, ticket_parameters); // create ticket

        addTicketToQueue(ticket);
        tryExecuteNextTicket(action_type);

        return ticket;
    }

    protected Ticket nextTicket(ActionType type) {

        if (getActive(type) == null) {
            
            return actionQueues.get(type).poll();

        } else {

            return null;
        }
    }

    protected void tryExecuteNextTicket(ActionType action_type) {

        Ticket next_ticket = nextTicket(action_type);

        if (next_ticket == null) { return; }

        executeTicket(next_ticket);
    }

    protected void executeTicket(Ticket ticket) {

        // get ticket type
        ActionType ticket_type = ticket.getType();

        // make ticket active
        activeTickets.putIfAbsent(ticket_type, ticket);

        // this is where we create and run our worker!
        // • we then wait for the worker to be done 
        // • mark ticket as complete (+remove from active)
        // • then poll the next ticket.
        // for now simulate work being done
        CompletableFuture.runAsync(() -> waitThenCompleteFuture(ticket));
    }

    protected void completeTicket(Ticket ticket) {

        ticket.getFuture().complete(new TicketResult<JsonObject>(new CaughtExceptions(), ticket, Status.FAILED, null));

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
    public PriorityBlockingQueue<Ticket> getQueue(ActionType type) {
        return actionQueues.get(type);
    }

    public ConcurrentHashMap<ActionType, PriorityBlockingQueue<Ticket>> getAllQueues() {
        return actionQueues;
    }

    public Ticket getActive(ActionType type) {
        return activeTickets.get(type);
    }

    public ConcurrentHashMap<ActionType, Ticket> getAllActive() {
        return activeTickets;
    }

    // setters
    public void setActive(Ticket ticket) {
        activeTickets.put(ticket.getType(), ticket);
    }

}
