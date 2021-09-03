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

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.function.BiFunction;
import org.jgrapht.Graph;
import org.jungrapht.visualization.PropertyLoader;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.selection.SelectedState;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draws edges with complete features: Edge shapes may be curved, edge arrows, when specified, are
 * computed and drawn.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class HeavyweightEdgeRenderer<V, E> extends AbstractEdgeRenderer<V, E>
    implements Renderer.Edge<V, E> {

  private static Logger log = LoggerFactory.getLogger(HeavyweightEdgeRenderer.class);

  static {
    PropertyLoader.load();
  }

  protected EdgeArrowRenderingSupport<V, E> edgeArrowRenderingSupport =
      getPreferredEdgeArrowRenderingSupport();
  protected float edgeArrowScaleThreshold =
      Float.parseFloat(System.getProperty(PREFIX + "lightweightScaleThreshold", "0.3f"));

  private EdgeArrowRenderingSupport<V, E> getPreferredEdgeArrowRenderingSupport() {
    switch (System.getProperty(PREFIX + "edgeArrowPlacement", "ENDPOINTS")) {
      case "CENTER":
        return new CenterEdgeArrowRenderingSupport<>();
      case "ENDPOINTS":
      default:
        return new DefaultEdgeArrowRenderingSupport<>();
    }
  }

  /**
   * for the HeavyweightEdgeRenderer, we use whatever edge shape was provided by the
   * edgeShapeFunction.
   *
   * @param edgeShapeFunction the visualization's edge shape
   * @param edge the edge to render
   * @param graph the graph for the function context
   * @return the edgeShape specified by the edgeShapeFunction
   */
  @Override
  protected Shape getEdgeShape(
      BiFunction<Graph<V, E>, E, Shape> edgeShapeFunction, E edge, Graph<V, E> graph) {
    return edgeShapeFunction.apply(graph, edge);
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
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e) {

    Graphics2D g2d = renderContext.getGraphicsContext().getDelegate();
    Stroke savedStroke = g2d.getStroke();
    float widenessFactor = (float) (1.0 / g2d.getTransform().getScaleX());
    if (savedStroke instanceof BasicStroke) {
      BasicStroke basicStroke = (BasicStroke) savedStroke;
      Stroke widerStroke =
          new BasicStroke(
              basicStroke.getLineWidth() * widenessFactor,
              basicStroke.getEndCap(),
              basicStroke.getLineJoin(),
              basicStroke.getMiterLimit(),
              basicStroke.getDashArray(),
              basicStroke.getDashPhase());
      // if the transform scale is small, make the stroke wider so it is still visible
      g2d.setStroke(widerStroke);
    } else {
      g2d.setStroke(new BasicStroke(renderContext.getEdgeWidth() * widenessFactor));
    }

    int[] coords = new int[4];
    boolean[] loop = new boolean[1];
    Shape edgeShape = prepareFinalEdgeShape(renderContext, layoutModel, e, coords, loop);

    int x1 = coords[0];
    int y1 = coords[1];
    int x2 = coords[2];
    int y2 = coords[3];
    boolean isLoop = loop[0];

    GraphicsDecorator g = renderContext.getGraphicsContext();
    Graph<V, E> graph = layoutModel.getGraph();

    Paint oldPaint = g.getPaint();

    Paint fillPaint;
    Paint drawPaint;
    //    Stroke drawStroke;
    SelectedState<E> edgeSelectedState = renderContext.getSelectedEdgeState();
    if (edgeSelectedState.isSelected(e)) {
      fillPaint = renderContext.getSelectedEdgeFillPaintFunction().apply(e);
      drawPaint = renderContext.getSelectedEdgeDrawPaintFunction().apply(e);
      //      drawStroke = renderContext.getSelectedEdgeStrokeFunction().apply(e);
    } else {
      fillPaint = renderContext.getEdgeFillPaintFunction().apply(e);
      drawPaint = renderContext.getEdgeDrawPaintFunction().apply(e);
      //      drawStroke = renderContext.getEdgeStrokeFunction().apply(e);
    }
    // get Paints for filling and drawing
    // (filling is done first so that drawing and label use same Paint)
    //    Paint fill_paint = renderContext.getEdgeFillPaintFunction().apply(e);
    if (fillPaint != null) {
      g.setPaint(fillPaint);
      g.fill(edgeShape);
    }
    //    Paint drawPaint = renderContext.getEdgeDrawPaintFunction().apply(e);
    if (drawPaint != null) {
      g.setPaint(drawPaint);
      g.draw(edgeShape);
    }

    float scalex = (float) g.getTransform().getScaleX();
    float scaley = (float) g.getTransform().getScaleY();
    // see if arrows are too small to bother drawing
    if (scalex < edgeArrowScaleThreshold || scaley < edgeArrowScaleThreshold) {
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
