package io.nebulaflow.cache;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.*;
@Component public class WorkflowCache {
  private static final Logger log=LoggerFactory.getLogger(WorkflowCache.class); private final StringRedisTemplate redis; private final ObjectMapper mapper; private final Duration ttl;
  public WorkflowCache(StringRedisTemplate redis,ObjectMapper mapper,@Value("${nebulaflow.cache.ttl:10m}")Duration ttl){this.redis=redis;this.mapper=mapper;this.ttl=ttl;}
  public void put(String tenant,UUID id,Map<String,Object> definition){try{redis.opsForValue().set(key(tenant,id),mapper.writeValueAsString(definition),ttl);}catch(Exception e){log.debug("Redis unavailable: {}",e.getMessage());}}
  public Optional<Map<String,Object>> get(String tenant,UUID id){try{String value=redis.opsForValue().get(key(tenant,id));return value==null?Optional.empty():Optional.of(mapper.readValue(value,new TypeReference<>(){}));}catch(Exception e){return Optional.empty();}}
  private String key(String tenant,UUID id){return "nebulaflow:workflow:"+tenant+":"+id;}
}
