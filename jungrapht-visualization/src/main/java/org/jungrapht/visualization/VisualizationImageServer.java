/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.Graph;

/**
 * A class that could be used on the server side of a thin-client application. It creates the jung
 * visualization, then produces an image of it.
 *
 * @author tom
 * @param <V> the vertex type
 * @param <E> the edge type
 */
@SuppressWarnings("serial")
public class VisualizationImageServer<V, E> extends DefaultVisualizationServer<V, E> {

  public static class Builder<
          V, E, T extends VisualizationImageServer<V, E>, B extends Builder<V, E, T, B>>
      extends DefaultVisualizationServer.Builder<V, E, T, B> {

    protected Builder(Graph<V, E> graph) {
      super(graph);
    }

    public T build() {
      return (T) new VisualizationImageServer<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder(Graph<V, E> graph) {
    return new Builder(graph);
  }

  protected VisualizationImageServer(Builder<V, E, ?, ?> builder) {
    super(builder);
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    addNotify();
  }

  Map<RenderingHints.Key, Object> renderingHints = new HashMap<>();

  public Image getImage(Point2D center, Dimension d) {
    int width = getWidth();
    int height = getHeight();

    float scalex = (float) width / d.width;
    float scaley = (float) height / d.height;
    try {
      renderContext
          .getMultiLayerTransformer()
          .getTransformer(MultiLayerTransformer.Layer.VIEW)
          .scale(scalex, scaley, center);

      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = bi.createGraphics();
      graphics.setRenderingHints(renderingHints);
      paint(graphics);
      graphics.dispose();
      return bi;
    } finally {
      renderContext
          .getMultiLayerTransformer()
          .getTransformer(MultiLayerTransformer.Layer.VIEW)
          .setToIdentity();
    }
  }
}
