package io.nebulaflow.engine;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.concurrent.Callable;
@Component public class RetryPolicy {
  public <T>T execute(Callable<T> operation,int maxAttempts,Duration initialBackoff){RuntimeException last=null;int limit=Math.max(1,maxAttempts);for(int attempt=1;attempt<=limit;attempt++){try{return operation.call();}catch(Exception e){last=e instanceof RuntimeException r?r:new IllegalStateException(e);if(attempt==limit)break;try{Thread.sleep(Math.min(initialBackoff.toMillis()*(1L<<Math.min(attempt-1,8)),30000L));}catch(InterruptedException x){Thread.currentThread().interrupt();throw new IllegalStateException("retry interrupted",x);}}}throw last==null?new IllegalStateException("operation failed"):last;}
}
