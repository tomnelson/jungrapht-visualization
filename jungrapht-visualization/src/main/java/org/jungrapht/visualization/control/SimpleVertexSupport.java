package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;

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

  public void startVertexCreate(VisualizationServer<V, E> vv, Point2D point) {
    Objects.requireNonNull(
        vv.getVisualizationModel().getGraph().getType().isModifiable(), "graph must be mutable");
    V newVertex = vertexFactory.get();
    VisualizationModel<V, E> visualizationModel = vv.getVisualizationModel();
    Graph<V, E> graph = visualizationModel.getGraph();
    graph.addVertex(newVertex);
    Point2D p2d = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(point);
    visualizationModel.getLayoutModel().set(newVertex, p2d.getX(), p2d.getY());
    vv.repaint();
  }

  public void midVertexCreate(VisualizationServer<V, E> vv, Point2D point) {
    // noop
  }

  public void endVertexCreate(VisualizationServer<V, E> vv, Point2D point) {
    //noop
  }

  public Supplier<V> getVertexFactory() {
    return vertexFactory;
  }

  public void setVertexFactory(Supplier<V> vertexFactory) {
    this.vertexFactory = vertexFactory;
  }
}
