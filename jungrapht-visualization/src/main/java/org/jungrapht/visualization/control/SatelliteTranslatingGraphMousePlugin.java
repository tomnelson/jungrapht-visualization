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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overrides TranslatingGraphMousePlugin so that mouse events in the satellite view cause
 * translating of the main view
 *
 * @see TranslatingGraphMousePlugin
 * @author Tom Nelson
 */
public class SatelliteTranslatingGraphMousePlugin extends TranslatingGraphMousePlugin {

  private static final Logger log =
      LoggerFactory.getLogger(SatelliteTranslatingGraphMousePlugin.class);

  public SatelliteTranslatingGraphMousePlugin() {}

  public SatelliteTranslatingGraphMousePlugin(int modifiers) {
    super(modifiers);
  }

  /**
   * Check the modifiers. If accepted, translate the main view according to the dragging of the
   * mouse pointer in the satellite view
   *
   * @param e the event
   */
  public void mouseDragged(MouseEvent e) {
    VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
    boolean accepted = checkModifiers(e);
    if (accepted) {
      if (vv instanceof SatelliteVisualizationViewer) {
        VisualizationViewer<?, ?> vvMaster = ((SatelliteVisualizationViewer<?, ?>) vv).getMaster();

        MutableTransformer modelTransformerMaster =
            vvMaster
                .getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
        vv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        try {
          Point2D q = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
          Point2D p =
              vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint());
          float dx = (float) (p.getX() - q.getX());
          float dy = (float) (p.getY() - q.getY());

          modelTransformerMaster.translate(-dx, -dy);
          down.x = e.getX();
          down.y = e.getY();
        } catch (RuntimeException ex) {
          log.error("down = {}, e = {}", down, e);
          throw ex;
        }
      }
      e.consume();
    }
  }
}
