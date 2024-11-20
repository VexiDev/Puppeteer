package gg.vexi.Puppeteer;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Exceptions.PuppetNotFound;

public class Registry {
    private final Map<String, Function<Ticket<?>, Puppet<?>>> registry = new ConcurrentHashMap<>();

    public <T> void registerPuppet(String name, Class<? extends Puppet<T>> puppetClass) {
        Function<Ticket<T>, Puppet<T>> factory = (ticket) -> {
            try {
                return puppetClass
                    .getDeclaredConstructor(Ticket.class)
                    .newInstance(ticket);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException
                | SecurityException | InvocationTargetException e) {
                throw new RuntimeException("Failed to instantiate puppet class", e);
            }
        };

        // safe cast since the function maintains type T
        @SuppressWarnings("unchecked")
        Function<Ticket<?>, Puppet<?>> wildcardFactory =
            (Function<Ticket<?>, Puppet<?>>) (Function<?, ?>) factory;

        registry.put(name.toLowerCase(Locale.ROOT), wildcardFactory);

        // debug output
        //System.out.println(String.format("Registered puppet %s%s%s with associated type %s%s%s",
        //   "\033[0;32m", name.toLowerCase(), "\033[0m", "\033[0;92m",
        //    factory.getClass().getSimpleName(),
        //    "\033[0m"));
    }

    @SuppressWarnings("unchecked")
    public <T> Puppet<T> retreive(Ticket<?> ticket) {
        String name = ticket.puppet().toLowerCase(Locale.ROOT);
        if (!contains(name))
            throw new PuppetNotFound(String.format("\"%s\" is not a registered puppet", name));
        Function<Ticket<?>, Puppet<?>> constructor = registry.get(name.toLowerCase(Locale.ROOT));
        // safe cast since function maintains the type relationship between puppet and ticket
        return (Puppet<T>) constructor.apply(ticket);
    }

    public boolean contains(String puppetName) {
        return this.registry.containsKey(puppetName);
    }

    public Map<String, Function<Ticket<?>, Puppet<?>>> all() {
        return this.registry;
    }

}
