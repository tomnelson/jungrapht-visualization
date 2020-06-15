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
import java.awt.geom.Ellipse2D;
import java.util.function.Function;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
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

  protected Shape getVertexShape(RenderContext<V, E> renderContext, V vertex) {
    return simpleVertexShapeFunction.apply(vertex);
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
