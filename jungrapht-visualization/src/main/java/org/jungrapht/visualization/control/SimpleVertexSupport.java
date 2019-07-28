package org.jungrapht.visualization.control;

import com.google.common.base.Preconditions;
import java.awt.geom.Point2D;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jungrapht.visualization.BasicVisualizationServer;
import org.jungrapht.visualization.VisualizationModel;

/**
 * sample implementation showing how to use the VertexSupport interface member of the
 * EditingGraphMousePlugin. override midVertexCreate and endVertexCreate for more elaborate
 * implementations
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class SimpleVertexSupport<V, E> implements VertexSupport<V, E> {

  protected Supplier<V> vertexFactory;

  public SimpleVertexSupport(Supplier<V> vertexFactory) {
    this.vertexFactory = vertexFactory;
  }

  public void startVertexCreate(BasicVisualizationServer<V, E> vv, Point2D point) {
    Preconditions.checkState(
        vv.getModel().getGraph().getType().isModifiable(), "graph must be mutable");
    V newVertex = vertexFactory.get();
    VisualizationModel<V, E> visualizationModel = vv.getModel();
    Graph<V, E> graph = visualizationModel.getGraph();
    graph.addVertex(newVertex);
    Point2D p2d = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point);
    visualizationModel.getLayoutModel().set(newVertex, p2d.getX(), p2d.getY());
    vv.repaint();
  }

  public void midVertexCreate(BasicVisualizationServer<V, E> vv, Point2D point) {
    // noop
  }

  public void endVertexCreate(BasicVisualizationServer<V, E> vv, Point2D point) {
    //noop
  }

  public Supplier<V> getVertexFactory() {
    return vertexFactory;
  }

  public void setVertexFactory(Supplier<V> vertexFactory) {
    this.vertexFactory = vertexFactory;
  }
}
