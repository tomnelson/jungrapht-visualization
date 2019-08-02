package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.DefaultVisualizationServer;

/**
 * interface to support the creation of new vertices by the EditingGraphMousePlugin.
 * SimpleVertexSupport is a sample implementation.
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 */
public interface VertexSupport<V, E> {

  void startVertexCreate(DefaultVisualizationServer<V, E> vv, Point2D point);

  void midVertexCreate(DefaultVisualizationServer<V, E> vv, Point2D point);

  void endVertexCreate(DefaultVisualizationServer<V, E> vv, Point2D point);
}
