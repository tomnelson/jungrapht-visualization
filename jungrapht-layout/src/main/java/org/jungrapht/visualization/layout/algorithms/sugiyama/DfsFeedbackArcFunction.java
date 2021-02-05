package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiFunction;
import org.jgrapht.Graph;

public class DfsFeedbackArcFunction<V, E>
    implements BiFunction<Graph<V, E>, Comparator<V>, Collection<E>> {

  @Override
  public Collection<E> apply(Graph<V, E> graph, Comparator<V> comparator) {
    DFSCycleRemoval<V, E> dfsCycleRemoval = new DFSCycleRemoval(graph, comparator);
    return dfsCycleRemoval.getFeedbackArcs();
  }
}
