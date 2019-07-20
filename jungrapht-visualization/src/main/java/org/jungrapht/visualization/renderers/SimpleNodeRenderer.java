/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package org.jungrapht.visualization.renderers;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.function.Function;
import javax.swing.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleNodeRenderer<N, E> implements Renderer.Node<N, E> {

  private static final Logger log = LoggerFactory.getLogger(SimpleNodeRenderer.class);

  private Function<N, Shape> simpleNodeShapeFunction = n -> new Ellipse2D.Float(-6.f, -6.f, 12, 12);

  public void paintNode(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v) {
    if (renderContext.getNodeIncludePredicate().test(v)) {
      paintIconForNode(renderContext, visualizationModel, v);
    }
  }

  /**
   * Returns the node shape in layout coordinates.
   *
   * @param v the node whose shape is to be returned
   * @param coords the x and y view coordinates
   * @return the node shape in view coordinates
   */
  protected Shape prepareFinalNodeShape(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E> visualizationModel,
      N v,
      int[] coords) {

    // get the shape to be rendered
    Shape shape = simpleNodeShapeFunction.apply(v);
    Point p = visualizationModel.getLayoutModel().apply(v);
    // p is the node location in layout coordinates
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
    // the node to be rendered
    AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
    // transform the node shape with xtransform
    shape = xform.createTransformedShape(shape);
    return shape;
  }

  /**
   * Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>.
   *
   * @param v the node to be painted
   */
  protected void paintIconForNode(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    int[] coords = new int[2];
    Shape shape = prepareFinalNodeShape(renderContext, visualizationModel, v, coords);

    paintShapeForNode(renderContext, visualizationModel, v, shape);
  }

  protected void paintShapeForNode(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E> visualizationModel,
      N v,
      Shape shape) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Paint oldPaint = g.getPaint();
    Paint fillPaint = renderContext.getNodeFillPaintFunction().apply(v);
    if (fillPaint != null) {
      g.setPaint(fillPaint);
      g.fill(shape);
      g.setPaint(oldPaint);
    }
    Paint drawPaint = renderContext.getNodeDrawPaintFunction().apply(v);
    if (drawPaint != null) {
      g.setPaint(drawPaint);
      Stroke oldStroke = g.getStroke();
      Stroke stroke = renderContext.getNodeStrokeFunction().apply(v);
      if (stroke != null) {
        g.setStroke(stroke);
      }
      g.draw(shape);
      g.setPaint(oldPaint);
      g.setStroke(oldStroke);
    }
  }
}
