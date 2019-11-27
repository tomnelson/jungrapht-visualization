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
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.decorators.ArticulatedEdgeShapeFunction;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public class HeayweightEdgeLabelRenderer<V, E> implements Renderer.EdgeLabel<V, E> {

  private static final Logger log = LoggerFactory.getLogger(HeayweightEdgeLabelRenderer.class);

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

    // if this in an articulated edge, use only the first segment for label positioning
    // get the edge shape, move it into position (layout coords), get the first articulation point
    // and use that for p2
    Function<Context<Graph<V, E>, E>, Shape> edgeShapeFunction =
        renderContext.getEdgeShapeFunction();
    if (edgeShapeFunction instanceof ArticulatedEdgeShapeFunction) {
      ArticulatedEdgeShapeFunction<V, E> articulatedEdgeShapeFunction =
          (ArticulatedEdgeShapeFunction<V, E>) edgeShapeFunction;
      Function<E, List<Point>> edgeArticulationFunction =
          articulatedEdgeShapeFunction.getEdgeArticulationFunction();
      if (edgeArticulationFunction.apply(e).size() > 0) {

        Shape edgeShape = getArticulatedEdgeShape(renderContext, visualizationModel, e);

        if (edgeShape instanceof Path2D) {

          float[] seg = new float[6];
          Path2D path = (Path2D) edgeShape;
          // get the first bend
          for (PathIterator i = path.getPathIterator(null, 1); !i.isDone(); i.next()) {
            int ret = i.currentSegment(seg);
            if (ret == PathIterator.SEG_LINETO) {
              log.trace("p2 was {} now {}", p2, Point.of(seg[0], seg[1]));
              p2 = Point.of(seg[0], seg[1]);
              break;
            }
          }
        }
      }
    }

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
            .apply(Context.getInstance(visualizationModel.getGraph(), e));

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

  /**
   * used for articulated edges. returns the final edge shape in layout space
   *
   * @param renderContext
   * @param visualizationModel
   * @param e
   * @return
   */
  private Shape getArticulatedEdgeShape(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {
    V v1 = visualizationModel.getGraph().getEdgeSource(e);
    V v2 = visualizationModel.getGraph().getEdgeTarget(e);

    org.jungrapht.visualization.layout.model.Point p1 =
        visualizationModel.getLayoutModel().apply(v1);
    Point p2 = visualizationModel.getLayoutModel().apply(v2);
    float x1 = (float) p1.x;
    float y1 = (float) p1.y;
    float x2 = (float) p2.x;
    float y2 = (float) p2.y;
    Shape s2 = renderContext.getVertexShapeFunction().apply(v2);
    // use LINE or ArticulatedLine for lightweight edges
    Shape edgeShape;
    Function<Context<Graph<V, E>, E>, Shape> edgeShapeFunction =
        renderContext.getEdgeShapeFunction();
    if (edgeShapeFunction instanceof EdgeShape.ArticulatedLine) {
      edgeShape =
          renderContext
              .getEdgeShapeFunction()
              .apply(Context.getInstance(visualizationModel.getGraph(), e));
    } else {
      edgeShape = EdgeShape.line().apply(Context.getInstance(visualizationModel.getGraph(), e));
    }

    AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

    // this is a normal edge. Rotate it to the angle between
    // vertex endpoints, then scale it to the distance between
    // the vertices
    float dx = x2 - x1;
    float dy = y2 - y1;
    float thetaRadians = (float) Math.atan2(dy, dx);
    xform.rotate(thetaRadians);
    float dist = (float) Math.sqrt(dx * dx + dy * dy);
    if (edgeShape instanceof Path2D) {
      xform.scale(dist, dist);
    } else {
      xform.scale(dist, 1.0);
    }
    return xform.createTransformedShape(edgeShape);
  }
}
