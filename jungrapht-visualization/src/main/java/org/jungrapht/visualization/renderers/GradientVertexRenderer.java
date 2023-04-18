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
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A renderer that will fill vertex shapes with a GradientPaint
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class GradientVertexRenderer<V, E> implements Renderer.Vertex<V, E> {

  private static final Logger log = LoggerFactory.getLogger(GradientVertexRenderer.class);

  Color colorOne;
  Color colorTwo;
  Color pickedColorOne;
  Color pickedColorTwo;
  MutableSelectedState<V> mutablePickedState;
  boolean cyclic;

  public GradientVertexRenderer(Color colorOne, Color colorTwo, boolean cyclic) {
    this.colorOne = colorOne;
    this.colorTwo = colorTwo;
    this.cyclic = cyclic;
  }

  public GradientVertexRenderer(
      MutableSelectedState<V> mutableSelectedState,
      Color colorOne,
      Color colorTwo,
      Color pickedColorOne,
      Color pickedColorTwo,
      boolean cyclic) {
    this.mutablePickedState = mutableSelectedState;
    this.colorOne = colorOne;
    this.colorTwo = colorTwo;
    this.pickedColorOne = pickedColorOne;
    this.pickedColorTwo = pickedColorTwo;
    this.cyclic = cyclic;
  }

  public void paintVertex(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v) {
    if (renderContext.getVertexIncludePredicate().test(v)) {
      // get the shape to be rendered
      Shape shape = renderContext.getVertexShapeFunction().apply(v);
      Point p = layoutModel.apply(v);
      Point2D p2d =
          renderContext
              .getMultiLayerTransformer()
              .transform(MultiLayerTransformer.Layer.LAYOUT, p.x, p.y);

      double x = p2d.getX();
      double y = p2d.getY();

      // create a transform that translates to the location of
      // the vertex to be rendered
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      // transform the vertex shape with xtransform
      shape = xform.createTransformedShape(shape);
      log.trace("prepared a shape for " + v + " to go at " + p);

      paintShapeForVertex(renderContext, v, shape);
    }
  }

  protected void paintShapeForVertex(RenderContext<V, E> renderContext, V v, Shape shape) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Paint oldPaint = g.getPaint();
    Rectangle r = shape.getBounds();
    float y2 = (float) r.getMaxY();
    if (cyclic) {
      y2 = (float) (r.getMinY() + r.getHeight() / 2);
    }

    Paint fillPaint;
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
    Paint drawPaint = renderContext.getVertexDrawPaintFunction().apply(v);
    if (drawPaint != null) {
      g.setPaint(drawPaint);
    }
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
