package gg.vexi.TicketSystem;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Ticket.ActionType;
import gg.vexi.TicketSystem.Ticket.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketPriority;
import gg.vexi.TicketSystem.Ticket.TicketResult;

public class TicketManager {

    private final ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> actionQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ActionType, Ticket> activeTickets = new ConcurrentHashMap<>();

    public TicketManager() {
        for (ActionType type : ActionType.values()) {
            actionQueues.put(type, new ConcurrentLinkedQueue<>());
        }
    }

    protected void addTicketToQueue(Ticket ticket) {

        actionQueues.get(ticket.getType()).add(ticket);
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
        // for now simulate work being done then return the future
        waitThenCompleteFuture(ticket);

    }

    protected void waitThenCompleteFuture(Ticket ticket) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("waitThenCompleteFuture interrupted!");
        }

        ticket.getFuture().complete(new TicketResult());

        activeTickets.remove(ticket.getType());

    }

    public ConcurrentLinkedQueue<Ticket> getQueue(ActionType type) {
        return actionQueues.get(type);
    }

    public ConcurrentHashMap<ActionType, ConcurrentLinkedQueue<Ticket>> getAllQueues() {
        return actionQueues;
    }


    public void setActive(Ticket ticket) {
        activeTickets.put(ticket.getType(), ticket);
    }

    public Ticket getActive(ActionType type) {
        return activeTickets.get(type);
    }

    public ConcurrentHashMap<ActionType, Ticket> getAllActive() {
        return activeTickets;
    }

}
