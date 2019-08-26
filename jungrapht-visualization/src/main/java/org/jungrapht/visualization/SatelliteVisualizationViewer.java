/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 15, 2005
 */

package org.jungrapht.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import org.jungrapht.visualization.transform.shape.ShapeTransformer;

/**
 * A VisualizationViewer that can act as a satellite view for another (master) VisualizationViewer.
 * In this view, the full graph is always visible and all mouse actions affect the graph in the
 * master view.
 *
 * <p>A rectangular shape in the satellite view shows the visible bounds of the master view.
 *
 * @author Tom Nelson
 */
public interface SatelliteVisualizationViewer<V, E> extends VisualizationViewer<V, E> {

  class Builder<
          V, E, T extends DefaultSatelliteVisualizationViewer<V, E>, B extends Builder<V, E, T, B>>
      extends VisualizationViewer.Builder<V, E, T, B> {

    protected VisualizationViewer<V, E> master;

    protected Builder(VisualizationViewer<V, E> master) {
      super(master.getVisualizationModel());
      this.master = master;
    }

    private Dimension getViewSize() {
      return viewSize;
    }

    public T build() {
      return (T) new DefaultSatelliteVisualizationViewer<>(this);
    }
  }

  static <V, E> Builder<V, E, ?, ?> builder(VisualizationViewer<V, E> master) {
    return new Builder(master);
  }

  /** @return Returns the master. */
  VisualizationViewer<V, E> getMaster();

  /**
   * A four-sided shape that represents the visible part of the master view and is drawn in the
   * satellite view
   *
   * @author Tom Nelson
   */
  class ViewLens<V, E> implements Paintable {

    VisualizationViewer<V, E> master;
    VisualizationViewer<V, E> vv;

    public ViewLens(VisualizationViewer<V, E> vv, VisualizationViewer<V, E> master) {
      this.vv = vv;
      this.master = master;
    }

    public void paint(Graphics g) {
      ShapeTransformer masterViewTransformer =
          master
              .getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.VIEW);
      ShapeTransformer masterLayoutTransformer =
          master
              .getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
      ShapeTransformer vvLayoutTransformer =
          vv.getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.LAYOUT);

      Shape lens = master.getBounds();
      lens = masterViewTransformer.inverseTransform(lens);
      lens = masterLayoutTransformer.inverseTransform(lens);
      lens = vvLayoutTransformer.transform(lens);
      Graphics2D g2d = (Graphics2D) g;
      Color old = g.getColor();
      Color lensColor = master.getBackground();
      Color darker =
          new Color(
              Math.max((int) (lensColor.getRed() * .95), 0),
              Math.max((int) (lensColor.getGreen() * .95), 0),
              Math.max((int) (lensColor.getBlue() * .95), 0),
              lensColor.getAlpha());
      vv.setBackground(darker);
      g.setColor(lensColor);
      g2d.fill(lens);
      g.setColor(Color.gray);
      g2d.draw(lens);
      g.setColor(old);
    }

    public boolean useTransform() {
      return true;
    }
  }
}
