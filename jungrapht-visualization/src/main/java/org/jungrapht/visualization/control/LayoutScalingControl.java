/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package org.jungrapht.visualization.control;

import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.transform.MutableTransformer;

/**
 * LayoutScalingControl applies a scaling transformation to the graph layout. The Vertices get
 * closer or farther apart, but do not themselves change size. ScalingGraphMouse uses
 * MouseWheelEvents to apply the scaling.
 *
 * @author Tom Nelson
 */
public class LayoutScalingControl implements ScalingControl {

  public static class Builder {
    double minScale = Double.parseDouble(System.getProperty(MIN_SCALE, "0.2"));
    double maxScale = Double.parseDouble(System.getProperty(MAX_SCALE, "5.0"));

    public Builder minScale(double minScale) {
      this.minScale = minScale;
      return this;
    }

    public Builder maxScale(double maxScale) {
      this.maxScale = maxScale;
      return this;
    }

    public ScalingControl build() {
      return new LayoutScalingControl(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  LayoutScalingControl(Builder builder) {
    this.minScale = builder.minScale;
    this.maxScale = builder.maxScale;
  }

  public LayoutScalingControl() {
    this(LayoutScalingControl.builder());
  }

  protected double minScale;

  protected double maxScale;

  /** zoom the display in or out, depending on the direction of the mouse wheel motion. */
  @Override
  public void scale(
      VisualizationServer<?, ?> vv, double horizontalAmount, double verticalAmount, Point2D from) {
    Point2D ivtfrom =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .inverseTransform(MultiLayerTransformer.Layer.VIEW, from);
    MutableTransformer modelTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.LAYOUT);

    double scaleX = modelTransformer.getScaleX();
    double scaleY = modelTransformer.getScaleY();

    if (scaleX > maxScale && horizontalAmount > 1.0) {
      return;
    }
    if (scaleX < minScale && horizontalAmount < 1.0) {
      return;
    }
    if (scaleY > maxScale && verticalAmount > 1.0) {
      return;
    }
    if (scaleY < minScale && verticalAmount < 1.0) {
      return;
    }

    modelTransformer.scale(horizontalAmount, verticalAmount, ivtfrom);
    vv.repaint();
  }
}
