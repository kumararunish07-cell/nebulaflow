package io.nebulaflow.runner;

import io.nebulaflow.engine.DagPlanner;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskRunnerRegistryTest {
  @Test void resolvesRunnerByCaseInsensitiveType() {
    var registry = new TaskRunnerRegistry(List.of(new TaskRunner() {
      @Override public String type() { return "HTTP"; }
      @Override public Object run(DagPlanner.Node node, Map<String, Object> context, TaskContext taskContext) { return Map.of(); }
    }));
    var node = new DagPlanner.Node("call", "http", List.of(), Map.of());
    assertEquals("HTTP", registry.require(node).type());
  }
}
