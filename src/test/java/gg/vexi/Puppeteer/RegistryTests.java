package gg.vexi.Puppeteer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ExamplePuppets.ExamplePuppet_String;
import gg.vexi.Puppeteer.Ticket.TicketPriority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

//TODO: Happy/Unhappy paths

class _Registry {

    Registry reg;
    Map<String, Function<Ticket<?>, Puppet<?>>> reg_map;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        reg = new Registry();

        // get the registry map field reflection
        Field reg_map_field = reg.getClass().getDeclaredField("registry");
        reg_map_field.setAccessible(true);
        reg_map = (Map<String, Function<Ticket<?>, Puppet<?>>>) reg_map_field.get(reg);
    }

    @Test
    public void testRegister() {
        // register puppet
        reg.registerPuppet("test", ExamplePuppet_String.class);
        // check if registry map size matches
        assertEquals(1, reg_map.size(), "Size mismatch");
        // check if registry map contains key "test"
        assertTrue(reg_map.containsKey("test"), "Key missing");
        // TODO: Check if value is expected object?
    }

    @Test
    public void testRetrieve() {
        // register puppet
        reg.registerPuppet("test", ExamplePuppet_String.class);
        Puppet<String> p = reg.retreive(
            new Ticket<String>("test", TicketPriority.NORMAL, new HashMap<>(), new CompletableFuture<>())
        );
        // check not null
        assertNotNull(p);
        // check correct puppet type
        assertTrue(
            p instanceof ExamplePuppet_String, "Retrieved puppet is not correct registered puppet implementation"
        );
    }

    @Test
    public void testContains() {
        // register puppet
        reg.registerPuppet("test", ExamplePuppet_String.class);
        assertEquals(reg.contains("test"), reg_map.containsKey("test"));
    }
}
