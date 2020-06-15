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
import javax.swing.Icon;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public class HeavyweightVertexRenderer<V, E> extends AbstractVertexRenderer<V, E>
    implements Renderer.Vertex<V, E> {

  private static final Logger log = LoggerFactory.getLogger(HeavyweightVertexRenderer.class);

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
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    int[] coords = new int[2];
    Shape shape = prepareFinalVertexShape(renderContext, visualizationModel, v, coords);

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
