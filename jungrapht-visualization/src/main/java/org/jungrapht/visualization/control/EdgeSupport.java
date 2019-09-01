package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.VisualizationServer;

/**
 * interface to support the creation of new edges by the EditingGraphMousePlugin SimpleEdgeSupport
 * is a sample implementation
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public interface EdgeSupport<V, E> {

  void startEdgeCreate(VisualizationServer<V, E> vv, V startVertex, Point2D startPoint);

  void midEdgeCreate(VisualizationServer<V, E> vv, Point2D midPoint);

  void endEdgeCreate(VisualizationServer<V, E> vv, V endVertex);

  void abort(VisualizationServer<V, E> vv);
}
