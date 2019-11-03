package org.jungrapht.visualization.layout.algorithms.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreedyCycleRemoval<V, E> {

  private static Logger log = LoggerFactory.getLogger(GreedyCycleRemoval.class);

  private Graph<V, E> graph;

  List<V> left = new ArrayList<>();
  List<V> right = new LinkedList<>();

  public GreedyCycleRemoval(Graph<V, E> graph) {
    this.graph = graph;
    doit();
  }

  void doit() {
    for (V v : graph.vertexSet()) {
      if (graph.outDegreeOf(v) == 0) {
        right.add(0, v);
      } else if (graph.inDegreeOf(v) == 0) {
        left.add(v);
      }
    }
    graph.removeAllVertices(right);
    graph.removeAllVertices(left);
    left.addAll(
        graph
            .vertexSet()
            .stream()
            .sorted(
                (v, w) -> {
                  int vDelta = graph.outDegreeOf(v) - graph.inDegreeOf(v);
                  int wDelta = graph.outDegreeOf(w) - graph.inDegreeOf(w);
                  log.info(
                      "compare {} {} d{}  d{} is {}",
                      w,
                      v,
                      wDelta,
                      vDelta,
                      Integer.compare(wDelta, vDelta));
                  return Integer.compare(wDelta, vDelta);
                })
            .collect(Collectors.toList()));

    log.info("left is {}", left);
    log.info("right is {}", right);
    log.info("graph is {}", graph);
  }
}
