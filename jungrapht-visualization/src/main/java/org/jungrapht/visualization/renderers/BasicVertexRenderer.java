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

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.swing.Icon;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicVertexRenderer<V, E> implements Renderer.Vertex<V, E> {

  private static final Logger log = LoggerFactory.getLogger(BasicVertexRenderer.class);

  public void paintVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    if (renderContext.getVertexIncludePredicate().test(v)) {
      paintIconForVertex(renderContext, visualizationModel, v);
    }
  }

  /**
   * Returns the vertex shape in layout coordinates.
   *
   * @param v the vertex whose shape is to be returned
   * @param coords the x and y view coordinates
   * @return the vertex shape in view coordinates
   */
  protected Shape prepareFinalVertexShape(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      V v,
      int[] coords) {

    // get the shape to be rendered
    Shape shape = renderContext.getVertexShapeFunction().apply(v);
    Point p = visualizationModel.getLayoutModel().apply(v);
    // p is the vertex location in layout coordinates
    log.trace("prepared a shape for " + v + " to go at " + p);
    Point2D p2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p.x, p.y));
    // now p is in view coordinates, ready to be further transformed by any transform in the
    // graphics context
    float x = (float) p2d.getX();
    float y = (float) p2d.getY();
    coords[0] = (int) x;
    coords[1] = (int) y;
    // create a transform that translates to the location of
    // the vertex to be rendered
    AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
    // transform the vertex shape with xtransform
    shape = xform.createTransformedShape(shape);
    return shape;
  }

  /**
   * Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>.
   *
   * @param v the vertex to be painted
   */
  protected void paintIconForVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    int[] coords = new int[2];
    Shape shape = prepareFinalVertexShape(renderContext, visualizationModel, v, coords);

    if (renderContext.getVertexIconFunction() != null) {
      Icon icon = renderContext.getVertexIconFunction().apply(v);
      if (icon != null) {

        g.draw(icon, renderContext.getScreenDevice(), shape, coords[0], coords[1]);

      } else {
        paintShapeForVertex(renderContext, visualizationModel, v, shape);
      }
    } else {
      paintShapeForVertex(renderContext, visualizationModel, v, shape);
    }
  }

  protected void paintShapeForVertex(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      V v,
      Shape shape) {
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
}
