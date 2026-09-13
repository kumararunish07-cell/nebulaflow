package io.nebulaflow.rag;
import java.util.List;
public interface EmbeddingModel { List<Double> embed(String text); int dimensions(); }