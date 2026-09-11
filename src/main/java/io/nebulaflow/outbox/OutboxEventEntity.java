package io.nebulaflow.outbox;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="outbox_events",indexes=@Index(name="idx_outbox_unpublished",columnList="published_at,created_at")) public class OutboxEventEntity {
 @Id private UUID id; @Column(nullable=false,length=200) private String topic; @Column(name="event_key",nullable=false,length=200) private String eventKey; @Column(nullable=false,columnDefinition="text") private String payload; @Column(name="created_at",nullable=false) private Instant createdAt; @Column(name="published_at") private Instant publishedAt;
 protected OutboxEventEntity(){} public OutboxEventEntity(UUID id,String topic,String eventKey,String payload,Instant createdAt){this.id=id;this.topic=topic;this.eventKey=eventKey;this.payload=payload;this.createdAt=createdAt;} public UUID getId(){return id;} public String getTopic(){return topic;} public String getEventKey(){return eventKey;} public String getPayload(){return payload;} public void markPublished(){publishedAt=Instant.now();}
}
