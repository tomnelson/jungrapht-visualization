package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.BasicVisualizationServer;

/**
 * interface to support the creation of new edges by the EditingGraphMousePlugin SimpleEdgeSupport
 * is a sample implementation
 *
 * @author Tom Nelson
 * @param <N> the node type
 * @param <N> the edge type
 */
public interface EdgeSupport<N, E> {

  void startEdgeCreate(BasicVisualizationServer<N, E> vv, N startNode, Point2D startPoint);

  void midEdgeCreate(BasicVisualizationServer<N, E> vv, Point2D midPoint);

  void endEdgeCreate(BasicVisualizationServer<N, E> vv, N endNode);

  void abort(BasicVisualizationServer<N, E> vv);
}
