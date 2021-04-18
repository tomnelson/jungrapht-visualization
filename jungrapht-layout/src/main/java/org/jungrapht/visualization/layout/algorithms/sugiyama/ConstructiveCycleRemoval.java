package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remove cycles in the graph and place them in a collection of feedback edges. A later operation
 * will reverse the direction of the feedback arcs for DAG processing. Once complete, the feedback
 * arcs will be restored to their original direction.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class ConstructiveCycleRemoval<V, E> {

  private static final Logger log = LoggerFactory.getLogger(ConstructiveCycleRemoval.class);

  Collection<E> feedbackArcs = new HashSet<>();
  Graph<V, E> graph;
  Graph<V, E> dag;
  Comparator<E> comparator;

  public ConstructiveCycleRemoval(Graph<V, E> graph) {
    this(graph, (v1, v2) -> 0);
  }

  public ConstructiveCycleRemoval(Graph<V, E> graph, Comparator<E> comparator) {
    this.graph = graph;
    this.comparator = comparator;
    this.dag = GraphTypeBuilder.<V, E>forGraphType(DefaultGraphType.dag()).buildGraph();
    GraphBuilder<V, E, ? extends Graph<V, E>> builder =
        (GraphBuilder<V, E, ? extends Graph<V, E>>) DirectedAcyclicGraph.createBuilder(Object::new);
    Graph<V, E> dag2 = builder.build();

    graph.vertexSet().forEach(dag::addVertex);
    graph.vertexSet().forEach(dag2::addVertex);
    List<E> edgeList = new ArrayList<>(graph.edgeSet());
    edgeList.sort(comparator);
    for (E edge : edgeList) {
      V source = graph.getEdgeSource(edge);
      V target = graph.getEdgeTarget(edge);
      try {
        dag.addEdge(source, target, edge);
        dag2.addEdge(source, target, edge);
      } catch (Exception ex) {
        feedbackArcs.add(edge);
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("graph is {}", graph);
      log.trace("dag is {}", dag);

      log.trace(
          "graph vertex sets are the same: {}", graph.vertexSet().containsAll(dag.vertexSet()));
      log.trace("graph edge sets are the same: {}", graph.edgeSet().containsAll(dag.edgeSet()));
    }
  }

  public Collection<E> getFeedbackArcs() {
    return this.feedbackArcs;
  }
}
