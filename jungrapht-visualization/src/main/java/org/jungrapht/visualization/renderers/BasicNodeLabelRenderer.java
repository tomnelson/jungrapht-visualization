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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.BidirectionalTransformer;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.transform.shape.ShapeTransformer;
import org.jungrapht.visualization.transform.shape.TransformingGraphics;

public class BasicNodeLabelRenderer<N, E> implements Renderer.NodeLabel<N, E> {

  //  protected Position position = Position.SE;
  private Positioner positioner = new OutsidePositioner();

  public Component prepareRenderer(
      RenderContext<N, E> renderContext, Object value, boolean isSelected, N node) {
    return renderContext
        .getNodeLabelRenderer()
        .getNodeLabelRendererComponent(
            renderContext.getScreenDevice(),
            value,
            renderContext.getNodeFontFunction().apply(node),
            isSelected,
            node);
  }

  /**
   * Labels the specified node with the specified label. Uses the font specified by this instance's
   * <code>NodeFontFunction</code>. (If the font is unspecified, the existing font for the graphics
   * context is used.) If node label centering is active, the label is centered on the position of
   * the node; otherwise the label is offset slightly.
   */
  public void labelNode(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E> visualizationModel,
      N v,
      String label) {
    if (!renderContext.getNodeIncludePredicate().test(v)) {
      return;
    }
    LayoutModel<N> layoutModel = visualizationModel.getLayoutModel();
    org.jungrapht.visualization.layout.model.Point pt = layoutModel.apply(v);
    Point2D pt2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(pt.x, pt.y));

    float x = (float) pt2d.getX();
    float y = (float) pt2d.getY();

    Component component =
        prepareRenderer(
            renderContext, label, renderContext.getSelectedNodeState().isSelected(v), v);
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Dimension d = component.getPreferredSize();
    AffineTransform xform = AffineTransform.getTranslateInstance(x, y);

    Shape shape = renderContext.getNodeShapeFunction().apply(v);
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
    Position position = renderContext.getNodeLabelPosition();
    if (position == Position.AUTO) {
      Dimension vvd = renderContext.getScreenDevice().getSize();
      if (vvd.width == 0 || vvd.height == 0) {
        vvd = renderContext.getScreenDevice().getPreferredSize();
      }
      p = getAnchorPoint(bounds, d, positioner.getPosition(x, y, vvd));
    } else {
      p = getAnchorPoint(bounds, d, position);
    }

    Paint fillPaint = renderContext.getNodeLabelDrawPaintFunction().apply(v);
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
      Rectangle2D nodeBounds, Dimension labelSize, Position position) {
    double x;
    double y;
    int offset = 5;
    switch (position) {
      case N:
        x = nodeBounds.getCenterX() - labelSize.width / 2.;
        y = nodeBounds.getMinY() - offset - labelSize.height;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case NE:
        x = nodeBounds.getMaxX() + offset;
        y = nodeBounds.getMinY() - offset - labelSize.height;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case E:
        x = nodeBounds.getMaxX() + offset;
        y = nodeBounds.getCenterY() - labelSize.height / 2.;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case SE:
        x = nodeBounds.getMaxX() + offset;
        y = nodeBounds.getMaxY() + offset;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case S:
        x = nodeBounds.getCenterX() - labelSize.width / 2.;
        y = nodeBounds.getMaxY() + offset;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case SW:
        x = nodeBounds.getMinX() - offset - labelSize.width;
        y = nodeBounds.getMaxY() + offset;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case W:
        x = nodeBounds.getMinX() - offset - labelSize.width;
        y = nodeBounds.getCenterY() - labelSize.height / 2.;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case NW:
        x = nodeBounds.getMinX() - offset - labelSize.width;
        y = nodeBounds.getMinY() - offset - labelSize.height;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      case CNTR:
        x = nodeBounds.getCenterX() - labelSize.width / 2.;
        y = nodeBounds.getCenterY() - labelSize.height / 2.;
        return org.jungrapht.visualization.layout.model.Point.of(x, y);

      default:
        return Point.ORIGIN;
    }
  }

  public static class InsidePositioner implements Positioner {
    public Position getPosition(float x, float y, Dimension d) {
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
    public Position getPosition(float x, float y, Dimension d) {
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
