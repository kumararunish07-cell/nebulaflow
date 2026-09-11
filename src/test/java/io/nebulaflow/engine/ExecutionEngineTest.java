package io.nebulaflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionEngineTest {
  @Test void transformsValues() {
    var engine = new ExecutionEngine(new ObjectMapper(), new RetryPolicy());
    var definition = Map.<String, Object>of("steps", List.of(Map.of(
        "id", "start", "type", "TRANSFORM",
        "config", Map.of("value", "hello", "expression", "uppercase"))));
    var output = engine.execute(definition, Map.of());
    assertEquals("HELLO", ((Map<?, ?>) output.get("steps")).get("start"));
  }

  @Test void retriesAccordingToTaskConfiguration() {
    var policy = new RetryPolicy();
    assertEquals("done", policy.execute(() -> "done", 2, java.time.Duration.ZERO));
  }
}
