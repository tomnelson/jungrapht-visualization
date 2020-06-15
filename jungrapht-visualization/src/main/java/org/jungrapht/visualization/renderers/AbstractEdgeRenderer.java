package org.jungrapht.visualization.renderers;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.Context;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class AbstractEdgeRenderer<V, E> implements Renderer.Edge<V, E> {

  @Override
  public void paintEdge(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {
    GraphicsDecorator g2d = renderContext.getGraphicsContext();
    if (!renderContext.getEdgeIncludePredicate().test(e)) {
      return;
    }

    // don't draw edge if either incident vertex is not drawn
    V u = visualizationModel.getGraph().getEdgeSource(e);
    V v = visualizationModel.getGraph().getEdgeTarget(e);
    Predicate<V> vertexIncludePredicate = renderContext.getVertexIncludePredicate();
    if (!vertexIncludePredicate.test(u) || !vertexIncludePredicate.test(v)) {
      return;
    }

    Stroke new_stroke = renderContext.edgeStrokeFunction().apply(e);
    Stroke old_stroke = g2d.getStroke();
    if (new_stroke != null) {
      g2d.setStroke(new_stroke);
    }

    drawSimpleEdge(renderContext, visualizationModel, e);

    // restore paint and stroke
    if (new_stroke != null) {
      g2d.setStroke(old_stroke);
    }
  }

  /**
   * For Heavyweight graph visualizations, edges are rendered with the user requested
   * edgeShapeFunction. For Lightweight graph visualizations, edges are rendered with a
   * (lightweight) line edge except when they are articulated edges (sugiyama layout). The
   * LightweightEdgeRenderer overrides this method to supply the correct edge shape.
   *
   * @param edgeShapeFunction the user specified edgeShapeFunction
   * @param edge the edge to render
   * @param graph for the Function context
   * @return the edge shape, heavyweight (anything) or lightweight (line or articulated line)
   */
  protected abstract Shape getEdgeShape(
      Function<Context<Graph<V, E>, E>, Shape> edgeShapeFunction, E edge, Graph<V, E> graph);

  protected abstract void drawSimpleEdge(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e);
}
