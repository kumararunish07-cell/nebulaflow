# NebulaFlow

NebulaFlow is a portfolio-grade distributed workflow orchestration platform built with Java 21 and Spring Boot. It executes directed acyclic graphs (DAGs) of tasks with tenant isolation, durable run state, idempotency, retries, event publication, and operational telemetry.

> Status: foundation release (v0.1). The core API and execution engine are implemented; production hardening and additional connectors are tracked as roadmap work.

## Architecture

- **API** — versioned REST endpoints for workflow definitions and runs.
- **Orchestrator** — validates graphs, plans dependencies, and dispatches runnable nodes.
- **Execution engine** — virtual-thread workers and deterministic task handlers.
- **Durability** — PostgreSQL, JPA, and Flyway migrations.
- **Events** — Kafka lifecycle events for downstream integrations.
- **Operations** — Actuator health and metrics, Docker Compose, and GitHub Actions.

## Quick start

Requirements: JDK 21+, Docker, and Docker Compose.

```bash
docker compose up -d postgres kafka
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Every request must include `X-Tenant-Id`.

Create a workflow:

```bash
curl -X POST http://localhost:8080/api/v1/workflows \
  -H 'Content-Type: application/json' -H 'X-Tenant-Id: demo' \
  -d '{"name":"hello-pipeline","definition":{"steps":[{"id":"prepare","type":"NOOP"},{"id":"transform","type":"TRANSFORM","dependsOn":["prepare"],"config":{"expression":"uppercase","value":"nebula"}}]}}'
```

Trigger it with `POST /api/v1/workflows/{id}/runs`, then poll `GET /api/v1/runs/{runId}`.

## Engineering highlights

- DAG validation rejects duplicate nodes, missing dependencies, and cycles before persistence.
- Idempotency keys prevent duplicate runs for the same tenant and workflow.
- State transitions are explicit: `QUEUED -> RUNNING -> SUCCEEDED|FAILED`.
- Tenant context is mandatory and propagated through the request boundary.
- The test strategy separates pure graph/engine tests from database integration tests.

## Roadmap

1. Pluggable HTTP and container task runners with sandboxing.
2. Leader election and multi-node work claiming using PostgreSQL advisory locks.
3. Exactly-once outbox delivery and replayable event streams.
4. Web UI for visual DAG authoring and live run traces.
5. Kubernetes Helm chart and horizontal worker autoscaling.

## License

MIT

