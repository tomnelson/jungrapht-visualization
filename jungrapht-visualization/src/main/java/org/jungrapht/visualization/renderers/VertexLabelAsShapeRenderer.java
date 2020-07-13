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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;

/**
 * Renders Vertex Labels, but can also supply Shapes for vertices. This has the effect of making the
 * vertex label the actual vertex shape. The user will probably want to center the vertex label on
 * the vertex location.
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class VertexLabelAsShapeRenderer<V, E>
    implements Renderer.VertexLabel<V, E>, Function<V, Shape> {

  protected Map<V, Shape> shapes = new HashMap<>();
  protected final LayoutModel<V> layoutModel;
  protected final RenderContext<V, E> renderContext;

  public VertexLabelAsShapeRenderer(LayoutModel<V> layoutModel, RenderContext<V, E> rc) {
    this.layoutModel = layoutModel;
    this.renderContext = rc;
  }

  public Component prepareRenderer(
      RenderContext<V, E> rc, Object value, boolean isSelected, V vertex) {
    return rc.getVertexLabelRenderer()
        .getVertexLabelRendererComponent(
            rc.getScreenDevice(),
            value,
            rc.getVertexFontFunction().apply(vertex),
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
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Component component =
        prepareRenderer(
            renderContext, label, renderContext.getSelectedVertexState().isSelected(v), v);
    Dimension d = component.getPreferredSize();

    int h_offset = -d.width / 2;
    int v_offset = -d.height / 2;

    Point p = layoutModel.apply(v);
    Point2D p2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, p.x, p.y);

    int x = (int) p2d.getX();
    int y = (int) p2d.getY();

    boolean selected = renderContext.getSelectedVertexState().isSelected(v);
    if (selected) {
      component.setBackground(Color.pink);
    }
    g.draw(
        component,
        renderContext.getRendererPane(),
        x + h_offset,
        y + v_offset,
        d.width,
        d.height,
        true);

    Dimension size = component.getPreferredSize();
    Rectangle bounds =
        new Rectangle(-size.width / 2 - 2, -size.height / 2 - 2, size.width + 4, size.height);
    shapes.put(v, bounds);
  }

  public Shape apply(V v) {
    Component component =
        prepareRenderer(
            renderContext,
            renderContext.getVertexLabelFunction().apply(v),
            renderContext.getSelectedVertexState().isSelected(v),
            v);
    Dimension size = component.getPreferredSize();
    Rectangle bounds =
        new Rectangle(-size.width / 2 - 2, -size.height / 2 - 2, size.width + 4, size.height);
    return bounds;
  }

  public Renderer.VertexLabel.Position getPosition() {
    return Renderer.VertexLabel.Position.CNTR;
  }

  public Renderer.VertexLabel.Positioner getPositioner() {
    return (x, y, d) -> Position.CNTR;
  }

  public void setPosition(Renderer.VertexLabel.Position position) {
    // noop
  }

  public void setPositioner(Renderer.VertexLabel.Positioner positioner) {
    //noop
  }
}
