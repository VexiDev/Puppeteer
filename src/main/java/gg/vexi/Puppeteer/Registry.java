package gg.vexi.Puppeteer;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.PuppetNotFound;

public class Registry {
    private final Map<String, Function<Ticket, Puppet>> registry = new ConcurrentHashMap<>();

    public void registerPuppet(String name, Function<Ticket, Puppet> factory) {
        registry.put(name.toLowerCase(Locale.ROOT), factory);
        // debug output
        // System.out.println(String.format("Registered puppet %s%s%s with associated type %s%s%s",
        // "\033[0;32m", type.toLowerCase(), "\033[0m", "\033[0;92m", factory.getClass().getSimpleName(),
        // "\033[0m"));
    }

    public Puppet retreive(Ticket ticket) {
        String name = ticket.puppet();
        if (contains(name))
            throw new PuppetNotFound(String.format("\"%s\" is not a registered puppet", name));
        Function<Ticket, Puppet> constructor = registry.get(name.toLowerCase(Locale.ROOT));
        return constructor.apply(ticket);
    }

    public boolean contains(String puppetName) {
        return registry.containsKey(puppetName);
    }

    public Map<String, Function<Ticket, Puppet>> all() {
        return registry;
    }

}
