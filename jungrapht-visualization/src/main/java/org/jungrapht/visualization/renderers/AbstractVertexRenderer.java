package org.jungrapht.visualization.renderers;

import java.awt.*;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class AbstractVertexRenderer<V, E> implements Renderer.Vertex<V, E> {

  @Override
  public void paintVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    if (renderContext.getVertexIncludePredicate().test(v)) {
      paintIconForVertex(renderContext, visualizationModel, v);
    }
  }

  protected void paintShapeForVertex(RenderContext<V, E> renderContext, V v, Shape shape) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Paint oldPaint = g.getPaint();
    Paint fillPaint = renderContext.getVertexFillPaintFunction().apply(v);
    if (fillPaint != null) {
      g.setPaint(fillPaint);
      g.fill(shape);
      g.setPaint(oldPaint);
    }
    Paint drawPaint = renderContext.getVertexDrawPaintFunction().apply(v);
    if (drawPaint != null) {
      g.setPaint(drawPaint);
      Stroke oldStroke = g.getStroke();
      Stroke stroke = renderContext.getVertexStrokeFunction().apply(v);
      if (stroke != null) {
        g.setStroke(stroke);
      }
      g.draw(shape);
      g.setPaint(oldPaint);
      g.setStroke(oldStroke);
    }
  }

  protected abstract void paintIconForVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v);
}
