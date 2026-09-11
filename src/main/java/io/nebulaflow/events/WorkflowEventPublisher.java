package io.nebulaflow.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nebulaflow.domain.WorkflowRunEntity;
import io.nebulaflow.outbox.OutboxEventEntity;
import io.nebulaflow.outbox.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class WorkflowEventPublisher {
  private final OutboxEventRepository outbox;
  private final String topic;
  private final ObjectMapper mapper;

  public WorkflowEventPublisher(OutboxEventRepository outbox, ObjectMapper mapper,
      @Value("${nebulaflow.events.topic:nebulaflow.lifecycle}") String topic) {
    this.outbox = outbox;
    this.mapper = mapper;
    this.topic = topic;
  }

  public void publish(WorkflowRunEntity run) {
    try {
      Map<String, Object> event = Map.of(
          "runId", run.getId().toString(),
          "workflowId", run.getWorkflowId().toString(),
          "tenantId", run.getTenantId(),
          "status", run.getStatus().name(),
          "at", Instant.now().toString());
      outbox.save(new OutboxEventEntity(UUID.randomUUID(), topic, run.getId().toString(), mapper.writeValueAsString(event), Instant.now()));
    } catch (Exception error) {
      throw new IllegalStateException("could not write lifecycle event", error);
    }
  }
}
