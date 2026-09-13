package io.nebulaflow.runner;

import io.nebulaflow.engine.DagPlanner;
import java.util.Map;

public interface TaskRunner {
  String type();

  Object run(DagPlanner.Node node, Map<String, Object> context, TaskContext taskContext) throws Exception;
}
