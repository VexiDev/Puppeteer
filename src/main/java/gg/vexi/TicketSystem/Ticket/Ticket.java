package gg.vexi.TicketSystem.Ticket;

public class Ticket {

    private final ActionType type;

    public Ticket(ActionType TicketType) {
        type = TicketType;
    }

    public ActionType getType() { return type; }

}
