package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.BasicVisualizationServer;

/**
 * interface to support the creation of new nodes by the EditingGraphMousePlugin. SimpleNodeSupport
 * is a sample implementation.
 *
 * @author Tom Nelson
 * @param <N> the node type
 */
public interface NodeSupport<N, E> {

  void startNodeCreate(BasicVisualizationServer<N, E> vv, Point2D point);

  void midNodeCreate(BasicVisualizationServer<N, E> vv, Point2D point);

  void endNodeCreate(BasicVisualizationServer<N, E> vv, Point2D point);
}
