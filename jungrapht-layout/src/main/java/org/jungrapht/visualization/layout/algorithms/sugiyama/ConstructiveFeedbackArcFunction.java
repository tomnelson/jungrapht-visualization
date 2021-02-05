package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;
import org.jgrapht.Graph;

public class ConstructiveFeedbackArcFunction<V, E> implements Function<Graph<V, E>, Collection<E>> {

  Comparator<E> comparator;

  public ConstructiveFeedbackArcFunction(Comparator<E> comparator) {
    this.comparator = comparator;
  }

  @Override
  public Collection<E> apply(Graph<V, E> graph) {
    ConstructiveCycleRemoval<V, E> cycleRemoval = new ConstructiveCycleRemoval(graph);
    return cycleRemoval.getFeedbackArcs();
  }
}
