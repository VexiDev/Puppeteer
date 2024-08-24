package gg.vexi.Puppeteer;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import gg.vexi.Puppeteer.Core.AbstractWorker;

public class WorkerRegistry {
    private final Map<String, Supplier<AbstractWorker>> registry = new ConcurrentHashMap<>();

    public void registerWorker(String type, Supplier<AbstractWorker> factory) {
        registry.put(type.toLowerCase(Locale.ROOT), factory);
        // debug output
        // System.out.println(String.format("Registered worker %s%s%s with associated type %s%s%s", "\033[0;32m", type.toLowerCase(), "\033[0m", "\033[0;92m", factory.getClass().getSimpleName(), "\033[0m"));
    }

    public AbstractWorker getWorker(String type) {
        Supplier<AbstractWorker> factory = registry.get(type.toLowerCase(Locale.ROOT));
        if (factory == null) {
            throw new IllegalArgumentException("No process registered for type: " + type);
        }
        return factory.get();
    }

    public Map<String, Supplier<AbstractWorker>> getFullRegistry() {
        return registry;
    }

    public Set<String> getAllActionTypes() {
        return registry.keySet();
    }
}
