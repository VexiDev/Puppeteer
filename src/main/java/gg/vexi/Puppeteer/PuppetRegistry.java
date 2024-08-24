package gg.vexi.Puppeteer;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import gg.vexi.Puppeteer.Core.AbstractPuppet;

public class PuppetRegistry {
    private final Map<String, Supplier<AbstractPuppet>> registry = new ConcurrentHashMap<>();

    public void registerPuppet(String type, Supplier<AbstractPuppet> factory) {
        registry.put(type.toLowerCase(Locale.ROOT), factory);
        // debug output
        // System.out.println(String.format("Registered puppet %s%s%s with associated type %s%s%s", "\033[0;32m", type.toLowerCase(), "\033[0m", "\033[0;92m", factory.getClass().getSimpleName(), "\033[0m"));
    }

    public AbstractPuppet getPuppet(String type) {
        Supplier<AbstractPuppet> factory = registry.get(type.toLowerCase(Locale.ROOT));
        if (factory == null) {
            throw new IllegalArgumentException("No process registered for type: " + type);
        }
        return factory.get();
    }

    public Map<String, Supplier<AbstractPuppet>> getFullRegistry() {
        return registry;
    }

    public Set<String> getAllActionTypes() {
        return registry.keySet();
    }
}
