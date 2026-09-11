package io.nebulaflow.repository;
import io.nebulaflow.domain.WorkflowRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WorkflowRunRepository extends JpaRepository<WorkflowRunEntity,UUID> {
  Optional<WorkflowRunEntity> findByTenantIdAndId(String tenantId,UUID id);
  Optional<WorkflowRunEntity> findByTenantIdAndWorkflowIdAndIdempotencyKey(String tenantId,UUID id,String key);
}
