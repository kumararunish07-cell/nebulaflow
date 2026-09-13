package io.nebulaflow.runner;

import io.nebulaflow.engine.DagPlanner;
import io.nebulaflow.engine.WorkflowValidationException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TaskRunnerRegistry {
  private final Map<String, TaskRunner> runners;

  public TaskRunnerRegistry(List<TaskRunner> discoveredRunners) {
    runners = discoveredRunners.stream().collect(Collectors.toUnmodifiableMap(
        runner -> runner.type().toUpperCase(Locale.ROOT), Function.identity(),
        (left, right) -> { throw new IllegalStateException("duplicate task runner: " + left.type()); }));
  }

  public TaskRunner require(DagPlanner.Node node) {
    TaskRunner runner = runners.get(node.type().toUpperCase(Locale.ROOT));
    if (runner == null) throw new WorkflowValidationException("unsupported task type: " + node.type());
    return runner;
  }
}
