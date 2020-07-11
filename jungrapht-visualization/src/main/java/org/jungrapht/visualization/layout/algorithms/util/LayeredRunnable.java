package org.jungrapht.visualization.layout.algorithms.util;

import java.util.List;
import java.util.Map;
import org.jungrapht.visualization.layout.model.Point;

public interface LayeredRunnable<E> extends Runnable {
  Map<E, List<Point>> getEdgePointMap();
}
