package org.jungrapht.visualization.control;

import com.google.common.base.Preconditions;
import java.awt.geom.Point2D;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jungrapht.visualization.BasicVisualizationServer;
import org.jungrapht.visualization.VisualizationModel;

/**
 * sample implementation showing how to use the NodeSupport interface member of the
 * EditingGraphMousePlugin. override midNodeCreate and endNodeCreate for more elaborate
 * implementations
 *
 * @author Tom Nelson
 * @param <N> the node type
 * @param <E> the edge type
 */
public class SimpleNodeSupport<N, E> implements NodeSupport<N, E> {

  protected Supplier<N> nodeFactory;

  public SimpleNodeSupport(Supplier<N> nodeFactory) {
    this.nodeFactory = nodeFactory;
  }

  public void startNodeCreate(BasicVisualizationServer<N, E> vv, Point2D point) {
    Preconditions.checkState(
        vv.getModel().getNetwork().getType().isModifiable(), "graph must be mutable");
    N newNode = nodeFactory.get();
    VisualizationModel<N, E> visualizationModel = vv.getModel();
    Graph<N, E> graph = visualizationModel.getNetwork();
    graph.addVertex(newNode);
    Point2D p2d = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point);
    visualizationModel.getLayoutModel().set(newNode, p2d.getX(), p2d.getY());
    vv.repaint();
  }

  public void midNodeCreate(BasicVisualizationServer<N, E> vv, Point2D point) {
    // noop
  }

  public void endNodeCreate(BasicVisualizationServer<N, E> vv, Point2D point) {
    //noop
  }

  public Supplier<N> getNodeFactory() {
    return nodeFactory;
  }

  public void setNodeFactory(Supplier<N> nodeFactory) {
    this.nodeFactory = nodeFactory;
  }
}
