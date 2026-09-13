package io.nebulaflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nebulaflow.runner.TaskRunnerRegistry;
import io.nebulaflow.runner.TransformTaskRunner;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExecutionEngineTest {
  @Test void transformsValuesThroughARegisteredPlugin() {
    var mapper = new ObjectMapper();
    var registry = new TaskRunnerRegistry(List.of(new TransformTaskRunner(mapper)));
    var engine = new ExecutionEngine(mapper, new RetryPolicy(), registry);
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
