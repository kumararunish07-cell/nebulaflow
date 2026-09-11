package io.nebulaflow.repository;
import io.nebulaflow.domain.WorkflowDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinitionEntity,UUID> {
  List<WorkflowDefinitionEntity> findByTenantIdAndActiveTrueOrderByNameAscVersionDesc(String tenantId);
  Optional<WorkflowDefinitionEntity> findByTenantIdAndId(String tenantId,UUID id);
  int countByTenantIdAndName(String tenantId,String name);
}
