package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveCycles<V, E> {

  private static final Logger log = LoggerFactory.getLogger(RemoveCycles.class);
  protected Graph<V, E> graph;
  protected Set<V> stack = new HashSet<>();
  protected Set<V> marked = new HashSet<>();
  protected List<E> edges;
  protected final List<E> feedbackEdges = new ArrayList<>(); // cycles

  public RemoveCycles(Graph<V, E> graph) {
    this.graph = graph;
    this.edges = new LinkedList<>(graph.edgeSet());
  }

  public Graph<V, E> removeCycles() {
    //    log.info("vertices in order: {}", graph.vertexSet());
    graph.vertexSet().forEach(this::remove);
    return buildGraph(edges);
  }

  public List<E> getFeedbackEdges() {
    return feedbackEdges;
  }

  private Graph<V, E> buildGraph(List<E> edges) {
    Graph<V, E> newGraph = GraphTypeBuilder.forGraph(graph).buildGraph();
    for (E edge : edges) {
      V source = graph.getEdgeSource(edge);
      V target = graph.getEdgeTarget(edge);
      newGraph.addVertex(source);
      newGraph.addVertex(target);
      newGraph.addEdge(source, target, edge);
    }
    // if there are any vertices that were not added to newGraph, add them now.
    // these would be vertices with no incident edges
    graph
        .vertexSet()
        .stream()
        .filter(v -> !newGraph.containsVertex(v))
        .forEach(newGraph::addVertex);
    return newGraph;
  }

  private void remove(V v) {
    if (marked.add(v)) {
      //      log.info("added {}", v);
      stack.add(v);
      for (E edge : graph.outgoingEdgesOf(v)) {
        V target = graph.getEdgeTarget(edge);
        if (stack.contains(target)) {
          //          log.info("removing edge {} because {} has been seen", edge, target);
          edges.remove(edge);
          feedbackEdges.add(edge);
        } else if (!marked.contains(target)) {

          //          log.info("not removing edge {} because {} has not been seen", edge, target);
          remove(target);
        }
      }
      stack.remove(v);
    }
  }
}
