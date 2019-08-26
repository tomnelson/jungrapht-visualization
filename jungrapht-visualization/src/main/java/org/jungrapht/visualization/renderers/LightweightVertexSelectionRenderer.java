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
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightweightVertexSelectionRenderer<V, E> extends LightweightVertexRenderer<V, E>
    implements Renderer.Vertex<V, E> {

  private static final Logger log =
      LoggerFactory.getLogger(LightweightVertexSelectionRenderer.class);

  protected void paintShapeForVertex(RenderContext<V, E> renderContext, V v, Shape shape) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Stroke oldStroke = g.getStroke();
    Stroke stroke = new BasicStroke(4.f);
    g.setStroke(stroke);
    g.draw(shape);
    g.setStroke(oldStroke);
  }
}
