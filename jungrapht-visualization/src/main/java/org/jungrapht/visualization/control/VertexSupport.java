package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.VisualizationServer;

/**
 * interface to support the creation of new vertices by the EditingGraphMousePlugin.
 * SimpleVertexSupport is a sample implementation.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public interface VertexSupport<V, E> {

  void startVertexCreate(VisualizationServer<V, E> vv, Point2D point);

  void midVertexCreate(VisualizationServer<V, E> vv, Point2D point);

  void endVertexCreate(VisualizationServer<V, E> vv, Point2D point);
}
