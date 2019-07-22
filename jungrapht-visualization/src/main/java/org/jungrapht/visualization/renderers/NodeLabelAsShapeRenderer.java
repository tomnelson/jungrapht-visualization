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
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;

/**
 * Renders Node Labels, but can also supply Shapes for nodes. This has the effect of making the node
 * label the actual node shape. The user will probably want to center the node label on the node
 * location.
 *
 * @author Tom Nelson
 * @param <N> the node type
 * @param <E> the edge type
 */
public class NodeLabelAsShapeRenderer<N, E>
    implements Renderer.NodeLabel<N, E>, Function<N, Shape> {

  protected Map<N, Shape> shapes = new HashMap<>();
  protected final LayoutModel<N> layoutModel;
  protected final RenderContext<N, ?> renderContext;

  public NodeLabelAsShapeRenderer(
      VisualizationModel<N, E> visualizationModel, RenderContext<N, ?> rc) {
    this.layoutModel = visualizationModel.getLayoutModel();
    this.renderContext = rc;
  }

  public Component prepareRenderer(
      RenderContext<N, ?> rc, Object value, boolean isSelected, N node) {
    return rc.getNodeLabelRenderer()
        .getNodeLabelRendererComponent(
            rc.getScreenDevice(), value, rc.getNodeFontFunction().apply(node), isSelected, node);
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
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Component component =
        prepareRenderer(
            renderContext, label, renderContext.getSelectedNodeState().isSelected(v), v);
    Dimension d = component.getPreferredSize();

    int h_offset = -d.width / 2;
    int v_offset = -d.height / 2;

    Point p = layoutModel.apply(v);
    Point2D p2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p.x, p.y));

    int x = (int) p2d.getX();
    int y = (int) p2d.getY();

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

  public Shape apply(N v) {
    Component component =
        prepareRenderer(
            renderContext,
            renderContext.getNodeLabelFunction().apply(v),
            renderContext.getSelectedNodeState().isSelected(v),
            v);
    Dimension size = component.getPreferredSize();
    Rectangle bounds =
        new Rectangle(-size.width / 2 - 2, -size.height / 2 - 2, size.width + 4, size.height);
    return bounds;
  }

  public Renderer.NodeLabel.Position getPosition() {
    return Renderer.NodeLabel.Position.CNTR;
  }

  public Renderer.NodeLabel.Positioner getPositioner() {
    return (x, y, d) -> Position.CNTR;
  }

  public void setPosition(Renderer.NodeLabel.Position position) {
    // noop
  }

  public void setPositioner(Renderer.NodeLabel.Positioner positioner) {
    //noop
  }
}
