package io.nebulaflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
public class ExecutionEngine {
  private final ObjectMapper mapper;
  private final RetryPolicy retryPolicy;

  public ExecutionEngine(ObjectMapper mapper, RetryPolicy retryPolicy) {
    this.mapper = mapper;
    this.retryPolicy = retryPolicy;
  }

  public Map<String, Object> execute(Map<String, Object> definition, Map<String, Object> input) {
    List<DagPlanner.Node> nodes = DagPlanner.plan(definition);
    Map<String, Object> context = new LinkedHashMap<>(input == null ? Map.of() : input);
    for (DagPlanner.Node node : nodes) context.put(node.id(), executeNode(node, context));
    return Map.of("steps", context, "completed", nodes.stream().map(DagPlanner.Node::id).toList());
  }

  private Object executeNode(DagPlanner.Node node, Map<String, Object> context) {
    int attempts = ((Number) node.config().getOrDefault("maxAttempts", 1)).intValue();
    long backoff = ((Number) node.config().getOrDefault("backoffMs", 100)).longValue();
    return retryPolicy.execute(() -> executeNodeOnce(node, context), attempts, Duration.ofMillis(backoff));
  }

  private Object executeNodeOnce(DagPlanner.Node node, Map<String, Object> context) {
    return switch (node.type().toUpperCase(Locale.ROOT)) {
      case "NOOP" -> Map.of("status", "ok");
      case "DELAY" -> {
        long millis = ((Number) node.config().getOrDefault("millis", 0)).longValue();
        if (millis > 0) try { Thread.sleep(Math.min(millis, 30_000)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("task interrupted", e); }
        yield Map.of("delayedMs", millis);
      }
      case "TRANSFORM" -> transform(node, context);
      default -> throw new WorkflowValidationException("unsupported task type: " + node.type());
    };
  }

  private Object transform(DagPlanner.Node node, Map<String, Object> context) {
    String expression = String.valueOf(node.config().getOrDefault("expression", "identity"));
    Object value = node.config().get("value");
    if (value == null && !node.dependsOn().isEmpty()) value = context.get(node.dependsOn().get(node.dependsOn().size() - 1));
    if ("uppercase".equalsIgnoreCase(expression) && value != null) return String.valueOf(value).toUpperCase(Locale.ROOT);
    if ("json".equalsIgnoreCase(expression)) try { return mapper.writeValueAsString(context); }
    catch (Exception e) { throw new IllegalStateException(e); }
    return value;
  }
}
