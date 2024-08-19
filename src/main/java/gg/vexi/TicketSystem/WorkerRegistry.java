package gg.vexi.TicketSystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import gg.vexi.TicketSystem.core.AbstractWorker;

public class WorkerRegistry {
    private final Map<String, Supplier<AbstractWorker>> registry = new ConcurrentHashMap<>();

    public void registerWorker(String type, Supplier<AbstractWorker> factory) {
        registry.put(type, factory);
    }

    public AbstractWorker getWorker(String type) {
        Supplier<AbstractWorker> factory = registry.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("No process registered for type: " + type);
        }
        return factory.get();
    }
}
