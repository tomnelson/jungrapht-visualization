package org.jungrapht.visualization.layout.algorithms;

import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;

public interface Layered<V, E> {

  void setLayering(Layering layering);

  void setMaxLevelCrossFunction(Function<Graph<V, E>, Integer> maxLevelCrossFunction);
}
