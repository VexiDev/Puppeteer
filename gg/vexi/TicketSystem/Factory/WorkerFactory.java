package gg.vexi.TicketSystem.Factory;

import gg.vexi.TicketSystem.TicketManager;
import gg.vexi.TicketSystem.Enums.ActionType;
import gg.vexi.TicketSystem.Worker.WorldCreationWorker;
import gg.vexi.TicketSystem.Worker.WorldLoadWorker;

public class WorkerFactory {
    private static TicketManager ticketManager;

    public static void setTicketManager(TicketManager manager) {
        ticketManager = manager;
    }

    public static AbstractWorker createWorker(ActionType actionType) {
        switch (actionType) {
            case WORLD_CREATION:
                return new WorldCreationWorker(ticketManager);
            case WORLD_LOAD:
                return new WorldLoadWorker(ticketManager);
            default:
                throw new IllegalArgumentException("Unknown action type: " + actionType);
        }
    }
}