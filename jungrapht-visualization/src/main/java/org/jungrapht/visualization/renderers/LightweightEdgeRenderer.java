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

import static org.jungrapht.visualization.DefaultRenderContext.EDGE_WIDTH;

import java.awt.*;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.Context;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LightweightEdgeRenderer<V, E> extends AbstractEdgeRenderer<V, E>
    implements Renderer.Edge<V, E> {

  /**
   * For the LightweightEdgeRenderer, we only want the default 'line' edge shape when the edge is
   * not an articulated edge
   *
   * @param edgeShapeFunction the visualization's edge shape function
   * @param edge the edge to render
   * @param graph the graph (for the function context)
   * @return either a (lightweight) line edge or an articulated edge
   */
  @Override
  protected Shape getEdgeShape(
      Function<Context<Graph<V, E>, E>, Shape> edgeShapeFunction, E edge, Graph<V, E> graph) {
    if (edgeShapeFunction instanceof EdgeShape.ArticulatedLine) {
      return edgeShapeFunction.apply(Context.getInstance(graph, edge));
    } else {
      return EdgeShape.<V, E>line().apply(Context.getInstance(graph, edge));
    }
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
    float minStrokeWidth = Float.parseFloat(System.getProperty(EDGE_WIDTH, "1.0f"));
    // if the transform scale is small, make the stroke wider so it is still visible
    g2d.setStroke(
        new BasicStroke(Math.max(minStrokeWidth, (int) (1.0 / g2d.getTransform().getScaleX()))));

    int[] coords = new int[4];
    boolean[] loop = new boolean[1];
    Shape edgeShape = prepareFinalEdgeShape(renderContext, layoutModel, e, coords, loop);

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
      // set the stroke to something proportional to the scale
      g.draw(edgeShape);
    }
    // restore old paint
    g.setPaint(oldPaint);
    g2d.setStroke(savedStroke);
  }

  public EdgeArrowRenderingSupport<V, E> getEdgeArrowRenderingSupport() {
    return null;
  }

  public void setEdgeArrowRenderingSupport(
      EdgeArrowRenderingSupport<V, E> edgeArrowRenderingSupport) {}
}
