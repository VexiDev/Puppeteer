<!-- Project Title -->
# Puppeteer
<!-- Description -->

> [!WARNING]
> Puppeteer is a learning project for exploring concurrency in java!
> In almost all cases it is recommended to just use `java.util.concurrent.ExecutorService`

<span style="color: #00C2FF;">`TL;DR`</span> A process manager for sequential or concurrent execution of repeating asynchronous tasks

Real description: <span style="color: #00C2FF;">`TO BE WRITTEN`</span>

---
<!-- Installation -->
## Installation

Using Gradle: <span style="color: #00C2FF;">`TO BE WRITTEN`</span>

Using Maven: <span style="color: #00C2FF;">`TO BE WRITTEN`</span>

---

<!-- Usage -->
## How to use Puppeteer

#### Definitions:
 - `Puppeteer` : The master class of Puppeteer
 - `Puppet` : The class where you define a puppet's task
    - `Performance` : A name for the task a puppet "performes"
 - `Ticket` : The format of a request to Puppeteer 

### Define a `Puppet` by extending `AbstractPuppet` :
```java
package org.example;

import gg.vexi.Puppeteer.Core.AbstractPuppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;

@RegisterPuppet("ReverseText") 
// @RegisterPuppet will use classname if none provided (eg: "ReverseTextPuppet")
public class ReverseTextPuppet extends AbstractPuppet {

    private String data;

    public ReverseTextPuppet(Ticket ticket) {
        super(ticket);
    }

    // main() is the entry point to a puppet
    @Override
    public void main() {
        // ticket_parameters is defined in AbstractPuppet!
        String text = ticket_parameters.get("text").getAsString();
        
        data = new StringBuilder(text).reverse().toString();
        
        super.complete(Status.SUCCESS, data);
    }
}
```



### Create an instance of `Puppeteer` :
```java
public class MainClass {
    
    static final Puppeteer puppeteer = new Puppeteer("org.example");

    public static void main(String[] args) {
        // ...
    }
}
```
- Puppeteer will attempt to register any puppets annotated with `@RegisterPuppet` that are __within the given package__ (eg: `org.example`)



### Create a `Ticket` for the `Puppet` :
```java
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Ticket.TicketPriority;

public static Ticket getExampleTicket(String text){

    // REQUIRED: The name of the puppet you want to execute
    String puppetName = "ReverseText";

    // OPTIONAL: Priority in queue 
    // - Defaults to NORMAL
    TicketPriority priority = TicketPriority.NORMAL;

    // OPTIONAL: Json paramaters for puppet
    // - Defaults to empty JsonObject
    JsonObject parameters = new JsonObject();
    parameters.addProperty("text", text);

    // There are two ways to get your Ticket object:

    // 1. Create and retreive it using `createTicket()`:
    Ticket ticket = puppeteer.createTicket(puppetName, priority, parameters);

    // 2. Create and queue it immediately using `queueTicket()`:
    // Ticket ticket = puppeteer.queueTicket(ticket_type, priority, parameters);

    return ticket;
}
```



### Running a `Puppet` using a `Ticket` :
```java
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Status;

public class MainClass {

    static final Puppeteer puppeteer = new Puppeteer("org.example");
    
    public static void main(String[] args) {
        runExampleTask();
    }

    public static void runExampleTask() {
        // create the example ticket
        Ticket ticket = getExampleTicket("Hello World!");

        // Queue the ticket
        puppeteer.queueTicket(ticket);

        // Get the ticket's CompletableFuture
        CompletableFuture<TicketResult> future = ticket.getFuture();

        // Once the performance is complete process the result
        future.thenAccept(result -> {

            if (result.isSuccessful()) {
                // result.getData() returns `Object data` which can be null!
                String reversedString = (String) result.getData();
                System.out.println(reversedString); 
                // Outputs -> !dlroW olleH

            } else {
                System.out.println(String.format(
                    "Request was not successful! (STATUS:%s)\nHas Exceptions: %b",
                    result.getStatus().toString(), result.hasExceptions()
                ));
            }
        });

        // wait for future to complete (BLOCKING)
        future.join();
    }

    // Defined in previous step
    public static void getExampleTicket(String type) { ... }
}
```



<!-- Features -->
## Features
<span style="color: #00C2FF;">`TO BE WRITTEN`</span>
- `Queues` : 
- `ExceptionHandler` : 
- `Annotation` : 



<!-- Limitations -->
## Limitations
<span style="color: #00C2FF;">`TO BE WRITTEN`</span>

---

<!-- Planned features -->
## Planned features

<ul style="list-style-type: none; padding-left: 20px;">


<li style="margin-bottom: 10px;">
<details>
    <summary style="cursor: pointer; font-weight: bold;">Multiple concurrent instances of a single puppet type</summary>
        <ul style="list-style-type: disc; padding-left: 20px;">
    <li>Currently only 1 puppet of each type can perform at a time
    <ul style="list-style-type: circle; padding-left: 20px;">
        <br><li><i>Allow n instances of a Puppet to perform concurrently</i></li>
                    <ul style="list-style-type: square; padding-left: 20px;">
        <li>We would then poll for the next Puppet (if any) in the queue when a slot opens</li>
        <br></ul>
        <li><i>Specified in the @RegisterPuppet annotation?</i>
        <ul style="list-style-type: square; padding-left: 20px;">
        <li>eg: @RegisterPuppet("Example", 3) --> 3 instances of Example can run concurrently</li>
        </ul>
        <br>
        </li>
        </ul>
    </li>
    </ul>
</details>
</li>
</li>


<li style="margin-bottom: 10px;">
<details>
    <summary style="cursor: pointer; font-weight: bold;">Allow for multi-puppet tickets that return a bundle of multiple performance results</summary>
    <ul style="list-style-type: disc; padding-left: 20px;">
    <li>Currently a ticket can only run one puppet of a given type
        <ul style="list-style-type: circle; padding-left: 20px;">
        <br>
        <li><i>Maybe it could run multiple puppets?</i></li>
        <li><i>Maybe multiple different types of puppets?</i></li>
        <br>
        </ul>
    </li>
    </ul>
</details>
</li>


<li style="margin-bottom: 10px;">
<details>
    <summary style="cursor: pointer; font-weight: bold;">Manual registration of Puppets (without class scanning)</summary>
    <ul style="list-style-type: disc; padding-left: 20px;">
    <li>Currently a Puppet can only be registered using the @RegisterPuppet annotation
        <ul style="list-style-type: circle; padding-left: 20px;">
        <br>
        <li><i>Make the registerPuppet() method  public</i></li>
        <li><i>Make the annotation scanning optional</i></li>
        <br>
        </ul>
    </li>
    </ul>
</details>
</li>

</ul>


<span style="color: #00C2FF;">`MORE TO BE WRITTEN`</span>

<!-- Contributing -->
## Contributing
<span style="color: #00C2FF;">`TO BE WRITTEN`</span>



<!-- License -->
## License
- __This project is licensed under the terms of the [Apache 2.0 License](./LICENSE).__

---



<!-- Templates -->
## Templates
### Puppet Template
```java
import gg.vexi.Puppeteer.annotations.RegisterPuppet;
import gg.vexi.Puppeteer.Core.AbstractPuppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Status;

@RegisterPuppet("TemplateName") 
public class TemplatePuppet extends AbstractPuppet {

    private String data;

    public TemplatePuppet(Ticket ticket) {
        super(ticket);
    }


    @Override
    public void main() {
        data = "Template";
        super.complete(Status.SUCCESS, data);
    }
}
```



<!-- Advanced example -->
## More Examples
> These are implementations I made while testing Puppeteer and figuring what a good example would be!<br>My primary implementation for Puppeteer is for queued asynchronous execution of I/O tasks to avoid I/O corruption during concurrent operation or extreme I/O load.
### Get primes between any 2 whole numbers using a segemented implementation of the Seive of Eratosthenes
- Could support parsing values above MAX_LONG because we sieve using segments however since method arguments use long we are limited at method call. This can probably be circumvented by using a string?
```java
package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

import gg.vexi.Puppeteer.Core.AbstractPuppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Status;
import gg.vexi.Puppeteer.annotations.RegisterPuppet;

@RegisterPuppet("getPrimesBetween") 
public class PrimesBetweenPuppet extends AbstractPuppet {

    private long[] data;

    public PrimesBetweenPuppet(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {

        long start = ticket_parameters.get("start_num").getAsLong();
        long end = ticket_parameters.get("end_num").getAsLong();

        data = getPrimeNumbersBetween(start, end);

        super.complete(Status.SUCCESS, data);
    }


    private long[] getPrimeNumbersBetween(long start, long end) {
        if (start < 2) start = 2;
        if (end < start) return new long[0];

        int segmentSize = 1 << 20; // Use a segment size of 1M bits (128KB)
        BitSet isPrime = new BitSet(segmentSize);
        List<Long> primes = new ArrayList<>();

        // Generate small primes up to sqrt(end)
        long sqrtEnd = Math.min((long) Math.sqrt(end), Integer.MAX_VALUE - 1);
        BitSet smallPrimes = new BitSet((int) sqrtEnd + 1);
        smallPrimes.set(2, (int) sqrtEnd + 1);
        for (long i = 2; i * i <= sqrtEnd; i++) {
            if (smallPrimes.get((int) i)) {
                for (long j = i * i; j <= sqrtEnd; j += i) {
                    smallPrimes.clear((int) j);
                }
            }
        }

        // Main segmented sieve loop
        for (long low = start; low <= end; low += segmentSize) {
            isPrime.clear();
            isPrime.set(0, segmentSize);
            long high = Math.min(low + segmentSize - 1, end);

            // Sieve the segment
            for (long i = 2; i <= sqrtEnd; i++) {
                if (smallPrimes.get((int) i)) {
                    long loLim = Math.max(i * i, (low + i - 1) / i * i);
                    for (long j = loLim; j <= high; j += i) {
                        isPrime.clear((int) (j - low));
                    }
                }
            }

            // Collect primes from this segment
            final long segmentLow = low;
            IntStream.range(0, segmentSize)
                .filter(isPrime::get)
                .mapToLong(i -> i + segmentLow)
                .filter(i -> i <= high)  // Ensure we don't exceed the high bound
                .forEach(primes::add);
        }

        // Handle the special case of 2 if it's in range
        if (start <= 2 && 2 <= end) {
            primes.add(0, 2L);
        }

        // Convert List<Long> to long[]
        return primes.stream().mapToLong(Long::longValue).toArray();
    }
}
```

## Example...
## Example...
## Example...
## Example...
## Example...
## Example...
