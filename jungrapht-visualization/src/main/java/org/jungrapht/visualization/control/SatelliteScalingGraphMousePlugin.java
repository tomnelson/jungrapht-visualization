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

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import org.jungrapht.visualization.SatelliteVisualizationViewer;
import org.jungrapht.visualization.VisualizationViewer;

/**
 * Overrides ScalingGraphMousePlugin so that mouse events in the satellite view will cause scaling
 * in the main view
 * <li>Using only the mouse wheel, both the X-axis and Y-axis are scaled equally.
 * <li>If the CTRL key is pressed while the mouse wheel is turned, only the X-axis is scaled
 * <li>If the ALT key is pressed while the mouse wheel is turned, only the Y-axis is scaled
 *
 * @see ScalingGraphMousePlugin
 * @author Tom Nelson
 */
public class SatelliteScalingGraphMousePlugin extends ScalingGraphMousePlugin {

  public static class Builder extends ScalingGraphMousePlugin.Builder {

    public SatelliteScalingGraphMousePlugin build() {
      return new SatelliteScalingGraphMousePlugin(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public SatelliteScalingGraphMousePlugin() {
    this(SatelliteScalingGraphMousePlugin.builder());
  }

  public SatelliteScalingGraphMousePlugin(Builder builder) {
    this(
        builder.scalingControl,
        builder.scalingMask,
        builder.xAxisScalingMask,
        builder.yAxisScalingMask);
  }

  public SatelliteScalingGraphMousePlugin(
      ScalingControl scaler, int scalingMask, int xAxisScalingMask, int yAxisScalingMask) {
    super(scaler, scalingMask, xAxisScalingMask, yAxisScalingMask);
  }

  public SatelliteScalingGraphMousePlugin(
      ScalingControl scaler,
      int scalingMask,
      int xAxisScalingMask,
      int yAxisScalingMask,
      float in,
      float out) {
    super(scaler, scalingMask, xAxisScalingMask, yAxisScalingMask, in, out);
  }

  /**
   * zoom the master view display in or out, depending on the direction of the mouse wheel motion.
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    boolean accepted = checkModifiers(e);
    if (accepted) {
      ScalingControl scalingControl = scaler;
      float xin = in;
      float yin = in;
      float xout = out;
      float yout = out;
      // check for single axis
      if (e.getModifiersEx() == xAxisScalingMask) {
        // only scale x axis,
        yin = yout = 1.0f;
        scalingControl = layoutScalingControl;
      }
      if (e.getModifiersEx() == yAxisScalingMask) {
        // only scroll y axis
        xin = xout = 1.0f;
        scalingControl = layoutScalingControl;
      }
      VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();

      if (vv instanceof SatelliteVisualizationViewer) {
        VisualizationViewer<?, ?> vvMaster = ((SatelliteVisualizationViewer<?, ?>) vv).getMaster();

        int amount = e.getWheelRotation();

        if (amount < 0) {
          scalingControl.scale(vvMaster, xin, yin, vvMaster.getCenter());

        } else if (amount > 0) {
          scalingControl.scale(vvMaster, xout, yout, vvMaster.getCenter());
        }
        e.consume();
        vv.repaint();
      }
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {}
}
