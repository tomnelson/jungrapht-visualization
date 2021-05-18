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

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.function.Function;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * Renders graph vertices with optimizations for performance.
 * Vertex Icon images are not drawn, instead simple shapes are used.
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LightweightVertexRenderer<V, E> extends AbstractVertexRenderer<V, E>
    implements Renderer.Vertex<V, E> {

  protected Function<V, Shape> simpleVertexShapeFunction =
      n -> new Ellipse2D.Float(-10.f, -10.f, 20, 20);

  public void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
    this.simpleVertexShapeFunction = vertexShapeFunction;
  }

  public Function<V, Shape> getVertexShapeFunction() {
    return this.simpleVertexShapeFunction;
  }

  protected Shape getVertexShape(RenderContext<V, E> renderContext, V vertex) {
    return renderContext.getVertexShapeFunction().apply(vertex);
  }

  /**
   * Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>. Paints only the shape,
   * unlike HeavyweightVertexRenderer, which will paint an icon if supplied
   *
   * @param v the vertex to be painted
   */
  @Override
  protected void paintIconForVertex(
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v) {
    int[] coords = new int[2];
    Shape shape = prepareFinalVertexShape(renderContext, layoutModel, v, coords);

    paintShapeForVertex(renderContext, v, shape);
  }
}
