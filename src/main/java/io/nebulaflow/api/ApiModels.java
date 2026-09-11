package io.nebulaflow.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class ApiModels {
  private ApiModels() {}
  public record CreateWorkflowRequest(@NotBlank String name, @NotNull Map<String,Object> definition) {}
  public record WorkflowResponse(UUID id, String tenantId, String name, int version, Map<String,Object> definition, Instant createdAt) {}
  public record StartRunRequest(Map<String,Object> input, String idempotencyKey) {}
  public record RunResponse(UUID id, UUID workflowId, String tenantId, String status, Map<String,Object> output, Instant createdAt, Instant startedAt, Instant finishedAt) {}
  public record WorkflowSummary(UUID id, String name, int version, boolean active) {}
  public record ErrorResponse(String code, String message, Instant timestamp) {}
}
