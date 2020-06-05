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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.Graph;
import org.jungrapht.visualization.transform.MutableAffineTransformer;

/**
 * A class that could be used on the server side of a thin-client application. It creates the graph
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
    setSize(builder.viewSize);
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    addNotify();
  }

  Map<RenderingHints.Key, Object> renderingHints = new HashMap<>();

  //  public Image getFullImage(double scale) {
  //    Dimension layoutSize = getVisualizationModel().getLayoutSize();
  //    // make this view the same size as the layout size
  //    setSize(new Dimension((int) (layoutSize.width / scale), (int) (layoutSize.height / scale)));
  //
  //    return getImage(new Point2D.Double(), getSize());
  // save off the original view transform
  //    AffineTransform originalViewTransform = new AffineTransform(
  //            renderContext.getMultiLayerTransformer()
  //                    .getTransformer(MultiLayerTransformer.Layer.VIEW).getTransform()
  //    );
  //    AffineTransform originalLayoutTransform = new AffineTransform(
  //            renderContext.getMultiLayerTransformer()
  //                    .getTransformer(MultiLayerTransformer.Layer.LAYOUT).getTransform()
  //    );
  //    // remove any view scaling so that the vertices/edges are drawn at non-scaled size
  //    renderContext.getMultiLayerTransformer()
  //            .getTransformer(MultiLayerTransformer.Layer.VIEW).setToIdentity();
  //    try {
  //      BufferedImage bi = new BufferedImage(layoutSize.width, layoutSize.height, BufferedImage.TYPE_INT_RGB);
  //      Graphics2D graphics = bi.createGraphics();
  //      graphics.setRenderingHints(renderingHints);
  //      paint(graphics);
  //      graphics.dispose();
  //      return bi;
  //    } finally {
  //      renderContext
  //              .getMultiLayerTransformer()
  //              .setTransformer(MultiLayerTransformer.Layer.VIEW,
  //                      new MutableAffineTransformer(originalViewTransform));
  ////      renderContext
  ////              .getMultiLayerTransformer()
  ////              .setTransformer(MultiLayerTransformer.Layer.LAYOUT,
  ////                      new MutableAffineTransformer(originalLayoutTransform));
  //
  //    }
  //  }

  public Image getFullImage() {
    Dimension layoutSize = getVisualizationModel().getLayoutSize();
    // make this view the same size as the layout size
    setSize(layoutSize);
    // save off the original View transform
    AffineTransform originalViewTransform =
        new AffineTransform(
            renderContext
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.VIEW)
                .getTransform());
    // save off original Layout transform
    AffineTransform originalLayoutTransform =
        new AffineTransform(
            renderContext
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                .getTransform());
    // remove any scaling so that the vertices/edges are drawn at non-scaled size
    renderContext.getMultiLayerTransformer().setToIdentity();

    try {
      BufferedImage bi =
          new BufferedImage(layoutSize.width, layoutSize.height, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = bi.createGraphics();
      graphics.setRenderingHints(renderingHints);
      paint(graphics);
      graphics.dispose();
      return bi;
    } finally {
      renderContext
          .getMultiLayerTransformer()
          .setTransformer(
              MultiLayerTransformer.Layer.VIEW,
              new MutableAffineTransformer(originalViewTransform));
      renderContext
          .getMultiLayerTransformer()
          .setTransformer(
              MultiLayerTransformer.Layer.LAYOUT,
              new MutableAffineTransformer(originalLayoutTransform));
    }
  }

  //  public Image getImage(Point2D center, Dimension d) {
  //    return getImage(center, d, 1.0);
  //  }
  //
  //  public Image getImage(Point2D center, Dimension d, double scale) {
  //    int width = (int) this.getPreferredSize().getWidth();
  //    int height = (int) this.getPreferredSize().getHeight();
  //
  //    float scalex = (float) width / d.width;
  //    float scaley = (float) height / d.height;
  //    AffineTransform originalViewTransform =
  //        new AffineTransform(
  //            renderContext
  //                .getMultiLayerTransformer()
  //                .getTransformer(MultiLayerTransformer.Layer.VIEW)
  //                .getTransform());
  //    AffineTransform originalLayoutTransform =
  //        new AffineTransform(
  //            renderContext
  //                .getMultiLayerTransformer()
  //                .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
  //                .getTransform());
  //    try {
  //      renderContext
  //          .getMultiLayerTransformer()
  //          .getTransformer(MultiLayerTransformer.Layer.VIEW)
  //          .scale(scalex, scaley, center);
  //
  //      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  //      Graphics2D graphics = bi.createGraphics();
  //      //      graphics.setTransform(AffineTransform.getScaleInstance(scale, scale));
  //      graphics.setRenderingHints(renderingHints);
  //      paint(graphics);
  //      graphics.dispose();
  //      return bi;
  //    } finally {
  //      renderContext
  //          .getMultiLayerTransformer()
  //          .setTransformer(
  //              MultiLayerTransformer.Layer.VIEW,
  //              new MutableAffineTransformer(originalViewTransform));
  //      renderContext
  //          .getMultiLayerTransformer()
  //          .setTransformer(
  //              MultiLayerTransformer.Layer.LAYOUT,
  //              new MutableAffineTransformer(originalLayoutTransform));
  //    }
  //  }
}
