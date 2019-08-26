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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import org.jgrapht.Graph;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.LensTransformer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.jungrapht.visualization.transform.shape.TransformingGraphics;
import org.jungrapht.visualization.util.Context;

/**
 * uses a flatness argument to break edges into smaller segments. This produces a more detailed
 * transformation of the edge shape
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class ReshapingEdgeRenderer<V, E> extends HeavyweightEdgeRenderer<V, E>
    implements Renderer.Edge<V, E> {

  /**
   * Draws the edge <code>e</code>, whose endpoints are at <code>(x1,y1)</code> and <code>(x2,y2)
   * </code>, on the graphics context <code>g</code>. The <code>Shape</code> provided by the <code>
   * EdgeShapeFunction</code> instance is scaled in the x-direction so that its width is equal to
   * the distance between <code>(x1,y1)</code> and <code>(x2,y2)</code>.
   */
  protected void drawSimpleEdge(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {

    TransformingGraphics g = (TransformingGraphics) renderContext.getGraphicsContext();
    Graph<V, E> graph = visualizationModel.getGraph();
    V v1 = graph.getEdgeSource(e);
    V v2 = graph.getEdgeTarget(e);
    Point p1 = visualizationModel.getLayoutModel().apply(v1);
    Point p2 = visualizationModel.getLayoutModel().apply(v2);
    Point2D p12d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p1.x, p1.y));
    Point2D p22d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p2.x, p2.y));
    float x1 = (float) p12d.getX();
    float y1 = (float) p12d.getY();
    float x2 = (float) p22d.getX();
    float y2 = (float) p22d.getY();

    float flatness = 0;
    MutableTransformer transformer =
        renderContext.getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.VIEW);
    if (transformer instanceof LensTransformer) {
      LensTransformer ht = (LensTransformer) transformer;
      RectangularShape lensShape = ht.getLens().getLensShape();
      if (lensShape.contains(x1, y1) || lensShape.contains(x2, y2)) {
        flatness = .05f;
      }
    }

    boolean isLoop = v1.equals(v2);
    Shape s2 = renderContext.getVertexShapeFunction().apply(v2);
    Shape edgeShape = renderContext.getEdgeShapeFunction().apply(Context.getInstance(graph, e));

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

    Paint oldPaint = g.getPaint();

    // get Paints for filling and drawing
    // (filling is done first so that drawing and label use same Paint)
    Paint fill_paint = renderContext.getEdgeFillPaintFunction().apply(e);
    if (fill_paint != null) {
      g.setPaint(fill_paint);
      g.fill(edgeShape, flatness);
    }
    Paint draw_paint = renderContext.getEdgeDrawPaintFunction().apply(e);
    if (draw_paint != null) {
      g.setPaint(draw_paint);
      g.draw(edgeShape, flatness);
    }

    float scalex = (float) g.getTransform().getScaleX();
    float scaley = (float) g.getTransform().getScaleY();
    // see if arrows are too small to bother drawing
    if (scalex < .3 || scaley < .3) {
      return;
    }

    if (renderContext.renderEdgeArrow()) {

      Shape destVertexShape = renderContext.getVertexShapeFunction().apply(v2);

      AffineTransform xf = AffineTransform.getTranslateInstance(x2, y2);
      destVertexShape = xf.createTransformedShape(destVertexShape);

      AffineTransform at =
          edgeArrowRenderingSupport.getArrowTransform(
              renderContext, new GeneralPath(edgeShape), destVertexShape);
      if (at == null) {
        return;
      }
      Shape arrow = renderContext.getEdgeArrow();
      arrow = at.createTransformedShape(arrow);
      g.setPaint(renderContext.getArrowFillPaintFunction().apply(e));
      g.fill(arrow);
      g.setPaint(renderContext.getArrowDrawPaintFunction().apply(e));
      g.draw(arrow);

      if (!graph.getType().isDirected()) {
        Shape vertexShape = renderContext.getVertexShapeFunction().apply(v1);
        xf = AffineTransform.getTranslateInstance(x1, y1);
        vertexShape = xf.createTransformedShape(vertexShape);

        at =
            edgeArrowRenderingSupport.getReverseArrowTransform(
                renderContext, new GeneralPath(edgeShape), vertexShape, !isLoop);
        if (at == null) {
          return;
        }
        arrow = renderContext.getEdgeArrow();
        arrow = at.createTransformedShape(arrow);
        g.setPaint(renderContext.getArrowFillPaintFunction().apply(e));
        g.fill(arrow);
        g.setPaint(renderContext.getArrowDrawPaintFunction().apply(e));
        g.draw(arrow);
      }
    }
    // use existing paint for text if no draw paint specified
    if (draw_paint == null) {
      g.setPaint(oldPaint);
    }

    // restore old paint
    g.setPaint(oldPaint);
  }
}
