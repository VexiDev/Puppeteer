# One at a time please
> This system is being designed to be used in a minecraft plugin for on demand encapsulated worlds

> Currently we use a ticket system to queue world management actions such as create and load. This is effective to ensure that we minimize concurrent world management which may cause world corruption. We are looking to improve this system to allow all "dangerous" plugin actions to be run in queues to allow for more control over the action processes and easy implementation of safety mesures

<div style="border-left: 4px solid #00eb3b; background-color: rgba(80,80,80, 0.4); padding: 10px;">
    Later implementation could allow for multiple workers running in parellel via abusing region threads (Folia) or running in concurrency on the same thread with robust world saftey measures
</div>

----
## Ticket Manager 

> This is the queue manager that will handle scheduling of all actions and allow for communication between the:
<br><strong>customer</strong>(code that scheduled the ticket) and the <strong>worker</strong>(code the ticket executes)

```mermaid
%% the class design
classDiagram
    direction LR
    
    class TicketManager {
        -Map : ActionType, Queue~Ticket~ actionQueues
        -Map : ActionType, ExecutorService executors
        +scheduleTicket(Ticket ticket)
        +processNextTicket(ActionType actionType)
        -executeTicket(Ticket ticket)
        +getQueueStatus(ActionType actionType)
        +requestDecision(Ticket ticket, String question, List~String~ options)
    }

    class Ticket {
        -UUID id
        -ActionType actionType
        -Map : String, Object parameters
        -TicketStatus status
        -CompletableFuture~Boolean~ completionFuture
        -Map : String, Object ticketData
        +getStatus()
        +setStatus(TicketStatus status)
        +getCompletionFuture()
        +updateStatusData(String key, Object value)
        +getStatusData(String key)
        +updateTicketData(String key, Object value)
        +getTicketData(String key)
        +getTicketDataMap()
    }
    
    
    TicketManager --> "0..*" Ticket : has
```
## Ticket Factory

```mermaid

classDiagram
    direction TB
    
    class TicketFactory {
        +createTicket(ActionType actionType, Map parameters) Ticket
    }

    class Ticket {
        -UUID id
        -ActionType actionType
        -Map : String, Object parameters
        -TicketStatus status
        -CompletableFuture~Boolean~ completionFuture
        -Map : String, Object ticketData
    }

    class TicketStatus {
        - StatusEnum status
        - JsonObject data
        + getStatus()
        + setStatus(StatusEnum status)
        + setStatusData(String key, Object value)
        + getStatusData(String key)
        + getStatusDataMap()
    }
    
    class StatusEnum {
        <<enumeration>>
        QUEUED
        PROCESSING
        COMPLETED
        FAILED
    }

    TicketFactory ..> Ticket : creates
    EachTicket --> "1" TicketStatus : has
    TicketStatus --> "1" StatusEnum : has

```

## TicketManager Logic

```mermaid
%% the behavior logic

graph TB
    A[Start] --> B{Customer: Schedule Ticket}
    B --> |Create Ticket| C[Add to appropriate queue]
    C --> D{Queue empty?}
    D --> |Yes| E[Process ticket immediately]
    D --> |No| F[Status = Queued\nWaits for processing]
    
    E --> G{{Do stuff and Update the ticket\ne.g. status, position in queue,\netc...}}
    G --> L{Processing successful?}
    L --> |Yes| M[Mark ticket as completed]
    L --> |No| N[Mark ticket as failed]
    
    M --> O[Notify customer of completion]
    N --> P[Notify customer of failure]

    Y[Log the Ticket]
    O --> Y
    P --> Y

    Z[Check for Next Ticket]
    Y --> Z
    Z --> ZA
    
    ZA{Queue empty?}
    ZB[Process next ticket]
    ZC[End]

    ZA -->|Yes| ZB
    ZA --> |No| ZC


```
----
## Worker Design

```mermaid

classDiagram
    direction LR

    class Key {
        -privateAttribute
        +publicAttribute
        #protectedAttribute
        -privateMethod()
        +publicMethod()
        #protectedMethod()
    }

    Key -- AbstractWorker

    class AbstractWorker {
        <<abstract>>
        -TicketManager ticketManager
        +processTicket(Ticket ticket)*
        #notifyCustomer(Ticket ticket, String message)
        #updateProgress(Ticket ticket, int progress)
        #updateStatus(Ticket ticket, StatusEnum status)
        #logAction(Ticket ticket, String action)
    }

    class WorldCreationWorker {
        +processTicket(Ticket ticket)
    }

    class WorldLoadWorker {
        +processTicket(Ticket ticket)
    }

    class TicketManager {
        +notifyCustomer(Ticket ticket, String message)
        +updateTicketProgress(Ticket ticket, int progress)
        +updateTicketStatus(Ticket ticket, StatusEnum status)
        +logTicketAction(Ticket ticket, String action)
    }

    AbstractWorker <|-- WorldCreationWorker
    AbstractWorker <|-- WorldLoadWorker
    AbstractWorker --> TicketManager

```
## Worker Factory
```mermaid

classDiagram
    direction LR

    class WorkerFactory {
        +createWorker(ActionType actionType) AbstractWorker
    }

    class AbstractWorker {
        <<abstract>>
        -TicketManager ticketManager
        +processTicket(Ticket ticket)*
    }

    class WorldCreationWorker {
        +processTicket(Ticket ticket)
    }

    class WorldLoadWorker {
        +processTicket(Ticket ticket)
    }

    WorkerFactory ..> AbstractWorker : creates
    AbstractWorker <|-- WorldCreationWorker
    AbstractWorker <|-- WorldLoadWorker

```
## Example Ticket Sequence
<h5>
    <div style="border-left: 4px solid #ffeb3b; background-color: rgba(255, 235, 59, 0.1); padding: 10px;">
        Note: A Customer is just the code that scheduled the ticket. They communicate to the worker through the TicketManager
    </div>
</h5>


>     Example worker increments an int
>  
>      1. Set ticket status to processing 
>      2. Inform customer of status change
>      3. increment the progress value
>      4. update ticket data with new value (incremented int)
>      5. request decision from the customer (eg: new stepAmount)
>      6. receive a decision (eg: +5 every time)
>      7. Sets the new step count
>      8. Complete Ticket
>   
---

```mermaid

sequenceDiagram
    participant Customer
    participant TicketManager
    participant Ticket
    participant Worker

    %% customer wants to schedule a ticket
    Customer->>TicketManager: scheduleTicket(actionType, parameters)

    %% we create the relevant ticket and add it to queue
    %% customer receives a CompletableFuture
    TicketManager->>TicketFactory: createTicket(actionType, parameters)
    TicketFactory-->>TicketManager: return Ticket
    TicketManager->>TicketManager: addToQueue(ticket)
    TicketManager-->>Customer: return CompletableFuture

    %% Queue processing stage (see TicketManager Logic for how we reach this stage)
    loop Queue Processing

        %% create the relevant worker class using worker factory
        TicketManager->>WorkerFactory: createWorker(actionType)
        WorkerFactory-->>TicketManager: return Worker

        %% tell worker to begin working
        TicketManager->>Worker: startTask(ticket)

        %% begin the task
        %% [Setup:1]
        Worker->>TicketManager: updateTicketStatus(ticket, PROCESSING)
        TicketManager->>Ticket: setStatus(PROCESSING)
        %% [Setup:2]
        TicketManager->>Customer: notify(notification,ticket)

        %% This is the worker's Task loop
        loop Task Execution

            %% [Task:1]
            Worker->>TicketManager: updateTicketProgress(ticket, value)
            %% [Task:2]
            TicketManager->>Ticket: updateStatusData("progress", value)

            %% [Task:3]
                
                %% request decision through ticketmanager
                Worker->>TicketManager: requestDecision(ticket, question, options)
                
                %% ticketmanager asks question to customer
                TicketManager->>Customer: requestDecision(ticket, question, options)
                
                %% customer returns an answer
                Customer-->>TicketManager: provideDecision(ticket, decision)
                
                %% ticketmanager passes answer to worker
                TicketManager-->>Worker: provideDecision(ticket, question, decision)
                %% worker processes the answer and continues

                TicketManager --> Worker: Task ends here we don't loop

        end %% that was the last step so we actually move on to the task completion stage

        %% finalize task completion:

        %% if successful
        alt Successful processing
            %% tells ticketmanager ticket is now complete
            Worker->>TicketManager: updateTicketStatus(ticket, COMPLETED)
            %% ticket manager updates the status
            TicketManager->>Ticket: setStatus(COMPLETED)

        %% if the task failed (error, failed test,etc)
        else Failed processing

            %% worker tells ticketmanager it failed
            Worker->>TicketManager: updateTicketStatus(ticket, FAILED)
            %% ticketmanager sets status to failed
            TicketManager->>Ticket: setStatus(FAILED)
            %% ticketmanager adds an error key to the ticket data with more info for the customer
            TicketManager->>Ticket: updateStatusData("error", errorMessage)
        end

        %% upon completion create a report of the ticket for the logs
        Worker->>TicketManager: logTicketAction(ticket)

        %% return the completable future to the customer
        TicketManager->>Customer: completeCompletableFuture(ticket)

    end
```
