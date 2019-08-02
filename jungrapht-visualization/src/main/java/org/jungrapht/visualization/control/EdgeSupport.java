package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.DefaultVisualizationServer;

/**
 * interface to support the creation of new edges by the EditingGraphMousePlugin SimpleEdgeSupport
 * is a sample implementation
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <V> the edge type
 */
public interface EdgeSupport<V, E> {

  void startEdgeCreate(DefaultVisualizationServer<V, E> vv, V startVertex, Point2D startPoint);

  void midEdgeCreate(DefaultVisualizationServer<V, E> vv, Point2D midPoint);

  void endEdgeCreate(DefaultVisualizationServer<V, E> vv, V endVertex);

  void abort(DefaultVisualizationServer<V, E> vv);
}
