package io.nebulaflow.runner;

import java.util.Map;
import java.util.UUID;

public record TaskContext(String tenantId, UUID runId, Map<String, Object> input) {
  public TaskContext {
    input = input == null ? Map.of() : Map.copyOf(input);
  }
}
