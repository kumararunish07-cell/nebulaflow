package io.nebulaflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nebulaflow.runner.TaskContext;
import io.nebulaflow.runner.TaskRunner;
import io.nebulaflow.runner.TaskRunnerRegistry;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ExecutionEngine {
  private final RetryPolicy retryPolicy;
  private final TaskRunnerRegistry runners;

  public ExecutionEngine(ObjectMapper mapper, RetryPolicy retryPolicy, TaskRunnerRegistry runners) {
    this.retryPolicy = retryPolicy;
    this.runners = runners;
  }

  public Map<String, Object> execute(Map<String, Object> definition, Map<String, Object> input) {
    return execute(definition, input, new TaskContext("system", null, input));
  }

  public Map<String, Object> execute(Map<String, Object> definition, Map<String, Object> input, TaskContext taskContext) {
    List<DagPlanner.Node> nodes = DagPlanner.plan(definition);
    Map<String, Object> context = new LinkedHashMap<>(input == null ? Map.of() : input);
    for (DagPlanner.Node node : nodes) context.put(node.id(), executeNode(node, context, taskContext));
    return Map.of("steps", context, "completed", nodes.stream().map(DagPlanner.Node::id).toList());
  }

  private Object executeNode(DagPlanner.Node node, Map<String, Object> context, TaskContext taskContext) {
    long attemptsValue = number(node.config().getOrDefault("maxAttempts", 1), "maxAttempts");
    if (attemptsValue > Integer.MAX_VALUE) throw new WorkflowValidationException("maxAttempts is too large for " + node.id());
    int attempts = (int) attemptsValue;
    long backoff = number(node.config().getOrDefault("backoffMs", 100), "backoffMs");
    if (attempts < 1) throw new WorkflowValidationException("maxAttempts must be positive for " + node.id());
    if (backoff < 0) throw new WorkflowValidationException("backoffMs cannot be negative for " + node.id());
    TaskRunner runner = runners.require(node);
    return retryPolicy.execute(() -> runner.run(node, context, taskContext), attempts, Duration.ofMillis(backoff));
  }

  private static long number(Object value, String name) {
    if (!(value instanceof Number number)) throw new WorkflowValidationException(name + " must be numeric");
    return number.longValue();
  }
}
