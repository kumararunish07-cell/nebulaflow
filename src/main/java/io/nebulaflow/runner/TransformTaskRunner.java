package io.nebulaflow.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nebulaflow.engine.DagPlanner;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TransformTaskRunner implements TaskRunner {
  private final ObjectMapper mapper;

  public TransformTaskRunner(ObjectMapper mapper) { this.mapper = mapper; }

  @Override public String type() { return "TRANSFORM"; }

  @Override public Object run(DagPlanner.Node node, Map<String, Object> context, TaskContext taskContext) {
    String expression = String.valueOf(node.config().getOrDefault("expression", "identity"));
    Object value = node.config().get("value");
    if (value == null && !node.dependsOn().isEmpty()) value = context.get(node.dependsOn().get(node.dependsOn().size() - 1));
    if ("uppercase".equalsIgnoreCase(expression) && value != null) return String.valueOf(value).toUpperCase(Locale.ROOT);
    if ("json".equalsIgnoreCase(expression)) try { return mapper.writeValueAsString(context); }
    catch (Exception e) { throw new IllegalStateException(e); }
    return value;
  }
}
