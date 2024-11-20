package gg.vexi.Puppeteer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ExamplePuppets.ExamplePuppet_String;
import gg.vexi.Puppeteer.Ticket.TicketPriority;

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

    }

    @Test
    public void testRetrieve() {

    }

    @Test
    public void testContains() {

    }

}
