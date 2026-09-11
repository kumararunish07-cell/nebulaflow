package io.nebulaflow.outbox;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity,UUID>{List<OutboxEventEntity> findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();}
