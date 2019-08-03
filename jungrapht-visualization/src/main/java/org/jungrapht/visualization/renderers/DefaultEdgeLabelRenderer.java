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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.function.Predicate;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.Context;

public class DefaultEdgeLabelRenderer<V, E> implements Renderer.EdgeLabel<V, E> {

  public Component prepareRenderer(
      RenderContext<V, E> renderContext, Object value, boolean isSelected, E edge) {
    return renderContext
        .getEdgeLabelRenderer()
        .getEdgeLabelRendererComponent(
            renderContext.getScreenDevice(),
            value,
            renderContext.getEdgeFontFunction().apply(edge),
            isSelected,
            edge);
  }

  @Override
  public void labelEdge(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      E e,
      String label) {
    if (label == null || label.length() == 0) {
      return;
    }

    // don't draw edge if either incident vertex is not drawn
    V v1 = visualizationModel.getGraph().getEdgeSource(e);
    V v2 = visualizationModel.getGraph().getEdgeTarget(e);
    Predicate<V> vertexIncludePredicate = renderContext.getVertexIncludePredicate();
    if (!vertexIncludePredicate.test(v1) || !vertexIncludePredicate.test(v2)) {
      return;
    }

    Point p1 = visualizationModel.getLayoutModel().apply(v1);
    Point p2 = visualizationModel.getLayoutModel().apply(v2);
    Point2D p2d1 =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p1.x, p1.y));
    Point2D p2d2 =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p2.x, p2.y));
    float x1 = (float) p2d1.getX();
    float y1 = (float) p2d1.getY();
    float x2 = (float) p2d2.getX();
    float y2 = (float) p2d2.getY();

    GraphicsDecorator g = renderContext.getGraphicsContext();
    float distX = x2 - x1;
    float distY = y2 - y1;
    double totalLength = Math.sqrt(distX * distX + distY * distY);

    float closeness = renderContext.getEdgeLabelCloseness();

    int posX = (int) (x1 + (closeness) * distX);
    int posY = (int) (y1 + (closeness) * distY);

    int xDisplacement = (int) (renderContext.getLabelOffset() * (distY / totalLength));
    int yDisplacement = (int) (renderContext.getLabelOffset() * (-distX / totalLength));

    Component component =
        prepareRenderer(
            renderContext, label, renderContext.getSelectedEdgeState().isSelected(e), e);

    Dimension d = component.getPreferredSize();

    Shape edgeShape =
        renderContext
            .getEdgeShapeFunction()
            .apply(Context.getInstance(visualizationModel.getGraph(), e));

    double parallelOffset = 1;

    parallelOffset +=
        renderContext
            .getParallelEdgeIndexFunction()
            .getIndex(Context.getInstance(visualizationModel.getGraph(), e));

    parallelOffset *= d.height;
    if (edgeShape instanceof Ellipse2D) {
      parallelOffset += edgeShape.getBounds().getHeight();
      parallelOffset = -parallelOffset;
    }

    AffineTransform old = g.getTransform();
    AffineTransform xform = new AffineTransform(old);
    xform.translate(posX + xDisplacement, posY + yDisplacement);
    double dx = x2 - x1;
    double dy = y2 - y1;
    if (renderContext.getEdgeLabelRenderer().isRotateEdgeLabels()) {
      double theta = Math.atan2(dy, dx);
      if (dx < 0) {
        theta += Math.PI;
      }
      xform.rotate(theta);
    }
    if (dx < 0) {
      parallelOffset = -parallelOffset;
    }

    xform.translate(-d.width / 2, -(d.height / 2. - parallelOffset));
    g.setTransform(xform);
    g.draw(component, renderContext.getRendererPane(), 0, 0, d.width, d.height, true);

    g.setTransform(old);
  }
}
