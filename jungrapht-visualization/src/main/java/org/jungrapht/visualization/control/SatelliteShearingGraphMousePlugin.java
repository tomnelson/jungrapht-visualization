/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 15, 2005
 */

package org.jungrapht.visualization.control;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.SatelliteVisualizationViewer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.transform.MutableTransformer;

/**
 * Overrides ShearingGraphMousePlugin so that mouse events in the satellite view cause shearing of
 * the main view
 *
 * @see ShearingGraphMousePlugin
 * @author Tom Nelson
 */
public class SatelliteShearingGraphMousePlugin extends ShearingGraphMousePlugin {

  public SatelliteShearingGraphMousePlugin() {
    super();
  }

  public SatelliteShearingGraphMousePlugin(int modifiers) {
    super(modifiers);
  }

  /** overridden to shear the main view */
  public void mouseDragged(MouseEvent e) {
    if (down == null) {
      return;
    }
    VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
    boolean accepted = e.getModifiersEx() == shearingMask;
    if (accepted) {
      if (vv instanceof SatelliteVisualizationViewer) {
        VisualizationViewer<?, ?> vvMaster = ((SatelliteVisualizationViewer<?, ?>) vv).getMaster();

        MutableTransformer modelTransformerMaster =
            vvMaster
                .getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT);

        vv.setCursor(cursor);
        Point2D q = down;
        Point2D p = e.getPoint();
        double dx = p.getX() - q.getX();
        double dy = p.getY() - q.getY();

        Dimension d = vv.getSize();
        double shx = 2.f * dx / d.height;
        double shy = 2.f * dy / d.width;
        // I want to compute shear based on the view coordinates of the
        // lens center in the satellite view.
        // translate the master view center to layout coords, then translate
        // that point to the satellite view's view coordinate system....
        Point2D center =
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .transform(
                    vvMaster
                        .getRenderContext()
                        .getMultiLayerTransformer()
                        .inverseTransform(vvMaster.getCenter()));
        if (p.getX() < center.getX()) {
          shy = -shy;
        }
        if (p.getY() < center.getY()) {
          shx = -shx;
        }
        modelTransformerMaster.shear(-shx, -shy, vvMaster.getCenter());

        down.x = e.getX();
        down.y = e.getY();
      }
      e.consume();
    }
  }
}
