# NebulaFlow

NebulaFlow is a portfolio-grade distributed workflow orchestration platform built with Java 21 and Spring Boot. It executes directed acyclic graphs (DAGs) of tasks with tenant isolation, API-key authentication, role-based access control, durable run state, idempotency, retries, pluggable task runners, lifecycle events, and operational telemetry.

> Status: v0.3 foundation release. Core orchestration, Redis caching, retry policy, durable Kafka outbox, API-key RBAC, and allowlisted HTTP task execution are implemented. Production identity providers, secret vault integration, and container sandboxing remain hardening work.

## Architecture

- **API** - versioned REST endpoints for workflow definitions and runs, with OpenAPI documentation.
- **Security boundary** - stateless `X-API-Key` authentication, tenant authorization, and role-based endpoint policies.
- **Orchestrator** - validates graphs, plans dependencies, and dispatches runnable nodes.
- **Plugin engine** - a `TaskRunner` registry discovers task types as Spring components; built-ins include `NOOP`, `DELAY`, `TRANSFORM`, and `HTTP`.
- **HTTP runner** - outbound HTTP is restricted to an explicit host allowlist, bounded by connection/request timeouts, disables redirects, blocks managed headers, and limits response size.
- **Durability** - PostgreSQL, JPA, Flyway migrations, and a durable Kafka outbox.
- **Operations** - Actuator health and metrics, Redis caching, and Docker Compose.

## Quick start

Requirements: JDK 21+, Maven, Docker, and Docker Compose.

```bash
docker compose up -d postgres kafka redis
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Protected requests require both `X-Tenant-Id` and `X-API-Key`.

Development API keys from `application.yml`:

| Key | Principal | Roles | Tenant |
|---|---|---|---|
| `demo-admin` | `admin` | ADMIN, OPERATOR | demo |
| `demo-operator` | `operator` | OPERATOR | demo |
| `demo-viewer` | `viewer` | VIEWER | demo |

These keys are development defaults only. Replace them through `NEBULAFLOW_API_KEYS` before deploying anywhere real. The format is:

```text
apiKey=principal|ROLE_NAME|ROLE_NAME|TENANTS:tenant-a,tenant-b;otherKey=...
```

OpenAPI documentation is available without authentication at `http://localhost:8080/swagger-ui.html`.

Create a workflow:

```bash
curl -X POST http://localhost:8080/api/v1/workflows \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Id: demo' \
  -H 'X-API-Key: demo-admin' \
  -d '{"name":"hello-pipeline","definition":{"steps":[{"id":"prepare","type":"NOOP"},{"id":"transform","type":"TRANSFORM","dependsOn":["prepare"],"config":{"expression":"uppercase","value":"nebula"}}]}}'
```

Trigger it with `POST /api/v1/workflows/{id}/runs` using `demo-admin` or `demo-operator`, then poll `GET /api/v1/runs/{runId}` using any key authorized for the `demo` tenant.

## HTTP task runner

The `HTTP` plugin accepts a URL, method, headers, and optional body. The host must be explicitly allowlisted through `NEBULAFLOW_HTTP_ALLOWED_HOSTS`.

```json
{
  "id": "call-healthcheck",
  "type": "HTTP",
  "config": {
    "method": "GET",
    "url": "http://localhost:8080/actuator/health",
    "headers": {"Accept": "application/json"},
    "maxAttempts": 2,
    "backoffMs": 250
  }
}
```

Configuration:

```bash
NEBULAFLOW_HTTP_ALLOWED_HOSTS=localhost,127.0.0.1
NEBULAFLOW_HTTP_TIMEOUT=10s
NEBULAFLOW_HTTP_MAX_RESPONSE_BYTES=1048576
```

Do not place credentials in workflow definitions. Use a secret-aware runner or vault integration before connecting to third-party services.

## Engineering highlights

- DAG validation rejects duplicate nodes, missing dependencies, and cycles before persistence.
- Idempotency keys prevent duplicate runs for the same tenant and workflow.
- State transitions are explicit: `QUEUED -> RUNNING -> SUCCEEDED|FAILED`.
- Each task can set `maxAttempts` and `backoffMs` for bounded retry behavior.
- New task types are added as `TaskRunner` Spring components rather than by modifying the execution switch.
- HTTP execution has explicit SSRF defenses: scheme validation, host allowlisting, no redirects, timeout bounds, managed-header protection, and response-size limits.
- Lifecycle events are persisted before asynchronous Kafka delivery through an outbox table.
- Workflow definitions are cached by tenant and ID in Redis, with graceful cache degradation.
- Tenant context is mandatory and API keys must be authorized for that tenant.
- The test strategy separates pure graph/engine/plugin tests from database integration tests.

## Security notes

This release uses configuration-backed API keys to keep the sample self-contained. For production, replace the catalog with an OIDC/JWT resource server or a secrets manager, rotate keys, avoid committing defaults, add audit events for authorization decisions, and run HTTP/container plugins in a dedicated sandbox.

## Roadmap

1. OIDC/JWT integration and secret-vault-backed runner credentials.
2. Container task runner with resource limits and isolation.
3. Leader election and multi-node work claiming using PostgreSQL advisory locks.
4. Exactly-once event delivery with consumer deduplication and replay tooling.
5. Web UI for visual DAG authoring and live run traces.
6. Kubernetes Helm chart and horizontal worker autoscaling.

## License

MIT
