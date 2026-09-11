package io.nebulaflow.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="workflow_runs")
public class WorkflowRunEntity {
  @Id private UUID id;
  @Column(name="tenant_id", nullable=false, length=120) private String tenantId;
  @Column(name="workflow_id", nullable=false) private UUID workflowId;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private RunStatus status;
  @Column(name="input_json", columnDefinition="text") private String inputJson;
  @Column(name="output_json", columnDefinition="text") private String outputJson;
  @Column(name="idempotency_key", length=200) private String idempotencyKey;
  @Column(name="started_at") private Instant startedAt;
  @Column(name="finished_at") private Instant finishedAt;
  @Column(name="created_at", nullable=false) private Instant createdAt;
  protected WorkflowRunEntity() {}
  public WorkflowRunEntity(UUID id,String tenantId,UUID workflowId,RunStatus status,String inputJson,String key,Instant createdAt){this.id=id;this.tenantId=tenantId;this.workflowId=workflowId;this.status=status;this.inputJson=inputJson;this.idempotencyKey=key;this.createdAt=createdAt;}
  public UUID getId(){return id;} public String getTenantId(){return tenantId;} public UUID getWorkflowId(){return workflowId;} public RunStatus getStatus(){return status;} public String getInputJson(){return inputJson;} public String getOutputJson(){return outputJson;} public Instant getStartedAt(){return startedAt;} public Instant getFinishedAt(){return finishedAt;} public Instant getCreatedAt(){return createdAt;}
  public void markRunning(){status=RunStatus.RUNNING;startedAt=Instant.now();}
  public void markSucceeded(String output){status=RunStatus.SUCCEEDED;outputJson=output;finishedAt=Instant.now();}
  public void markFailed(String output){status=RunStatus.FAILED;outputJson=output;finishedAt=Instant.now();}
}
