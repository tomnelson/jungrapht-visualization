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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A renderer that will fill node shapes with a GradientPaint
 *
 * @author Tom Nelson
 * @param <N> the node type
 * @param <N> the edge type
 */
public class GradientNodeRenderer<N, E> implements Renderer.Node<N, E> {

  private static final Logger log = LoggerFactory.getLogger(GradientNodeRenderer.class);

  Color colorOne;
  Color colorTwo;
  Color pickedColorOne;
  Color pickedColorTwo;
  MutableSelectedState<N> mutablePickedState;
  boolean cyclic;

  public GradientNodeRenderer(
      VisualizationServer<N, ?> vv, Color colorOne, Color colorTwo, boolean cyclic) {
    this.colorOne = colorOne;
    this.colorTwo = colorTwo;
    this.cyclic = cyclic;
  }

  public GradientNodeRenderer(
      VisualizationServer<N, ?> vv,
      Color colorOne,
      Color colorTwo,
      Color pickedColorOne,
      Color pickedColorTwo,
      boolean cyclic) {
    this.colorOne = colorOne;
    this.colorTwo = colorTwo;
    this.pickedColorOne = pickedColorOne;
    this.pickedColorTwo = pickedColorTwo;
    this.mutablePickedState = vv.getSelectedNodeState();
    this.cyclic = cyclic;
  }

  public void paintNode(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v) {
    if (renderContext.getNodeIncludePredicate().test(v)) {
      // get the shape to be rendered
      Shape shape = renderContext.getNodeShapeFunction().apply(v);
      LayoutModel<N> layoutModel = visualizationModel.getLayoutModel();
      Point p = layoutModel.apply(v);
      Point2D p2d =
          renderContext
              .getMultiLayerTransformer()
              .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p.x, p.y));

      float x = (float) p2d.getX();
      float y = (float) p2d.getY();

      // create a transform that translates to the location of
      // the node to be rendered
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      // transform the node shape with xtransform
      shape = xform.createTransformedShape(shape);
      log.trace("prepared a shape for " + v + " to go at " + p);

      paintShapeForNode(renderContext, v, shape);
    }
  }

  protected void paintShapeForNode(RenderContext<N, E> renderContext, N v, Shape shape) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Paint oldPaint = g.getPaint();
    Rectangle r = shape.getBounds();
    float y2 = (float) r.getMaxY();
    if (cyclic) {
      y2 = (float) (r.getMinY() + r.getHeight() / 2);
    }

    Paint fillPaint = null;
    if (mutablePickedState != null && mutablePickedState.isSelected(v)) {
      fillPaint =
          new GradientPaint(
              (float) r.getMinX(),
              (float) r.getMinY(),
              pickedColorOne,
              (float) r.getMinX(),
              y2,
              pickedColorTwo,
              cyclic);
    } else {
      fillPaint =
          new GradientPaint(
              (float) r.getMinX(),
              (float) r.getMinY(),
              colorOne,
              (float) r.getMinX(),
              y2,
              colorTwo,
              cyclic);
    }
    g.setPaint(fillPaint);
    g.fill(shape);
    g.setPaint(oldPaint);
    Paint drawPaint = renderContext.getNodeDrawPaintFunction().apply(v);
    if (drawPaint != null) {
      g.setPaint(drawPaint);
    }
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
