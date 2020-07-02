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

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.VisualizationViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScalingGraphMouse applies a scaling transformation to the graph layout. The Vertices get closer
 * or farther apart, but do not themselves change layoutSize. ScalingGraphMouse uses
 * MouseWheelEvents to apply the scaling.
 *
 * @author Tom Nelson
 */
public class ScalingGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseWheelListener {

  private static final Logger log = LoggerFactory.getLogger(ScalingGraphMousePlugin.class);

  /** the amount to zoom in by */
  protected float in = 1.1f;
  /** the amount to zoom out by */
  protected float out = 1 / 1.1f;

  /** whether to center the zoom at the current mouse position */
  protected boolean zoomAtMouse = true;

  /** controls scaling operations */
  protected ScalingControl scaler;

  public ScalingGraphMousePlugin(ScalingControl scaler, int modifiers) {
    this(scaler, modifiers, 1.1f, 1 / 1.1f);
  }

  public ScalingGraphMousePlugin(ScalingControl scaler, int modifiers, float in, float out) {
    super(modifiers);
    this.scaler = scaler;
    this.in = in;
    this.out = out;
  }
  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    this.zoomAtMouse = zoomAtMouse;
  }

  public boolean checkModifiers(MouseEvent e) {
    return e.getModifiersEx() == modifiers
        || (e.getModifiersEx() & modifiers) != 0
        || e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK
        || e.getModifiersEx() == InputEvent.ALT_DOWN_MASK;
  }

  /** zoom the display in or out, depending on the direction of the mouse wheel motion. */
  public void mouseWheelMoved(MouseWheelEvent e) {
    boolean accepted = checkModifiers(e);
    if (accepted) {
      float xin = in;
      float yin = in;
      float xout = out;
      float yout = out;
      // check for single axis
      if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
        // only scale x axis,
        yin = yout = 1.0f;
      }
      if ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK) {
        // only scroll y axis
        xin = xout = 1.0f;
      }
      VisualizationViewer vv = (VisualizationViewer) e.getSource();
      Point2D mouse = e.getPoint();
      Point2D center = vv.getCenter();
      int amount = e.getWheelRotation();
      if (zoomAtMouse) {
        if (amount < 0) {
          scaler.scale(vv, xin, yin, mouse);
        } else if (amount > 0) {
          scaler.scale(vv, xout, yout, mouse);
        }
      } else {
        if (amount < 0) {
          scaler.scale(vv, xin, yin, center);
        } else if (amount > 0) {
          scaler.scale(vv, xout, yout, center);
        }
      }
      e.consume();
      vv.repaint();
    }
  }

  /** @return Returns the zoom in value. */
  public float getIn() {
    return in;
  }
  /** @param in The zoom in value to set. */
  public void setIn(float in) {
    this.in = in;
  }
  /** @return Returns the zoom out value. */
  public float getOut() {
    return out;
  }
  /** @param out The zoom out value to set. */
  public void setOut(float out) {
    this.out = out;
  }

  public ScalingControl getScaler() {
    return scaler;
  }

  public void setScaler(ScalingControl scaler) {
    this.scaler = scaler;
  }
}
