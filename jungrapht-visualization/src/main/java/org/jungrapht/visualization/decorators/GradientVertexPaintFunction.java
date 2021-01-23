package org.jungrapht.visualization.decorators;

import static org.jungrapht.visualization.MultiLayerTransformer.Layer.LAYOUT;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.function.Function;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * {@code Function} to supply a {@code RadialGradientPaint} to graph vertices.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class GradientVertexPaintFunction<V, E> implements Function<V, Paint> {

  protected VisualizationServer<V, E> visualizationServer;
  protected Function<V, Paint> originalVertexPaintFunction;

  public GradientVertexPaintFunction(VisualizationServer<V, E> visualizationServer) {
    this.visualizationServer = visualizationServer;
    this.originalVertexPaintFunction =
        visualizationServer.getRenderContext().getVertexFillPaintFunction();
  }

  @Override
  public Paint apply(V vertex) {

    LayoutModel<V> layoutModel = visualizationServer.getVisualizationModel().getLayoutModel();
    MultiLayerTransformer transformer =
        visualizationServer.getRenderContext().getMultiLayerTransformer();
    float radius =
        (float) visualizationServer.getRenderContext().getVertexBoundsFunction().apply(vertex).width
            / 2;
    Point layoutPoint = layoutModel.apply(vertex);
    Point2D vertexInView = transformer.transform(LAYOUT, layoutPoint.x, layoutPoint.y);
    Color vertexColor = (Color) originalVertexPaintFunction.apply(vertex);
    Color vertexColor2 = Color.white;
    return new RadialGradientPaint(
        vertexInView, radius, new float[] {0, 1}, new Color[] {vertexColor2, vertexColor});
  }
}
