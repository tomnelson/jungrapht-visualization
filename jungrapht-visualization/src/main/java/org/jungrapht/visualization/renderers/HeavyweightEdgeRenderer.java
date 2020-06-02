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

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.*;
import java.awt.geom.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.ParallelEdgeShapeFunction;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.Context;
import org.jungrapht.visualization.util.EdgeIndexFunction;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public class HeavyweightEdgeRenderer<V, E> extends AbstractEdgeRenderer<V, E>
    implements Renderer.Edge<V, E> {

  protected EdgeArrowRenderingSupport<V, E> edgeArrowRenderingSupport =
      getPreferredEdgeArrowRenderingSupport();

  private EdgeArrowRenderingSupport<V, E> getPreferredEdgeArrowRenderingSupport() {
    switch (System.getProperty(PREFIX + "edgeArrowPlacement", "ENDPOINTS")) {
      case "CENTER":
        return new CenterEdgeArrowRenderingSupport<>();
      case "ENDPOINTS":
      default:
        return new DefaultEdgeArrowRenderingSupport<>();
    }
  }

  protected Shape prepareFinalEdgeShape(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      E e,
      int[] coords,
      boolean[] loop) {
    V source = visualizationModel.getGraph().getEdgeSource(e);
    V target = visualizationModel.getGraph().getEdgeTarget(e);

    Point sourcePoint = visualizationModel.getLayoutModel().apply(source);
    Point targetPoint = visualizationModel.getLayoutModel().apply(target);
    Point2D sourcePoint2D =
        renderContext
            .getMultiLayerTransformer()
            .transform(
                MultiLayerTransformer.Layer.LAYOUT,
                new Point2D.Double(sourcePoint.x, sourcePoint.y));
    Point2D targetPoint2D =
        renderContext
            .getMultiLayerTransformer()
            .transform(
                MultiLayerTransformer.Layer.LAYOUT,
                new Point2D.Double(targetPoint.x, targetPoint.y));
    float sourcePoint2DX = (float) sourcePoint2D.getX();
    float sourcePoint2DY = (float) sourcePoint2D.getY();
    float targetPoint2DX = (float) targetPoint2D.getX();
    float targetPoint2DY = (float) targetPoint2D.getY();
    coords[0] = (int) sourcePoint2DX;
    coords[1] = (int) sourcePoint2DY;
    coords[2] = (int) targetPoint2DX;
    coords[3] = (int) targetPoint2DY;

    boolean isLoop = loop[0] = source.equals(target);
    Shape targetShape = renderContext.getVertexShapeFunction().apply(target);
    Shape edgeShape =
        renderContext
            .getEdgeShapeFunction()
            .apply(Context.getInstance(visualizationModel.getGraph(), e));

    AffineTransform xform = AffineTransform.getTranslateInstance(sourcePoint2DX, sourcePoint2DY);

    if (isLoop) {
      // this is a self-loop. scale it is larger than the vertex
      // it decorates and translate it so that its nadir is
      // at the center of the vertex.
      Rectangle2D targetShapeBounds2D = targetShape.getBounds2D();
      xform.scale(targetShapeBounds2D.getWidth(), targetShapeBounds2D.getHeight());
      xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
    } else if (renderContext.getEdgeShapeFunction() instanceof EdgeShape.Orthogonal) {
      float dx = targetPoint2DX - sourcePoint2DX;
      float dy = targetPoint2DY - sourcePoint2DY;
      int index = 0;
      if (renderContext.getEdgeShapeFunction() instanceof ParallelEdgeShapeFunction) {
        EdgeIndexFunction<V, E> peif =
            ((ParallelEdgeShapeFunction<V, E>) renderContext.getEdgeShapeFunction())
                .getEdgeIndexFunction();
        index = peif.apply(Context.getInstance(visualizationModel.getGraph(), e));
        index *= 20;
      }
      GeneralPath gp = new GeneralPath();
      gp.moveTo(0, 0); // the xform will do the translation to x1,y1
      if (sourcePoint2DX > targetPoint2DX) {
        if (sourcePoint2DY > targetPoint2DY) {
          gp.lineTo(0, index);
          gp.lineTo(dx - index, index);
          gp.lineTo(dx - index, dy);
          gp.lineTo(dx, dy);
        } else {
          gp.lineTo(0, -index);
          gp.lineTo(dx - index, -index);
          gp.lineTo(dx - index, dy);
          gp.lineTo(dx, dy);
        }

      } else {
        if (sourcePoint2DY > targetPoint2DY) {
          gp.lineTo(0, index);
          gp.lineTo(dx + index, index);
          gp.lineTo(dx + index, dy);
          gp.lineTo(dx, dy);

        } else {
          gp.lineTo(0, -index);
          gp.lineTo(dx + index, -index);
          gp.lineTo(dx + index, dy);
          gp.lineTo(dx, dy);
        }
      }

      edgeShape = gp;

    } else {
      // this is a normal edge. Rotate it to the angle between
      // vertex endpoints, then scale it to the distance between
      // the vertices
      float dx = targetPoint2DX - sourcePoint2DX;
      float dy = targetPoint2DY - sourcePoint2DY;
      float thetaRadians = (float) Math.atan2(dy, dx);
      xform.rotate(thetaRadians);
      double dist = Math.sqrt(dx * dx + dy * dy);
      if (edgeShape instanceof Path2D) {
        xform.scale(dist, dist);
      } else {
        xform.scale(dist, 1.0);
      }
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

    Graphics2D g2d = renderContext.getGraphicsContext().getDelegate();
    Stroke savedStroke = g2d.getStroke();
    float savedStrokeWidth = renderContext.getEdgeWidth();
    // if the transform scale is small, make the stroke wider so it is still visible
    g2d.setStroke(
        new BasicStroke(Math.max(savedStrokeWidth, (int) (1.0 / g2d.getTransform().getScaleX()))));

    int[] coords = new int[4];
    boolean[] loop = new boolean[1];
    Shape edgeShape = prepareFinalEdgeShape(renderContext, visualizationModel, e, coords, loop);

    int x1 = coords[0];
    int y1 = coords[1];
    int x2 = coords[2];
    int y2 = coords[3];
    boolean isLoop = loop[0];

    GraphicsDecorator g = renderContext.getGraphicsContext();
    Graph<V, E> graph = visualizationModel.getGraph();

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

    float scalex = (float) g.getTransform().getScaleX();
    float scaley = (float) g.getTransform().getScaleY();
    // see if arrows are too small to bother drawing
    if (scalex < .3 || scaley < .3) {
      return;
    }

    if (renderContext.renderEdgeArrow()) {

      Stroke new_stroke = renderContext.getEdgeArrowStrokeFunction().apply(e);
      Stroke old_stroke = g.getStroke();
      if (new_stroke != null) {
        g.setStroke(new_stroke);
      }

      Shape destVertexShape = renderContext.getVertexShapeFunction().apply(graph.getEdgeTarget(e));

      AffineTransform xf = AffineTransform.getTranslateInstance(x2, y2);
      destVertexShape = xf.createTransformedShape(destVertexShape);

      AffineTransform at =
          edgeArrowRenderingSupport.getArrowTransform(renderContext, edgeShape, destVertexShape);
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
        Shape vertexShape = renderContext.getVertexShapeFunction().apply(graph.getEdgeSource(e));
        xf = AffineTransform.getTranslateInstance(x1, y1);
        vertexShape = xf.createTransformedShape(vertexShape);
        at =
            edgeArrowRenderingSupport.getReverseArrowTransform(
                renderContext, edgeShape, vertexShape, !isLoop);
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
      // restore paint and stroke
      if (new_stroke != null) {
        g.setStroke(old_stroke);
      }
    }

    // restore old paint
    g.setPaint(oldPaint);
    g2d.setStroke(savedStroke);
  }

  public EdgeArrowRenderingSupport<V, E> getEdgeArrowRenderingSupport() {
    return edgeArrowRenderingSupport;
  }

  public void setEdgeArrowRenderingSupport(
      EdgeArrowRenderingSupport<V, E> edgeArrowRenderingSupport) {
    this.edgeArrowRenderingSupport = edgeArrowRenderingSupport;
  }
}
