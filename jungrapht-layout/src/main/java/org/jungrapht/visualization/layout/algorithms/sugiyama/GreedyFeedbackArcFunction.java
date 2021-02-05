package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Collection;
import java.util.function.Function;
import org.jgrapht.Graph;

public class GreedyFeedbackArcFunction<V, E> implements Function<Graph<V, E>, Collection<E>> {

  @Override
  public Collection<E> apply(Graph<V, E> graph) {
    GreedyCycleRemoval<V, E> greedyCycleRemoval = new GreedyCycleRemoval(graph);
    return greedyCycleRemoval.getFeedbackArcs();
  }
}
