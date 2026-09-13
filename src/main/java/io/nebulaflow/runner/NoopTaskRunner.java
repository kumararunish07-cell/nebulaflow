package io.nebulaflow.runner;

import io.nebulaflow.engine.DagPlanner;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NoopTaskRunner implements TaskRunner {
  @Override public String type() { return "NOOP"; }

  @Override public Object run(DagPlanner.Node node, Map<String, Object> context, TaskContext taskContext) {
    return Map.of("status", "ok");
  }
}
