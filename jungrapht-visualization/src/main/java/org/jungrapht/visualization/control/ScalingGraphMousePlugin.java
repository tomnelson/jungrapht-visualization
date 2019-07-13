/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package org.jungrapht.visualization.control;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.VisualizationViewer;

/**
 * ScalingGraphMouse applies a scaling transformation to the graph layout. The Nodes get closer or
 * farther apart, but do not themselves change layoutSize. ScalingGraphMouse uses MouseWheelEvents
 * to apply the scaling.
 *
 * @author Tom Nelson
 */
public class ScalingGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseWheelListener {

  /** the amount to zoom in by */
  protected float in = 1.1f;
  /** the amount to zoom out by */
  protected float out = 1 / 1.1f;

  /** whether to center the zoom at the current mouse position */
  protected boolean zoomAtMouse = true;

  /** controls scaling operations */
  protected ScalingControl scaler;

  protected Timer timer;

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
    return e.getModifiers() == modifiers || (e.getModifiers() & modifiers) != 0;
  }

  static class Timer extends Thread {
    long value = 10;
    boolean done;
    VisualizationViewer vv;

    public Timer(VisualizationViewer vv) {
      this.vv = vv;
      vv.simplifyRenderer(true);
    }

    public void incrementValue() {
      value = 10;
    }

    public void run() {
      done = false;
      while (value > 0) {
        value--;
        try {
          Thread.sleep(50);
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      }
      vv.simplifyRenderer(false);
      done = true;
      vv.repaint();
    }
  }
  /** zoom the display in or out, depending on the direction of the mouse wheel motion. */
  public void mouseWheelMoved(MouseWheelEvent e) {
    boolean accepted = checkModifiers(e);
    if (accepted == true) {
      VisualizationViewer vv = (VisualizationViewer) e.getSource();
      if (timer == null || timer.done) {
        timer = new Timer(vv);
        timer.start();
      } else {
        timer.incrementValue();
      }
      Point2D mouse = e.getPoint();
      Point2D center = vv.getCenter();
      int amount = e.getWheelRotation();
      if (zoomAtMouse) {
        if (amount > 0) {
          scaler.scale(vv, in, mouse);
        } else if (amount < 0) {
          scaler.scale(vv, out, mouse);
        }
      } else {
        if (amount > 0) {
          scaler.scale(vv, in, center);
        } else if (amount < 0) {
          scaler.scale(vv, out, center);
        }
      }
      e.consume();
      vv.repaint();
      //      vv.simplifyRenderer(false);
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
