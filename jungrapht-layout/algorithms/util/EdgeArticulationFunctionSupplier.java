package org.jungrapht.visualization.layout.algorithms.util;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.jungrapht.visualization.layout.model.Point;

public interface EdgeArticulationFunctionSupplier<E> {

  default Function<E, List<Point>> getEdgeArticulationFunction() {
    return e -> Collections.emptyList();
  }
}
