package io.nebulaflow.runner;

import io.nebulaflow.engine.DagPlanner;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DelayTaskRunner implements TaskRunner {
  @Override public String type() { return "DELAY"; }

  @Override public Object run(DagPlanner.Node node, Map<String, Object> context, TaskContext taskContext) {
    long millis = ((Number) node.config().getOrDefault("millis", 0)).longValue();
    if (millis < 0 || millis > 30_000) throw new IllegalArgumentException("DELAY millis must be between 0 and 30000");
    try {
      if (millis > 0) Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("task interrupted", e);
    }
    return Map.of("delayedMs", millis);
  }
}
