# Architecture notes

## Run lifecycle

1. Validate the tenant, workflow, definition graph, and optional idempotency key.
2. Insert a durable `WorkflowRun` as `QUEUED`.
3. Dispatch the run to a virtual-thread worker.
4. Plan the DAG and execute nodes in dependency order.
5. Persist the terminal state and publish a Kafka lifecycle event.

## Consistency model

PostgreSQL is the source of truth for definitions and run state. Kafka is an integration stream, not the transaction coordinator. A production outbox is the next hardening step.

## Failure model

A task failure stops dependent nodes. Durable run state lets clients safely retry polling and command requests. Tenant context is checked at the HTTP boundary and every repository lookup includes tenant ownership.
