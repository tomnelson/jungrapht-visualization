package org.jungrapht.visualization.decorators;

import static org.jungrapht.visualization.MultiLayerTransformer.Layer.LAYOUT;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * {@code Function} to supply a {@code GradientPaint} to graph edges. If the initial edge color is
 * black, provides a gradient from a light gray to black, otherwise provides a gradient from a
 * lighter to a darker version of the initial edge draw color.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class GradientEdgePaintFunction<V, E> implements Function<E, Paint> {

  protected VisualizationServer<V, E> visualizationServer;
  protected Function<E, Paint> originalEdgePaintFunction;

  public GradientEdgePaintFunction(VisualizationServer<V, E> visualizationServer) {
    this.visualizationServer = visualizationServer;
    this.originalEdgePaintFunction =
        visualizationServer.getRenderContext().getEdgeDrawPaintFunction();
  }

  @Override
  public Paint apply(E edge) {

    LayoutModel<V> layoutModel = visualizationServer.getVisualizationModel().getLayoutModel();
    Graph<V, E> graph = layoutModel.getGraph();
    MultiLayerTransformer transformer =
        visualizationServer.getRenderContext().getMultiLayerTransformer();
    // get the edge endpoints and locations in view space
    V source = graph.getEdgeSource(edge);
    V target = graph.getEdgeTarget(edge);
    Point sourcePoint = layoutModel.apply(source);
    Point targetPoint = layoutModel.apply(target);
    Point2D sourceInView = transformer.transform(LAYOUT, sourcePoint.x, sourcePoint.y);
    Point2D targetInView = transformer.transform(LAYOUT, targetPoint.x, targetPoint.y);
    Color edgeColor = (Color) originalEdgePaintFunction.apply(edge);
    if (Color.black.equals(edgeColor)) {
      return new GradientPaint(sourceInView, Color.lightGray.brighter(), targetInView, Color.black);
    } else {
      return new GradientPaint(
          sourceInView, edgeColor.brighter().brighter(), targetInView, edgeColor.darker().darker());
    }
  }
}
