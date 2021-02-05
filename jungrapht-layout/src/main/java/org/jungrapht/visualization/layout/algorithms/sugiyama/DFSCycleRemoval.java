package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.builder.GraphTypeBuilder;

public class DFSCycleRemoval<V, E> { //implements Function<Graph<V, E>, Collection<E>> {

  private Graph<V, E> graph;
  Collection<E> edges;
  Set<V> marked = new HashSet<>();
  Stack<V> stack = new Stack<>();
  Set<E> feedbackArcs = new HashSet<>();
  Predicate<E> edgePredicate;
  Comparator<V> comparator;

  public DFSCycleRemoval(Graph<V, E> graph) {
    this(graph, (v1, v2) -> 0);
  }

  public DFSCycleRemoval(Graph<V, E> graph, Comparator<V> comparator) {
    // make a copy of the graph
    this.graph = GraphTypeBuilder.forGraph(graph).buildGraph();
    Graphs.addGraph(this.graph, graph); // dest, source
    this.edges = graph.edgeSet();
    this.comparator = comparator;
    removeCycles();
  }

  public void removeCycles() {
    List<V> vertices = graph.vertexSet().stream().sorted(comparator).collect(Collectors.toList());
    //    Collections.reverse(vertices);
    for (V vertex : vertices) {
      this.dfsRemove(vertex);
    }
  }

  public void setEdgePredicate(Predicate<E> edgePredicate) {
    this.edgePredicate = edgePredicate;
  }

  private void dfsRemove(V vertex) {
    if (this.marked.contains(vertex)) {
      return;
    }
    this.marked.add(vertex);
    this.stack.add(vertex);
    List<E> outgoingEdges =
        graph
            .outgoingEdgesOf(vertex)
            .stream()
            //            .sorted(comparator)
            .collect(Collectors.toList());
    //    Collections.reverse(outgoingEdges);
    for (E edge : outgoingEdges) {
      V target = graph.getEdgeTarget(edge);
      if (this.stack.contains(target)) {
        graph.removeEdge(edge);
        feedbackArcs.add(edge);
      } else if (!this.marked.contains(target)) {
        this.dfsRemove(target);
      }
    }
    this.stack.remove(vertex);
  }

  public Collection<E> getFeedbackArcs() {
    return this.feedbackArcs;
  }
}
