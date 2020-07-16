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
import javax.swing.*;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public class HeavyweightVertexRenderer<V, E> extends AbstractVertexRenderer<V, E>
    implements Renderer.Vertex<V, E> {

  protected Shape getVertexShape(RenderContext<V, E> renderContext, V vertex) {
    return renderContext.getVertexShapeFunction().apply(vertex);
  }

  /**
   * Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>.
   *
   * @param v the vertex to be painted
   */
  @Override
  protected void paintIconForVertex(
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    int[] coords = new int[2];
    Shape shape = prepareFinalVertexShape(renderContext, layoutModel, v, coords);

    if (renderContext.getVertexIconFunction() != null) {
      Icon icon = renderContext.getVertexIconFunction().apply(v);
      if (icon != null) {
        g.draw(icon, renderContext.getScreenDevice(), shape, coords[0], coords[1]);
      } else {
        paintShapeForVertex(renderContext, v, shape);
      }
    } else {
      paintShapeForVertex(renderContext, v, shape);
    }
  }
}
