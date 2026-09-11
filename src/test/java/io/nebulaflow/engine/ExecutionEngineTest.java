package io.nebulaflow.engine;
import com.fasterxml.jackson.databind.ObjectMapper; import org.junit.jupiter.api.Test; import java.util.*; import static org.junit.jupiter.api.Assertions.*;
class ExecutionEngineTest { @Test void transformsValues(){var e=new ExecutionEngine(new ObjectMapper());var d=Map.<String,Object>of("steps",List.of(Map.of("id","start","type","TRANSFORM","config",Map.of("value","hello","expression","uppercase"))));var out=e.execute(d,Map.of());assertEquals("HELLO",((Map<?,?>)out.get("steps")).get("start"));} }
