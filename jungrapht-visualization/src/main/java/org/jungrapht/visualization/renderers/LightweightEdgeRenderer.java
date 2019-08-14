/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package org.jungrapht.visualization.renderers;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Predicate;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.Context;

public class LightweightEdgeRenderer<V, E> implements Renderer.Edge<V, E> {

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

  protected Shape prepareFinalEdgeShape(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      E e,
      int[] coords,
      boolean[] loop) {
    V v1 = visualizationModel.getGraph().getEdgeSource(e);
    V v2 = visualizationModel.getGraph().getEdgeTarget(e);

    org.jungrapht.visualization.layout.model.Point p1 =
        visualizationModel.getLayoutModel().apply(v1);
    Point p2 = visualizationModel.getLayoutModel().apply(v2);
    Point2D p2d1 =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p1.x, p1.y));
    Point2D p2d2 =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p2.x, p2.y));
    float x1 = (float) p2d1.getX();
    float y1 = (float) p2d1.getY();
    float x2 = (float) p2d2.getX();
    float y2 = (float) p2d2.getY();
    coords[0] = (int) x1;
    coords[1] = (int) y1;
    coords[2] = (int) x2;
    coords[3] = (int) y2;

    boolean isLoop = loop[0] = v1.equals(v2);
    Shape s2 = renderContext.getVertexShapeFunction().apply(v2);
    Shape edgeShape = EdgeShape.line().apply(Context.getInstance(visualizationModel.getGraph(), e));

    AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

    if (isLoop) {
      // this is a self-loop. scale it is larger than the vertex
      // it decorates and translate it so that its nadir is
      // at the center of the vertex.
      Rectangle2D s2Bounds = s2.getBounds2D();
      xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
      xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);

    } else {
      // this is a normal edge. Rotate it to the angle between
      // vertex endpoints, then scale it to the distance between
      // the vertices
      float dx = x2 - x1;
      float dy = y2 - y1;
      float thetaRadians = (float) Math.atan2(dy, dx);
      xform.rotate(thetaRadians);
      float dist = (float) Math.sqrt(dx * dx + dy * dy);
      xform.scale(dist, 1.0);
    }
    edgeShape = xform.createTransformedShape(edgeShape);

    return edgeShape;
  }

  /**
   * Draws the edge <code>e</code>, whose endpoints are at <code>(x1,y1)</code> and <code>(x2,y2)
   * </code>, on the graphics context <code>g</code>. The <code>Shape</code> provided by the <code>
   * EdgeShapeFunction</code> instance is scaled in the x-direction so that its width is equal to
   * the distance between <code>(x1,y1)</code> and <code>(x2,y2)</code>.
   *
   * @param e the edge to be drawn
   */
  protected void drawSimpleEdge(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {

    int[] coords = new int[4];
    boolean[] loop = new boolean[1];
    Shape edgeShape = prepareFinalEdgeShape(renderContext, visualizationModel, e, coords, loop);

    GraphicsDecorator g = renderContext.getGraphicsContext();

    Paint oldPaint = g.getPaint();

    // get Paints for filling and drawing
    // (filling is done first so that drawing and label use same Paint)
    Paint fill_paint = renderContext.getEdgeFillPaintFunction().apply(e);
    if (fill_paint != null) {
      g.setPaint(fill_paint);
      g.fill(edgeShape);
    }
    Paint draw_paint = renderContext.getEdgeDrawPaintFunction().apply(e);
    if (draw_paint != null) {
      g.setPaint(draw_paint);
      g.draw(edgeShape);
    }
    // restore old paint
    g.setPaint(oldPaint);
  }

  public EdgeArrowRenderingSupport<V, E> getEdgeArrowRenderingSupport() {
    return null;
  }

  public void setEdgeArrowRenderingSupport(
      EdgeArrowRenderingSupport<V, E> edgeArrowRenderingSupport) {}
}