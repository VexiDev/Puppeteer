<!-- Project Title -->
# Puppeteer
<!-- Description -->

> [!Note]
> Puppeteer is __not__ polished, safe or intended for production use<br>
> This project followed `Test Driven Development`(TDD) for code implementation<br>
> During development I read:<br>
> • Doug Lea, "Concurrent Programming in Java," 2nd Edition"<br>
> • Javier Fernández González, "Mastering Concurrency Programming with Java 8"

`TL;DR`: Manager for non-blocking sequential execution of predefined tasks


Puppeteer is a thread manager for sequential asynchronous execution of tasks.
Only one of each registered task type may execute concurrently while any
additional requests for a specific type wait in a PriorityQueue.

Puppeteer's original purpose was to be used within a concurrent environment 
and handle blocking tasks in a seperate thread while only letting one of these
tasks be executed at a time since they access shared state and having multiple
of these tasks running concurrently would lead to corrupted states.

While working on Puppeteer Ive fallen into the rabbithole of Concurrency
and Parallelism and transitioned Puppeteer into a more theoretical exploration
to help me better understand the general concepts of concurrency and thread safety.
I also spent a good amount of time looking into how threads and concurrency are 
managed by the JVM in Java.

Puppeteer is technically complete but it is still missing many features.
It has also become very messy during development as I learned new concepts and 
attempted to apply them without a complete understanding of them. 

---

<!-- License -->
## License
- __This project is licensed under the terms of the [Apache 2.0 License](./LICENSE).__


---
<!-- Installation -->
<!-- ## Installation
<!-- 
<!-- Using Gradle: <span style="color: #00C2FF;">`TO BE WRITTEN`</span>
<!-- 
<!-- Using Maven: <span style="color: #00C2FF;">`TO BE WRITTEN`</span>
<!-- 
<!-- --- -->

<!-- Usage -->
## How to use Puppeteer
#### Definitions:
 - `Puppet<T>` : The parent class that all puppets extend from
    - I know, "boo inheritance!👎"
 - `Ticket<T>` : The object used to request a puppet's execution and get its result
 - `Result<T>` : The object returned by a puppet's future.

### Define a `Puppet` by extending `Puppet<T>` :
```java
package org.example;

import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Core.ResultStatus;
 
// Puppet<String> indicated the puppet will take in a Ticket<String> 
// and will return a Result<String> object when completed
public class ReverseTextPuppet extends Puppet<String> {

    private String data;

    public ReverseTextPuppet(Ticket<String> ticket) {
        super(ticket);
    }

    // main() is the entry point to a puppet
    @Override
    public void main() {
        
        // Access the ticket's parameters with `parameters`
        // parameters is a Map<String, Object> so requires casting
        String text = (String) parameters.get("text");
        
        data = new StringBuilder(text).reverse().toString();

        // Calling super.complete() will exit the puppet 
        // and return a Result object to the Ticket future
        super.complete(ResultStatus.SUCCESS, data);
    }
}
```



### Create an instance of `Puppeteer` and register your `Puppets` :
```java
import gg.vexi.Puppeteer.Puppeteer;

public class MainClass {
    
    static final Puppeteer puppeteer = new Puppeteer();

    public static void main(String[] args) {
        // Register your puppet
        puppeteer.registerPuppet("ReverseText", ReverseTextPuppet.class);
    }
}
```


### Create a `Ticket<T>` to schedule a `Puppet` :
```java
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Ticket.TicketPriority;

public static Ticket<String> getExampleTicket(String text){

    // REQUIRED: The name of the puppet you want to execute
    String puppetName = "ReverseText";

    // OPTIONAL: Priority in queue 
    // - Defaults to NORMAL
    TicketPriority priority = TicketPriority.NORMAL;

    // OPTIONAL: Map<String, Objec t> of paramaters for puppet
    // - Defaults to an empty map
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("text", text);

    // There are two ways to get a Ticket object:
   

    // 1. Create and retreive it using `createTicket()`:
    // Throws an exception:
    //  - If the ticket doesn't exist
    Ticket<String> ticket = puppeteer.createTicket(puppetName, priority, parameters);

    // 2. Create and queue it immediately using `queueTicket()`:
    // Throws an exception:
    //  - If the ticket doesn't exist
    //  - If the expected ticket type does not match the requested Puppet's type
    // Ticket<String> ticket = puppeteer.queueTicket(puppetName, priority, parameters);


    return ticket;
}
```



### Running a `Puppet` using a `Ticket` :
```java
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Core.ResultStatus;

public class MainClass {

    static final Puppeteer puppeteer = new Puppeteer();
    
    public static void main(String[] args) {
        puppeteer.registerPuppet("ReverseText", ReverseTextPuppet.class);
        runExampleTask();
    }

    public static void runExampleTask() {
        // create the example ticket
        Ticket<String> ticket = getExampleTicket("Hello World!");

        // Queue the ticket
        // Throws an exception:
        //  - If the ticket doesn't exist
        //  - If ticket doesn't match the exepcted type for the puppet it's requesting
        puppeteer.queueTicket(ticket);

        // Get the ticket's CompletableFuture
        CompletableFuture<Result<String>> future = ticket.getFuture();

        // Once the performance is complete process the result
        future.thenAccept(result -> {

            if (result.isSuccessful()) {

                String reversedString = result.getData();
                System.out.println(reversedString); 
                // Outputs -> !dlroW olleH

            } else {
                System.out.println(String.format(
                    "Request was not successful! (STATUS:%s)\nHas Exceptions: %b",
                    result.getStatus().toString(), result.hasExceptions()
                ));
            }                
            if (result.hasExceptions()) {
                // ProblemHandler is the builtin Puppeteer exception handler and
                // will wrap all Throwables with additional context
                List<Problem> l = result.problemHandler().getAll();
                for (Problem p : l) System.out.println(p);
            }

        });


        // wait for future to complete [BLOCKING]
        future.join();
        // Note: Generally Puppeteer is not meant to be used with blocking operations like 
        //       since it's intead to be running within a game loop
    }

    // Defined in previous step
    public static void getExampleTicket(String type) { ... }
}
```

### Using the `ProblemHandler`
- The Puppeteer `ProblemHandler` allows for centralized exception handling behaving similarly
  to try-catch blocks 
- An instance of `ProblemHandler` is returned in every `Result` object so Ticket holders
  can easily see all the exceptions that occured when running a Puppet 
    - The Puppet superclass wraps the Puppet `main()` method within its `ProblemHandler`
      to cleanly log unhandled exceptions and automatically complete the Puppet
- It offers convenience methods for executing code and having
  it handle exceptions automatically
    - `execute()`: Runs the given code, logs exceptions and rethrows them
    - `<T> attemptOptional()`: Returns an `Optional<T>` of the expected type
    - `attemptOrElse()`: Logs exceptions, returning a default value
    - `attempt()`: Logs exceptions and runs the provided handler code (fancy try catch)
- `Problem` is a wrapper class around a `Throwable` that contains more
  context than a simple message and cause that a normal exception can have
```java

import gg.vexi.Puppeteer.Exceptions.ProblemHandler;

{
    // pass in an optional boolean for verbose logging of errors to StdOut
    ProblemHandler ph = new ProblemHandler(true); 

    // logs and rethrows the exception
    ph.execute(() -> { throw new RuntimeException"rt_exception"); });

    // Returns an Optional<String>, logs exception if it occures
    Optional<String> result = ph.attemptOptional(() -> { 
        throw new RuntimeException("rt_exception"); 
    }); 
    String value = result.orElse("default"); // result is empty

    // logs the exception and returns the default value
    String result = ph.attemptOrElse(() -> { 
        throw new RuntimeException("rt_exception"); 
    }, "default");

    // logs the exception and runs the provided handler (basically try-catch)
    problemHandler.attempt(() -> {
        run();
    }, problem -> { // the Problem object of the logged exception
        System.out.println(problem.timestamp()); // Problem timestamp is an Instant object
    });
}

```


<!-- Features -->
<!-- ## Feature List
<!-- <span style="color: #00C2FF;">`TO BE WRITTEN`</span>


<!-- Limitations -->
## Limitations
- Puppeteer currently does not use a `ExecutorService` since I wnated to write my own 
  so it directly uses `CompletableFuture.runAsync()`
  - This means we cannot interrupt a Puppet thread since we hold no reference
    to it.
- Puppeteer has no lifetime tracking so if a puppet future never completes that 
  task will be marked active forever and we would never poll its queue
  - *eg:* if the puppet thread is interrupted
  - Currently the only way to resolve this is to create a new Puppeteer object and re-register all
    puppets

---

<!-- Planned features -->
## Planned features

<ul style="list-style-type: none; padding-left: 20px;">


<li style="margin-bottom: 10px;">
<details>
    <summary style="cursor: pointer; font-weight: bold;">Multiple of the same puppet type running concurrently/in parallel</summary>
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

<!-- Contributing -->
<!-- ## Contributing
<!-- <span style="color: #00C2FF;">`TO BE WRITTEN`</span>

---



<!-- Templates -->
### Puppet Template
```java
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;
import gg.vexi.Puppeteer.Core.ResultStatus;

public class TemplatePuppet extends Puppet<TEMPLATE> {

    private String data;

    public TemplatePuppet(Ticket<TEMPLATE> ticket) {
        super(ticket);
    }

    @Override
    public void main() {
        // - access ticket parameters with `parameter`
        // - use the builtin ProblemHandler instead of try-catch to centralize exception logging
        problemHandler.attempt(() -> {
            data = "Template";
        }, problem -> {
            // complete the puppet with an FAILED_ERROR status
            super.completeExceptionally();
        }
        super.complete(ResultStatus.SUCCESS, data);
    }
}
```
