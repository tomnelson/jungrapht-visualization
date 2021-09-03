package org.jungrapht.visualization.renderers;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.selection.SelectedState;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class AbstractVertexRenderer<V, E> implements Renderer.Vertex<V, E> {

  @Override
  public void paintVertex(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v) {
    if (renderContext.getVertexIncludePredicate().test(v)) {
      paintIconForVertex(renderContext, layoutModel, v);
    }
  }

  /**
   * LightweightVertexRenderer overrides to provide a lightweight shape HeavyweightVertexRenderer
   * uses the configured vertexShapeFunction
   *
   * @param renderContext
   * @param vertex
   * @return
   */
  protected abstract Shape getVertexShape(RenderContext<V, E> renderContext, V vertex);

  /**
   * Returns the vertex shape in layout coordinates.
   *
   * @param v the vertex whose shape is to be returned
   * @param coords the x and y view coordinates. used by caller to place the icon if available
   * @return the vertex shape in view coordinates
   */
  protected Shape prepareFinalVertexShape(
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v, int[] coords) {

    // get the shape to be rendered
    Shape shape = getVertexShape(renderContext, v);
    Point p = layoutModel.apply(v);
    // p is the vertex location in layout coordinates
    Point2D p2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, p.x, p.y);
    // now p is in view coordinates, ready to be further transformed by any transform in the
    // graphics context
    float x = (float) p2d.getX();
    float y = (float) p2d.getY();
    // coords values are set and returned to the caller in order to place an Icon instead of Shape
    coords[0] = (int) x;
    coords[1] = (int) y;
    // create a transform that translates to the location of
    // the vertex to be rendered
    AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
    // return the transformed vertex shape
    return xform.createTransformedShape(shape);
  }

  protected void paintShapeForVertex(RenderContext<V, E> renderContext, V v, Shape shape) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Paint oldPaint = g.getPaint();
    SelectedState<V> vertexSelectedState = renderContext.getSelectedVertexState();
    Paint fillPaint;
    Paint drawPaint;
    Stroke drawStroke;
    if (vertexSelectedState.isSelected(v)) {
      // selected colors and stroke
      fillPaint = renderContext.getSelectedVertexFillPaintFunction().apply(v);
      drawPaint = renderContext.getSelectedVertexDrawPaintFunction().apply(v);
      drawStroke = renderContext.getSelectedVertexStrokeFunction().apply(v);
    } else {
      fillPaint = renderContext.getVertexFillPaintFunction().apply(v);
      drawPaint = renderContext.getVertexDrawPaintFunction().apply(v);
      drawStroke = renderContext.getVertexStrokeFunction().apply(v);
    }
    if (fillPaint != null) {
      g.setPaint(fillPaint);
      g.fill(shape);
      g.setPaint(oldPaint);
    }
    if (drawPaint != null) {
      g.setPaint(drawPaint);
      Stroke oldStroke = g.getStroke();
      if (drawStroke != null) {
        g.setStroke(drawStroke);
      }
      g.draw(shape);
      g.setPaint(oldPaint);
      g.setStroke(oldStroke);
    }
  }

  protected abstract void paintIconForVertex(
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v);
}
