package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.VisualizationServer;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public interface EdgeEffects<V, E> {

  void startEdgeEffects(VisualizationServer<V, E> vv, Point2D down, Point2D out);

  void midEdgeEffects(VisualizationServer<V, E> vv, Point2D down, Point2D out);

  void endEdgeEffects(VisualizationServer<V, E> vv);

  void startArrowEffects(VisualizationServer<V, E> vv, Point2D down, Point2D out);

  void midArrowEffects(VisualizationServer<V, E> vv, Point2D down, Point2D out);

  void endArrowEffects(VisualizationServer<V, E> vv);
}
