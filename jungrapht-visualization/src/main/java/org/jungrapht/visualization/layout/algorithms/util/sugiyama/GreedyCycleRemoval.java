package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

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

  public Collection<E> getFeedbackArcs() {
    return this.feedbackArcs;
  }

  private Optional<V> getSink(Graph<V, E> graph) {
    return graph.vertexSet().stream().filter(v -> graph.outgoingEdgesOf(v).isEmpty()).findFirst();
  }

  private Optional<V> getSource(Graph<V, E> graph) {
    return graph.vertexSet().stream().filter(v -> graph.incomingEdgesOf(v).isEmpty()).findFirst();
  }

  private Collection<V> getIsolatedVerties(Graph<V, E> graph) {
    return graph
        .vertexSet()
        .stream()
        .filter(v -> graph.incomingEdgesOf(v).isEmpty() && graph.outgoingEdgesOf(v).isEmpty())
        .collect(Collectors.toList());
  }

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
          if (copy.outDegreeOf(l) - copy.inDegreeOf(l) > copy.outDegreeOf(r) - copy.inDegreeOf(r)) {
            return -1;
          } else if (copy.outDegreeOf(l) - copy.inDegreeOf(l)
              == copy.outDegreeOf(r) - copy.inDegreeOf(r)) {
            return 0;
          } else {
            return 1;
          }
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
}
