package io.nebulaflow.engine;
import java.util.*;
public final class DagPlanner {
  public record Node(String id,String type,List<String> dependsOn,Map<String,Object> config) {}
  private DagPlanner(){}
  public static List<Node> plan(Map<String,Object> definition){
    Object raw=definition.get("steps"); if(!(raw instanceof List<?> list)||list.isEmpty()) throw new WorkflowValidationException("definition.steps must be a non-empty array");
    Map<String,Node> nodes=new LinkedHashMap<>();
    for(Object item:list){if(!(item instanceof Map<?,?> map))throw new WorkflowValidationException("each step must be an object"); String id=String.valueOf(map.get("id")); if(id.isBlank()||nodes.containsKey(id))throw new WorkflowValidationException("duplicate or blank step id: "+id); Object typeValue=map.get("type"); String type=String.valueOf(typeValue==null?"NOOP":typeValue); List<String> deps=dependencies(map.get("dependsOn")); Map<String,Object> config=new LinkedHashMap<>(); Object c=map.get("config"); if(c instanceof Map<?,?> cm)cm.forEach((k,v)->config.put(String.valueOf(k),v)); nodes.put(id,new Node(id,type,deps,config));}
    for(Node node:nodes.values())for(String dep:node.dependsOn())if(!nodes.containsKey(dep))throw new WorkflowValidationException("missing dependency "+dep+" for "+node.id());
    List<Node> ordered=new ArrayList<>(); Set<String> done=new HashSet<>(); while(ordered.size()<nodes.size()){boolean progressed=false; for(Node node:nodes.values())if(!done.contains(node.id())&&done.containsAll(node.dependsOn())){ordered.add(node);done.add(node.id());progressed=true;} if(!progressed)throw new WorkflowValidationException("workflow contains a dependency cycle");} return ordered;
  }
  private static List<String> dependencies(Object raw){if(raw==null)return List.of(); if(!(raw instanceof List<?> list))throw new WorkflowValidationException("dependsOn must be an array"); return list.stream().map(String::valueOf).toList();}
}

