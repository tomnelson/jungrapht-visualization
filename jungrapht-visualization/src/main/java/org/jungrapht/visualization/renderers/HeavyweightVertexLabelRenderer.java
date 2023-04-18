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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.BidirectionalTransformer;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.transform.shape.ShapeTransformer;
import org.jungrapht.visualization.transform.shape.TransformingGraphics;

/**
 * Renders vertex labels when requested and supplied. Lightweight rendering does not draw vertex
 * labels.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class HeavyweightVertexLabelRenderer<V, E> implements Renderer.VertexLabel<V, E> {

  private Positioner positioner = new OutsidePositioner();

  public Component prepareRenderer(
      RenderContext<V, E> renderContext, Object value, boolean isSelected, V vertex) {
    return renderContext
        .getVertexLabelRenderer()
        .getVertexLabelRendererComponent(
            renderContext.getScreenDevice(),
            value,
            renderContext.getVertexFontFunction().apply(vertex),
            isSelected,
            vertex);
  }

  /**
   * Labels the specified vertex with the specified label. Uses the font specified by this
   * instance's <code>VertexFontFunction</code>. (If the font is unspecified, the existing font for
   * the graphics context is used.) If vertex label centering is active, the label is centered on
   * the position of the vertex; otherwise the label is offset slightly.
   */
  public void labelVertex(
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v, String label) {
    if (!renderContext.getVertexIncludePredicate().test(v)) {
      return;
    }
    org.jungrapht.visualization.layout.model.Point pt = layoutModel.apply(v);
    Point2D pt2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, pt.x, pt.y);

    double x = pt2d.getX();
    double y = pt2d.getY();

    Component component =
        prepareRenderer(
            renderContext, label, renderContext.getSelectedVertexState().isSelected(v), v);
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Dimension d = component.getPreferredSize();
    AffineTransform xform = AffineTransform.getTranslateInstance(x, y);

    Shape shape = renderContext.getVertexShapeFunction().apply(v);
    shape = xform.createTransformedShape(shape);
    if (renderContext.getGraphicsContext() instanceof TransformingGraphics) {
      BidirectionalTransformer transformer =
          ((TransformingGraphics) renderContext.getGraphicsContext()).getTransformer();
      if (transformer instanceof ShapeTransformer) {
        ShapeTransformer shapeTransformer = (ShapeTransformer) transformer;
        shape = shapeTransformer.transform(shape);
      }
    }
    Rectangle2D bounds = shape.getBounds2D();

    org.jungrapht.visualization.layout.model.Point p;
    Position position = renderContext.getVertexLabelPosition();
    if (position == Position.AUTO) {
      Dimension vvd = renderContext.getScreenDevice().getSize();
      if (vvd.width == 0 || vvd.height == 0) {
        vvd = renderContext.getScreenDevice().getPreferredSize();
      }
      p = getAnchorPoint(bounds, d, positioner.getPosition(x, y, vvd));
    } else {
      p = getAnchorPoint(bounds, d, position);
    }

    Paint fillPaint = renderContext.getVertexLabelDrawPaintFunction().apply(v);
    if (fillPaint != null) {
      Color oldPaint = component.getForeground();
      component.setForeground((Color) fillPaint);
      g.draw(
          component,
          renderContext.getRendererPane(),
          (int) p.x,
          (int) p.y,
          d.width,
          d.height,
          true);
      component.setForeground(oldPaint);
    } else {
      g.draw(
          component,
          renderContext.getRendererPane(),
          (int) p.x,
          (int) p.y,
          d.width,
          d.height,
          true);
    }
  }

  protected org.jungrapht.visualization.layout.model.Point getAnchorPoint(
      Rectangle2D vertexBounds, Dimension labelSize, Position position) {
    double x;
    double y;
    int offset = 5;
    switch (position) {
      case N:
        x = vertexBounds.getCenterX() - labelSize.width / 2.;
        y = vertexBounds.getMinY() - offset - labelSize.height;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case NE:
        x = vertexBounds.getMaxX() + offset;
        y = vertexBounds.getMinY() - offset - labelSize.height;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case E:
        x = vertexBounds.getMaxX() + offset;
        y = vertexBounds.getCenterY() - labelSize.height / 2.;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case SE:
        x = vertexBounds.getMaxX() + offset;
        y = vertexBounds.getMaxY() + offset;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case S:
        x = vertexBounds.getCenterX() - labelSize.width / 2.;
        y = vertexBounds.getMaxY() + offset;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case SW:
        x = vertexBounds.getMinX() - offset - labelSize.width;
        y = vertexBounds.getMaxY() + offset;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case W:
        x = vertexBounds.getMinX() - offset - labelSize.width;
        y = vertexBounds.getCenterY() - labelSize.height / 2.;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case NW:
        x = vertexBounds.getMinX() - offset - labelSize.width;
        y = vertexBounds.getMinY() - offset - labelSize.height;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case CNTR:
        x = vertexBounds.getCenterX() - labelSize.width / 2.;
        y = vertexBounds.getCenterY() - labelSize.height / 2.;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      default:
        return Point.ORIGIN;
    }
  }

  public static class InsidePositioner implements Positioner {
    public Position getPosition(double x, double y, Dimension d) {
      int cx = d.width / 2;
      int cy = d.height / 2;
      if (x > cx && y > cy) {
        return Position.NW;
      }
      if (x > cx && y < cy) {
        return Position.SW;
      }
      if (x < cx && y > cy) {
        return Position.NE;
      }
      return Position.SE;
    }
  }

  public static class OutsidePositioner implements Positioner {
    public Position getPosition(double x, double y, Dimension d) {
      int cx = d.width / 2;
      int cy = d.height / 2;
      if (x > cx && y > cy) {
        return Position.SE;
      }
      if (x > cx && y < cy) {
        return Position.NE;
      }
      if (x < cx && y > cy) {
        return Position.SW;
      }
      return Position.NW;
    }
  }
  /** @return the positioner */
  public Positioner getPositioner() {
    return positioner;
  }

  /** @param positioner the positioner to set */
  public void setPositioner(Positioner positioner) {
    this.positioner = positioner;
  }
}
