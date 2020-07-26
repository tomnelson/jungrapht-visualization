package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
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
public class GreedyCycleRemoval<V, E> {

  private static final Logger log = LoggerFactory.getLogger(GreedyCycleRemoval.class);

  final Collection<E> feedbackArcs = new HashSet<>();
  final Graph<V, E> graph;
  final Graph<V, E> copy;

  public GreedyCycleRemoval(Graph<V, E> graph) {
    this.graph = graph;
    copy = GraphTypeBuilder.forGraph(graph).buildGraph();
    Graphs.addGraph(copy, graph); // dest, source
    getFeedbackEdges();
  }

  /** @return */
  public Collection<E> getFeedbackArcs() {
    return this.feedbackArcs;
  }

  /**
   * Get a sink vertex (a vertex with no outgoing edges)
   *
   * @param graph the graph to examine
   * @return the first sink vertex, if present
   */
  private Optional<V> getSink(Graph<V, E> graph) {
    return graph.vertexSet().stream().filter(v -> graph.outgoingEdgesOf(v).isEmpty()).findFirst();
  }

  /**
   * Get a source vertex (a vertex with no incoming edges)
   *
   * @param graph the graph to examine
   * @return the first source vertex, if present
   */
  private Optional<V> getSource(Graph<V, E> graph) {
    return graph.vertexSet().stream().filter(v -> graph.incomingEdgesOf(v).isEmpty()).findFirst();
  }

  /**
   * get all vertices that are incident to no edges (degree 0) or have only loop edges
   *
   * @param graph the incoming graph to examine
   * @return a List of vertices of degree 0
   */
  private Collection<V> getIsolatedVerties(Graph<V, E> graph) {
    return graph
        .vertexSet()
        .stream()
        .filter(v -> graph.degreeOf(v) == 0 || isLoopVertex(graph, v))
        .collect(Collectors.toList());
  }

  public static <V, E> boolean isLoopVertex(Graph<V, E> graph, V v) {
    return graph.outgoingEdgesOf(v).equals(graph.incomingEdgesOf(v));
  }

  public static <V, E> boolean isIsolatedVertex(Graph<V, E> graph, V v) {
    return graph.degreeOf(v) == 0;
  }

  /** */
  private void getFeedbackEdges() {

    Optional<V> opt;
    while ((opt = getSink(copy)).isPresent()) {
      V sink = opt.get();
      Collection<E> losers =
          copy.edgeSet()
              .stream()
              .filter(e -> copy.getEdgeTarget(e).equals(sink))
              .collect(Collectors.toList());

      log.trace("removing sink {} and incoming edges {}", sink, losers);
      copy.removeAllEdges(losers);
      copy.removeVertex(sink);
    }
    copy.removeAllVertices(getIsolatedVerties(copy));

    while ((opt = getSource(copy)).isPresent()) {
      V source = opt.get();
      Collection<E> losers =
          copy.edgeSet()
              .stream()
              .filter(e -> copy.getEdgeTarget(e).equals(source))
              .collect(Collectors.toList());

      log.trace("removing source {} and incoming edges {}", source, losers);
      copy.removeAllEdges(losers);
      copy.removeVertex(source);
    }
    List<V> orderedBy = new ArrayList<>(copy.vertexSet());
    orderedBy.sort(
        (l, r) -> {
          int leftDiff = copy.outDegreeOf(l) - copy.inDegreeOf(l);
          int rightDiff = copy.outDegreeOf(r) - copy.inDegreeOf(r);
          return -Integer.compare(leftDiff, rightDiff);
        });
    for (V v : orderedBy) {
      Collection<E> outgoingEdges = new HashSet<>(copy.outgoingEdgesOf(v));
      Collection<E> incomingEdges = new HashSet<>(copy.incomingEdgesOf(v));
      log.trace("incoming {}", incomingEdges);
      feedbackArcs.addAll(incomingEdges);
      copy.removeAllEdges(outgoingEdges);
      copy.removeAllEdges(incomingEdges);
      copy.removeVertex(v);
    }
    log.trace("copy graph is {}", copy);
    log.trace("feedbackArcs {}", feedbackArcs);
  }

  public void reverseFeedbackArcs() {
    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
    for (E edge : feedbackArcs) {
      V source = graph.getEdgeSource(edge);
      V target = graph.getEdgeTarget(edge);
      graph.removeEdge(edge);
      graph.addEdge(target, source, edge);
    }
  }
}
