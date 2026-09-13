# NebulaFlow

NebulaFlow is a portfolio-grade distributed workflow orchestration platform built with Java 21 and Spring Boot. It executes directed acyclic graphs (DAGs) of tasks with tenant isolation, OIDC/JWT or API-key authentication, role-based access control, durable run state, idempotency, retries, pluggable task runners, lifecycle events, and operational telemetry.

> Status: v0.4 foundation release. Core orchestration, Redis caching, retry policy, durable Kafka outbox, pluggable task runners, API-key RBAC, OIDC/JWT resource-server validation, JWT tenant claims, and externalized secret configuration are implemented. Container sandboxing remains hardening work.

## Architecture

- **API** - versioned REST endpoints for workflow definitions and runs, with OpenAPI documentation.
- **Security boundary** - stateless API-key, OIDC/JWT, or hybrid authentication; tenant authorization; and role-based endpoint policies. JWT validation supports issuer discovery or a direct JWK set URI, audience validation, configurable role claims, and configurable tenant claims.
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

The API starts on `http://localhost:8080`. Every protected request requires `X-Tenant-Id`. In the default `API_KEY` mode it also requires `X-API-Key`; in `OIDC` mode it requires `Authorization: Bearer <JWT>`; `HYBRID` accepts either.

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

## OIDC/JWT production mode

Set `NEBULAFLOW_SECURITY_MODE=OIDC` and configure either an issuer or a JWK set URI. Issuer mode uses OIDC discovery and validates issuer plus timestamps; JWK-set mode validates timestamps and can use the configured audience validator. JWT roles are read from `roles` by default and mapped to Spring authorities such as `ROLE_ADMIN`; scopes from `scope` or `scp` become `SCOPE_*` authorities. The tenant claim defaults to `tenants` and may be a string or array. A JWT must contain the requested `X-Tenant-Id`, or `*`, in that claim.

```bash
NEBULAFLOW_SECURITY_MODE=OIDC
NEBULAFLOW_OIDC_ISSUER_URI=https://idp.example.com/realms/nebulaflow
NEBULAFLOW_OIDC_AUDIENCE=nebulaflow-api
NEBULAFLOW_OIDC_ROLES_CLAIM=roles
NEBULAFLOW_OIDC_TENANT_CLAIM=tenants
```

Use `NEBULAFLOW_SECURITY_MODE=HYBRID` during a controlled migration when both Bearer JWTs and API keys must be accepted. Do not use hybrid mode as a permanent substitute for removing legacy credentials.

## Production secret management

All database, security, OIDC, broker, and runtime settings can be supplied through environment variables. Spring also imports these optional mounted files, in order: `./config/application-secrets.yml` and `/run/secrets/nebulaflow.yml`. The local secret file and `/run/secrets/` are ignored by Git; `config/application-secrets.example.yml` is a safe template. In Kubernetes, mount the Secret as `/run/secrets/nebulaflow.yml`; in Docker or a VM, inject the same values through the environment or a mounted file. Never commit real credentials, API keys, client secrets, or private keys.

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
- Tenant context is mandatory and both API keys and JWTs must be authorized for that tenant.
- The test strategy separates pure graph/engine/plugin tests from database integration tests.

## Security notes

API keys remain available for local development and controlled migrations. Production deployments should use OIDC/JWT mode, short-lived tokens, issuer and audience validation, external secret injection, key rotation at the identity provider, and audit events for authorization decisions. Workflow definitions still must not contain credentials. HTTP and future container plugins should run with dedicated network and resource isolation.

## Roadmap

1. Container task runner with resource limits and isolation.
2. Leader election and multi-node work claiming using PostgreSQL advisory locks.
3. Exactly-once event delivery with consumer deduplication and replay tooling.
4. Web UI for visual DAG authoring and live run traces.
5. Kubernetes Helm chart and horizontal worker autoscaling.

## License

MIT
