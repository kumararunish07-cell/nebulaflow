package io.nebulaflow.rag;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface RagChunkRepository extends JpaRepository<RagChunkEntity,UUID>{ List<RagChunkEntity> findByTenantIdAndCollectionName(String tenantId,String collectionName); long deleteByTenantIdAndCollectionNameAndDocumentId(String tenantId,String collectionName,String documentId); }