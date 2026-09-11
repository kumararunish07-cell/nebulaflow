create table workflow_definitions (
  id uuid primary key, tenant_id varchar(120) not null, name varchar(200) not null,
  version integer not null, active boolean not null, definition_json text not null,
  created_at timestamp with time zone not null, unique (tenant_id, name, version)
);
create index idx_workflow_definitions_tenant_name on workflow_definitions(tenant_id, name, active);
create table workflow_runs (
  id uuid primary key, tenant_id varchar(120) not null, workflow_id uuid not null references workflow_definitions(id),
  status varchar(30) not null, input_json text, output_json text, idempotency_key varchar(200),
  started_at timestamp with time zone, finished_at timestamp with time zone, created_at timestamp with time zone not null,
  unique (tenant_id, workflow_id, idempotency_key)
);
create index idx_workflow_runs_tenant_created on workflow_runs(tenant_id, created_at desc);
