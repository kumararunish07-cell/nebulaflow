package io.nebulaflow.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="workflow_definitions")
public class WorkflowDefinitionEntity {
  @Id private UUID id;
  @Column(name="tenant_id", nullable=false, length=120) private String tenantId;
  @Column(nullable=false, length=200) private String name;
  @Column(nullable=false) private int version;
  @Column(nullable=false) private boolean active;
  @Column(name="definition_json", nullable=false, columnDefinition="text") private String definitionJson;
  @Column(name="created_at", nullable=false) private Instant createdAt;
  protected WorkflowDefinitionEntity() {}
  public WorkflowDefinitionEntity(UUID id,String tenantId,String name,int version,boolean active,String definitionJson,Instant createdAt){this.id=id;this.tenantId=tenantId;this.name=name;this.version=version;this.active=active;this.definitionJson=definitionJson;this.createdAt=createdAt;}
  public UUID getId(){return id;} public String getTenantId(){return tenantId;} public String getName(){return name;} public int getVersion(){return version;} public boolean isActive(){return active;} public String getDefinitionJson(){return definitionJson;} public Instant getCreatedAt(){return createdAt;}
}
