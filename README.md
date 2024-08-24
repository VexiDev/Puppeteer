<!-- Project Title -->
# Puppeteer
<!-- Description -->
<span style="color: #00C2FF;">`TL;DR`</span> An asynchronous process manager for queue based task execution

Real description: <span style="color: #00C2FF;">`TO BE WRITTEN`

---
<!-- Installation -->
## Installation

Using Gradle: <span style="color: #00C2FF;">`TO BE WRITTEN`</span>

Using Maven: <span style="color: #00C2FF;">`TO BE WRITTEN`</span>

---

<!-- Usage -->
## How to use Puppeteer

### Create an instance of Puppeteer:
```java
public class MainClass {
    
    Puppeteer Puppeteer;

    public static void main(String[] args) {
        Puppeteer = new Puppeteer();
    }
}

```
- Puppeteer will attempt to register any workers annotated with `@RegisterWorker` when it is initialized.
- <span style="color: #FFC525;">`CURRENTLY UNSAFE`</span> Refresh worker list with <span style="color: #FFFFFF;">`Puppeteer.refreshRegistry()`</span>

    

### Define a task by implementing `AbstractWorker`:
```java
import gg.vexi.TicketSystem.annotations.RegisterWorker;
import gg.vexi.TicketSystem.Core.AbstractWorker;
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.Status;

@RegisterWorker("ReverseText") 
// @RegisterWorker will use classname if none provided (eg: "ExampleWorker")
public class ReverseTextWorker extends AbstractWorker {

    private String data;

    public ReverseTextWorker(Ticket ticket) {
        super(ticket);
    }

    // the entry point of a worker
    @Override
    public void main() {

        // ticket_parameters is set in AbstractWorker!
        String text = ticket_paramaters.get("text").getAsString();
        
        data = new StringBuilder(text).reverse().toString();

        super.complete(Status.SUCCESS, data);
    }
}
```

### Creating a `Ticket` for a `Worker`:
```java
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.Ticket.TicketPriority;

public static Ticket getExampleTicket(String text){

    // REQUIRED: The name of the worker you want to execute
    String workerName = "ReverseText";

    // OPTIONAL: Priority in queue 
    // - Defaults to NORMAL
    TicketPriority priority = TicketPriority.NORMAL;
    
    // OPTIONAL: Json paramaters for worker
    // - Defaults to empty JsonObject
    JsonObject parameters = new JsonObject();
    parameters.addProperty("text", text);

    // There are two ways to get your Ticket object:

    // 1. Create and retreive it using `createTicket()`:
    Ticket ticket = Puppeteer.createTicket(ticket_type, priority, parameters);

    // 2. Create and queue it immediately using `queueTicket()`:
    // Ticket ticket = Puppeteer.queueTicket(ticket_type, priority, parameters);

    return ticket;
}
```

### Running `Worker` with a `Ticket`:
```java
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.Status;

public class MainClass {

    private final Puppeteer Puppeteer;
    
    public static void main(String[] args) {
        Puppeteer = new Puppeteer();
        runExampleTask();
    }

    public static void runExampleTask() {

        // create the example ticket
        Ticke ticket = getExampleTicket("Hello World!");

        // Queue the ticket
        Puppeteer.queueTicket(ticket);

        // wait for the ticket's completable future to complete
        ticket.getFuture().thenAccept(result -> {

            if (result.isSuccessful()) {
                // result.getData() returns `Object data` which can be null!
                String reversedString = (String) result.getData()

                System.out.println(reversedString);
                // Outputs -> !dlroW olleH
            
            } else {
                System.out.println(String.format(
                    "Request was not successful! (STATUS:%s)",
                    result.getStatus().toString()
                ));
            }
        });
    }
}
```


<!-- Features -->
## Features
<span style="color: #00C2FF;">`TO BE WRITTEN`

<!-- Limitations -->
## Limitations
<span style="color: #00C2FF;">`TO BE WRITTEN`
- <span style="color: #00C2FF;">`TO BE WRITTEN`

---

<!-- Contributing -->
## Contributing
<span style="color: #00C2FF;">`TO BE WRITTEN`


<!-- License -->
## License
- __This project is licensed under the terms of the [MIT License](./LICENSE).__
---




<!-- Templates -->
## Templates
### Worker Template
```java
import gg.vexi.TicketSystem.annotations.RegisterWorker;
import gg.vexi.TicketSystem.Core.AbstractWorker;
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.Status;

@RegisterWorker("TemplateName") 
public class TemplateWorker extends AbstractWorker {

    private String data;

    public TemplateWorker(Ticket ticket) {
        super(ticket);
    }


    @Override
    public vod main() {
        data = "Template";
        super.complete(Status.SUCCESS, data);
    }
}
```

<!-- Advanced example -->
## More Examples

### Using ticket parameters in a worker:
```java
import gg.vexi.TicketSystem.annotations.RegisterWorker;
import gg.vexi.TicketSystem.Core.AbstractWorker;
import gg.vexi.TicketSystem.Core.Ticket;
import gg.vexi.TicketSystem.Status;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.List;

@RegisterWorker("getPrimesBetween") 
// @RegisterWorker will use classname if none provided (eg: "PrimesBetweenWorker")
public class PrimesBetweenWorker extends AbstractWorker {

    private int[] data;

    public PrimesBetweenWorker(Ticket ticket) {
        super(ticket);
    }

    // the entry point of a worker
    @Override
    public void main() {

        // ticket_parameters is an attribute of AbstractWorker
        int start = ticket_parameters.get("start_num").getAsInt();
        int end = ticket_parameters.get("end_num").getAsInt();

        data = getPrimeNumbersBetween(start, end);
        
        super.complete(Status.SUCCESS, data);
    }

    private int[] getPrimeNumbersBetween(int start, int end) {
        
        // Sieve of Eratosthenes
        boolean[] isPrime = new boolean[end];
        Arrays.fill(isPrime, true);

        isPrime[0] = false;
        isPrime[1] = false;

        int end_i = (int) Math.floor(Math.pow(end, 0.5));

        for (int i = 2; i <= end_i ; i++) {
            if(isPrime[i]) {
                for (int j = i*i; j <= end-1; j += i) {
                    isPrime[j] = false;
                }
            }
        }

        return IntStream.range(start, end)
                    .filter(n -> (n > start))
                    .filter(n -> isPrime[n])
                    .toArray();
    }
}
```

## Example...
## Example...
## Example...
## Example...
## Example...
## Example...