package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.DefaultVisualizationServer;

public interface EdgeEffects<V, E> {

  void startEdgeEffects(DefaultVisualizationServer<V, E> vv, Point2D down, Point2D out);

  void midEdgeEffects(DefaultVisualizationServer<V, E> vv, Point2D down, Point2D out);

  void endEdgeEffects(DefaultVisualizationServer<V, E> vv);

  void startArrowEffects(DefaultVisualizationServer<V, E> vv, Point2D down, Point2D out);

  void midArrowEffects(DefaultVisualizationServer<V, E> vv, Point2D down, Point2D out);

  void endArrowEffects(DefaultVisualizationServer<V, E> vv);
}
