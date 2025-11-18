# Migration from Choreography to Orchestration-Based Sagas

This document describes the migration from choreography-based to orchestration-based saga pattern.

## Overview

The application has been migrated from **choreography-based sagas** (event-driven, decentralized) to **orchestration-based sagas** (command-driven, centralized) as described in Chapter 4 of "Microservices Patterns".

## Key Changes

### 1. Saga Step Interface

**Before (Choreography):**
- Steps were driven by events
- Each service listened to events and reacted independently
- No central coordination

**After (Orchestration):**
- Steps implement `AsyncSagaStep` interface
- Steps execute via REST commands from orchestrator
- Steps report back to orchestrator via callbacks

### 2. Communication Pattern

**Before (Choreography):**
```
Order Service → Publishes OrderApprovedEvent
Kitchen Service → Listens to OrderApprovedEvent → Creates Ticket
Accounting Service → Listens to OrderApprovedEvent → Authorizes Payment
```

**After (Orchestration):**
```
SagaManager (Orchestrator) → Commands Kitchen Service → Waits for Response
SagaManager (Orchestrator) → Commands Accounting Service → Waits for Response
SagaManager (Orchestrator) → Coordinates all steps sequentially
```

### 3. Saga Steps

All saga steps now:
- Execute asynchronously via REST API calls
- Report results back to orchestrator via callbacks
- Support compensation that is also orchestrated

**Updated Steps:**
- `ValidateOrderStep` - Validates order (orchestrated)
- `CreateTicketStep` - Creates ticket via REST command (orchestrated)
- `AuthorizeCardStep` - Authorizes payment via REST command (orchestrated)
- `ConfirmCreateOrderStep` - Confirms order (orchestrated)

### 4. Event Consumers

Event consumers in Kitchen and Accounting services are now **legacy** and kept for backward compatibility. The primary communication is via orchestrated REST commands.

## Benefits of Orchestration

1. **Centralized Control**: SagaManager has full visibility and control
2. **Easier Debugging**: All saga state is in one place
3. **Better Error Handling**: Orchestrator can handle failures centrally
4. **Simpler Compensation**: Compensation logic is centralized
5. **Clear Flow**: Saga flow is explicit and easy to follow

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              SagaManager (Orchestrator)                 │
│  - Creates saga instance                                │
│  - Executes steps sequentially                          │
│  - Handles failures and compensation                    │
└─────────────────────────────────────────────────────────┘
           │                    │                    │
           ▼                    ▼                    ▼
    ┌──────────┐         ┌──────────┐         ┌──────────┐
    │ Validate │         │  Create  │         │Authorize │
    │  Order   │         │  Ticket  │         │   Card   │
    └──────────┘         └──────────┘         └──────────┘
           │                    │                    │
           │ REST Command        │ REST Command       │ REST Command
           ▼                    ▼                    ▼
    ┌──────────┐         ┌──────────┐         ┌──────────┐
    │  Order   │         │ Kitchen  │         │Accounting│
    │ Service  │         │ Service  │         │ Service  │
    └──────────┘         └──────────┘         └──────────┘
```

## Implementation Details

### SagaManager

The `SagaManager` is the central orchestrator that:
- Creates saga instances
- Executes steps sequentially
- Waits for step completion via callbacks
- Handles failures and triggers compensation
- Manages saga state

### AsyncSagaStep

All steps implement `AsyncSagaStep` interface:
```java
void executeAsync(Object sagaData, Consumer<SagaStepResult> callback);
void compensateAsync(Object sagaData, Consumer<SagaStepResult> callback);
```

Steps execute asynchronously and report back via the callback.

### Step Execution Flow

1. Orchestrator calls `step.executeAsync(sagaData, callback)`
2. Step makes REST call to target service
3. Step reports result via `callback.accept(result)`
4. Orchestrator processes result and executes next step
5. If failure, orchestrator triggers compensation

## Migration Checklist

✅ Converted all saga steps to `AsyncSagaStep`
✅ Updated `SagaManager` to use async step execution
✅ Updated `SagaDefinition` to use `AsyncSagaStep`
✅ Added callback mechanism for step results
✅ Updated event consumers to note orchestration preference
✅ Added saga instance ID tracking
✅ Updated compensation to use async pattern

## Backward Compatibility

Event consumers are still present but marked as legacy. They can be removed once all clients are migrated to orchestration-based communication.

## Testing

To test the orchestration-based saga:

1. Create an order via Order Service
2. Order Service creates a saga instance
3. SagaManager orchestrates the steps:
   - Validates order
   - Creates ticket (REST call to Kitchen Service)
   - Authorizes card (REST call to Accounting Service)
   - Confirms order
4. All steps report back to orchestrator
5. Orchestrator tracks saga state

## Future Enhancements

- [ ] Remove legacy event consumers
- [ ] Add saga visualization dashboard
- [ ] Implement saga step retry with exponential backoff
- [ ] Add saga performance metrics
- [ ] Implement parallel step execution where possible

