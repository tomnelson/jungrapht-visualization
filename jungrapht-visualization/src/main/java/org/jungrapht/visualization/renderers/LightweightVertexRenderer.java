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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.function.Function;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LightweightVertexRenderer<V, E> extends AbstractVertexRenderer<V, E>
    implements Renderer.Vertex<V, E> {

  private static final Logger log = LoggerFactory.getLogger(LightweightVertexRenderer.class);

  protected Function<V, Shape> simpleVertexShapeFunction =
      n -> new Ellipse2D.Float(-10.f, -10.f, 20, 20);

  public void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
    this.simpleVertexShapeFunction = vertexShapeFunction;
  }

  public Function<V, Shape> getVertexShapeFunction() {
    return this.simpleVertexShapeFunction;
  }

  /**
   * Returns the vertex shape in layout coordinates. Uses the simpleVertexShapeFunction, unlike the
   * superclass
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
    Shape shape = simpleVertexShapeFunction.apply(v);
    Point p = visualizationModel.getLayoutModel().apply(v);
    // p is the vertex location in layout coordinates
    log.trace("prepared a shape for " + v + " to go at " + p);
    Point2D p2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p.x, p.y));
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
    return xform.createTransformedShape(shape);
  }

  /**
   * Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>. Paints only the shape,
   * unlike superclass
   *
   * @param v the vertex to be painted
   */
  @Override
  protected void paintIconForVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    int[] coords = new int[2];
    Shape shape = prepareFinalVertexShape(renderContext, visualizationModel, v, coords);

    paintShapeForVertex(renderContext, v, shape);
  }
}
