
> [!Warning]
> **This README is outdated!**
> *Documented code will no longer work!*

<!-- Project Title -->
# Puppeteer
<!-- Description -->

> [!Note]
> Puppeteer is an exploratory project into concurrency and thread management!
> Applying concepts from:
> • Doug Lea, "Concurrent Programming in Java, 2nd Edition"
> • ...put rest here

<span style="color: #00C2FF;">`TL;DR`</span> A process manager for queued execution of repeating asynchronous tasks

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
 - `Puppet` : The worker class where you define a task 
 - `Ticket` : The object used to request a Puppet's execution
 - `TicketResult` : The returned object when a Puppet is done executing
    - Puppeteer will always return a TicketResult even if the puppet fails

### Define a `Puppet` by extending `Puppet` :
```java
package org.example;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ResultStatus;

public class ReverseTextPuppet extends Puppet {

    private String data;

    public ReverseTextPuppet(Ticket ticket) {
        super(ticket);
    }

    // main() is the entry point to a puppet
    @Override
    public void main() {
        // Access your ticket parameters with `parameters`
        String text = parameters.get("text").getAsString();
        
        data = new StringBuilder(text).reverse().toString();
        
        super.complete(ResultStatus.SUCCESS, data);
    }
}
```



### Create an instance of `Puppeteer` and register your `Puppets` :
```java
public class MainClass {
    
    static final Puppeteer puppeteer = new Puppeteer();

    public static void main(String[] args) {
        // Register your puppets
        puppeteer.registerPuppet(ReverseTextPuppet.class, "ReverseText");
    }
}
```
- Puppeteer **may** have annotation based registration in the future to make puppet registration simpler



### Create a `Ticket` to schedule a `Puppet` :
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

    // There are two ways to get a Ticket object:

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
import gg.vexi.Puppeteer.ResultStatus;

public class MainClass {

    static final Puppeteer puppeteer = new Puppeteer();
    
    public static void main(String[] args) {
        puppeteer.registerPuppet(ReverseTextPuppet.class, "ReverseText");
        runExampleTask();
    }

    public static void runExampleTask() {
        // create the example ticket
        Ticket ticket = getExampleTicket("Hello World!");

        // Queue the ticket
        puppeteer.queueTicket(ticket);

        // Get the ticket's CompletableFuture
        CompletableFuture<Result> future = ticket.getFuture();

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


        // wait for future to complete [BLOCKING]
        future.join();
        // Note: Generally Puppeteer is not meant to be used with blocking operations like 
        //       join() since it would usually be running in a loop
    }

    // Defined in previous step
    public static void getExampleTicket(String type) { ... }
}
```



<!-- Features -->
## Feature List
<span style="color: #00C2FF;">`TO BE WRITTEN`</span>


<!-- Limitations -->
## Limitations
<span style="color: #00C2FF;">`TO BE WRITTEN`</span>

---

<!-- Planned features -->
## Planned features

<ul style="list-style-type: none; padding-left: 20px;">


<li style="margin-bottom: 10px;">
<details>
    <summary style="cursor: pointer; font-weight: bold;">Multiple of the same puppet type running in parallel</summary>
        <ul style="list-style-type: disc; padding-left: 20px;">
    <li>Currently only 1 puppet of each type can perform at a time
    <ul style="list-style-type: circle; padding-left: 20px;">
        <br><li><i>Allow n instances of a Puppet to perform concurrently</i></li>
                    <ul style="list-style-type: square; padding-left: 20px;">
        <li>We would then poll for the next Puppet (if any) in the queue when a slot opens</li>
        <br></ul>
    </li>
    </ul>
</details>
</li>
</li>


<li style="margin-bottom: 10px;">
<details>
    <summary style="cursor: pointer; font-weight: bold;">Execution of different Puppet types with one in depth ticket</summary>
    <ul style="list-style-type: disc; padding-left: 20px;">
    <li>Currently a ticket can only run one puppet of a given type
        <ul style="list-style-type: circle; padding-left: 20px;">
        <br>
        <li><i>Maybe it could run multiple puppets?</i></li>
        <li><i>Maybe multiple different types of puppets?</i></li>
        <li><i>Supporting both sequential and parallel execution of Puppets to create a performance where Puppets can pass results down the chain</i></li>
        <br>
        </ul>
    </li>
    </ul>
</details>
</li>

</ul>


<span style="color: #00C2FF;">`MORE TO BE WRITTEN`</span>

<!-- License -->
## License
- __This project is licensed under the terms of the [Apache 2.0 License](./LICENSE).__


<!-- Contributing -->
## Contributing
<span style="color: #00C2FF;">`TO BE WRITTEN`</span>

---



<!-- Templates -->
## Templates
### Puppet Template
```java
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.ResultStatus;

public class TemplatePuppet extends Puppet {

    private String data;

    public TemplatePuppet(Ticket ticket) {
        super(ticket);
    }


    @Override
    public void main() {
        // - access ticket parameters with `parameter`
        // - use the builtin ProblemHandler to automatically log and pass exceptions to
        //   ticket holders
        data = "Template";
        super.complete(PuppetStatus.SUCCESS, data);
    }
}
```



<!-- Advanced example -->
## Examples
### Get numbers between two primes using segmented Sieve of Eratosthenes
```java
package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Status;

public class PrimesBetweenPuppet extends Puppet {

    private long[] data;

    public PrimesBetweenPuppet(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {

        long start = ticket_parameters.get("start_num").getAsLong();
        long end = ticket_parameters.get("end_num").getAsLong();
        
        // If this method were to fail it would be automatically handled by the
        // ProblemHandler and the puppet would exit cleanly with a failed state 
        data = getPrimeNumbersBetween(start, end);

        super.complete(PuppetStatus.SUCCESS, data);
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

### [other examples here]
