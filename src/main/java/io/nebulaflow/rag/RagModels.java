package io.nebulaflow.rag;
import java.util.List; import java.util.Map;
public final class RagModels { private RagModels() {}
  public record Document(String id,String text,Map<String,Object> metadata){ public Document { if(id==null||id.isBlank()) throw new IllegalArgumentException("documentId is required"); if(text==null||text.isBlank()) throw new IllegalArgumentException("text is required"); metadata=metadata==null?Map.of():Map.copyOf(metadata); } }
  public record IngestResult(String collection,String documentId,int chunks){}
  public record Match(String documentId,int chunkIndex,String content,double score,Map<String,Object> metadata){}
  public record QueryResult(String collection,String query,List<Match> matches,String context){}
}