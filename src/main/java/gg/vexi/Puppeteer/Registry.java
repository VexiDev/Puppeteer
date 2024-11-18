package gg.vexi.Puppeteer;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;

public class Registry {
    private final Map<String, Function<Ticket, Puppet>> registry = new ConcurrentHashMap<>();

    public void registerPuppet(String type, Function<Ticket, Puppet> factory) {
        registry.put(type.toLowerCase(Locale.ROOT), factory);
        // debug output
        // System.out.println(String.format("Registered puppet %s%s%s with associated type %s%s%s", "\033[0;32m", type.toLowerCase(), "\033[0m", "\033[0;92m", factory.getClass().getSimpleName(), "\033[0m"));
    }

    public Puppet getPuppet(Ticket ticket) {
        String type = ticket.getType();
        Function<Ticket, Puppet> constructor = registry.get(type.toLowerCase(Locale.ROOT));
        if (constructor == null) {
            throw new IllegalArgumentException("No puppet registered for type: " + type);
        }
        return constructor.apply(ticket);
    }

    public boolean has(String type) {
        return registry.containsKey(type);
    }
    
    public Map<String, Function<Ticket, Puppet>> getFullRegistry() {
        return registry;
    }

    public Set<String> getAllActionTypes() {
        return registry.keySet();
    }
}
