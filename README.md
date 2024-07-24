# Asynchronous Ticket Management System
**ALL CODE IN THIS REPO IS NOT MEANT FOR ACTUAL USE!<br>ANYTHING YOU SEE HERE IS SUBJECT TO DRASTIC CHANGE** 
> This is still being brainstormed and worked through you can find design sheets [here](https://github.com/VexiDev/TicketSystemDemo/blob/main/SHEETS.md)
> 

## Overview

This project implements an asynchronous ticket management system designed for handling and processing various types of tasks or requests. It provides a robust framework for queuing, prioritizing, and executing tickets while managing customer interactions and decision requests.

## Key Components

1. **TicketManager**: Central component responsible for scheduling, queuing, and managing the execution of tickets as well as allowing communication between the customer and worker.
2. **Ticket**: Represents a single task or request, containing all relevant information and status.
3. **AbstractWorker**: Base class for implementing specific workers that process different types of tickets.
4. **CustomerCallback**: Interface for allowing communication from the ticketmanager to the customer

## Features

- Asynchronous ticket processing
- Queue management for different action types
- Priority-based ticket execution
- Easy communication between Customer and Workers
- Decision Requests allow workers to let customers pick from multiple options before proceeding

## Usage

1. Initialize the TicketManager:
   ```java
   TicketManager ticketManager = new TicketManager(threadPoolSize);
   ```

2. Create a CustomerCallback:
   ```java
   CustomerCallback callback = new CustomerCallback() {
       @Override
       public void onNotification(UUID ticketId, String message) {
           // Handle notification
       }

       @Override
       public <T> void onDecisionRequest(UUID ticketId, String question, List<T> options) {
           // Handle decision request
   
           // Send answer to ticket manager using
           ticketManager.provideDecision(ticketId, choice);
       }
   };
   ```

3. Schedule a ticket:
   ```java
   CompletableFuture<TicketManager.TicketResult> future = ticketManager.scheduleTicket(
       ActionType.SOME_ACTION,
       parameters,
       callback
   );
   ```

4. Handle the result:
   ```java
   future.thenAccept(result -> {
       if (result.success) {
           // Handle successful completion
       } else {
           // Handle failure
       }
   });
   ```

## Extending the System

To add new ticket types:
1. Create a new ActionType enum value.
2. Implement a new Worker class extending AbstractWorker.
3. Update the WorkerFactory to create instances of the new Worker for that ActionType.

## Performance Considerations

- The system uses a thread pool for processing tickets, allowing for concurrent execution of different action types.
- Queue positions are updated atomically to ensure consistency.
- Timeout functionality prevents system hangs due to unresponsive customers.

## Shutdown

To gracefully shut down the system:
```java
ticketManager.shutdown();
```

This ensures all ongoing processes are completed and resources are released properly.
