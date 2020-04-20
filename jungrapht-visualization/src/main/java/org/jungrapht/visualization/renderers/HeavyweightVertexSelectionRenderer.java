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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public class HeavyweightVertexSelectionRenderer<V, E> extends HeavyweightVertexRenderer<V, E>
    implements Renderer.Vertex<V, E> {

  private static final Logger log =
      LoggerFactory.getLogger(HeavyweightVertexSelectionRenderer.class);

  private VisualizationServer<V, E> visualizationServer;

  public HeavyweightVertexSelectionRenderer(VisualizationServer<V, E> visualizationServer) {
    this.visualizationServer = visualizationServer;
  }

  /**
   * Returns the vertex shape in layout coordinates.
   *
   * @param v the vertex whose shape is to be returned
   * @param coords the x and y view coordinates
   * @return the vertex shape in view coordinates
   */
  protected Shape prepareFinalVertexShape(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      V v,
      int[] coords) {

    // get the shape to be rendered
    Shape shape;
    Renderer<V, E> renderer = visualizationServer.getRenderer();
    if (renderer instanceof ModalRenderer) {
      ModalRenderer modalRenderer = (ModalRenderer) renderer;
      Renderer.Vertex vertexRenderer = modalRenderer.getVertexRenderer();
      if (vertexRenderer instanceof LightweightVertexSelectionRenderer) {
        LightweightVertexSelectionRenderer<V, E> lightweightVertexRenderer =
            (LightweightVertexSelectionRenderer) vertexRenderer;
        shape = lightweightVertexRenderer.getVertexShapeFunction().apply(v);
      } else {
        shape = renderContext.getVertexShapeFunction().apply(v);
      }

    } else {
      shape = renderContext.getVertexShapeFunction().apply(v);
    }
    log.trace("selection shape bounds: {}", shape.getBounds());
    Point p = visualizationModel.getLayoutModel().apply(v);
    // p is the vertex location in layout coordinates

    Point2D p2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, p.x, p.y);
    // now p is in view coordinates, ready to be further transformed by any transform in the
    // graphics context
    float x = (float) p2d.getX();
    float y = (float) p2d.getY();
    coords[0] = (int) x;
    coords[1] = (int) y;
    // create a transform that translates to the location of
    // the vertex to be rendered
    AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
    // transform the vertex shape with xtransform
    shape = xform.createTransformedShape(shape);
    return shape;
  }

  protected void paintIconForVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    int[] coords = new int[2];
    Shape shape = prepareFinalVertexShape(renderContext, visualizationModel, v, coords);
    log.trace("shape bounds: {}", shape.getBounds());
    paintShapeForVertex(renderContext, v, shape);
  }

  protected void paintShapeForVertex(RenderContext<V, E> renderContext, V v, Shape shape) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Stroke oldStroke = g.getStroke();
    Stroke stroke = new BasicStroke(4.f);
    g.setStroke(stroke);
    g.draw(shape);
    g.setStroke(oldStroke);
  }
}
