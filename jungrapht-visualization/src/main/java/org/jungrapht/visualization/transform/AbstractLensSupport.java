/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jul 21, 2005
 */

package org.jungrapht.visualization.transform;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.RectangularShape;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.LensGraphMouse;

/**
 * A class to make it easy to add an examining lens to a jung graph application. See
 * HyperbolicTransformerDemo, ViewLensSupport and LayoutLensSupport for examples of how to use it.
 *
 * @author Tom Nelson
 */
public abstract class AbstractLensSupport<V, E, T extends LensGraphMouse>
    implements LensSupport<T> {

  protected VisualizationViewer<V, E> vv;
  protected VisualizationViewer.GraphMouse graphMouse;
  protected LensTransformer lensTransformer;
  protected T lensGraphMouse;
  protected LensPaintable lensPaintable;
  protected LensControls lensControls;
  protected String defaultToolTipText;

  protected static final String instructions =
      "<html><center>Mouse-Drag the Lens center to move it<p>"
          + "Mouse-Drag the Lens edge to resize it<p>"
          + "Ctrl+MouseWheel to change magnification</center></html>";

  /**
   * create the base class, setting common members and creating a custom GraphMouse
   *
   * @param vv the VisualizationViewer to work on
   * @param lensGraphMouse the GraphMouse instance to use for the lens
   */
  public AbstractLensSupport(VisualizationViewer<V, E> vv, T lensGraphMouse) {
    this.vv = vv;
    this.graphMouse = vv.getGraphMouse();
    this.defaultToolTipText = vv.getToolTipText();
    this.lensGraphMouse = lensGraphMouse;
  }

  public void activate(boolean state) {
    if (state) {
      activate();
    } else {
      deactivate();
    }
  }

  public LensTransformer getLensTransformer() {
    return lensTransformer;
  }

  /** @return the hyperbolicGraphMouse. */
  public T getGraphMouse() {
    return lensGraphMouse;
  }

  /**
   * the background for the hyperbolic projection
   *
   * @author Tom Nelson
   */
  public static class LensPaintable implements VisualizationServer.Paintable {
    RectangularShape lensShape;
    Paint paint = Color.decode("0xdddddd");

    public LensPaintable(LensTransformer lensTransformer) {
      this.lensShape = lensTransformer.getLens().getLensShape();
    }

    /** @return the paint */
    public Paint getPaint() {
      return paint;
    }

    /** @param paint the paint to set */
    public void setPaint(Paint paint) {
      this.paint = paint;
    }

    public void paint(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(paint);
      g2d.fill(lensShape);
    }

    public boolean useTransform() {
      return true;
    }
  }

  /**
   * the background for the hyperbolic projection
   *
   * @author Tom Nelson
   */
  public static class LensControls implements VisualizationServer.Paintable {
    RectangularShape lensShape;
    Paint paint = Color.gray;

    public LensControls(LensTransformer lensTransformer) {
      this.lensShape = lensTransformer.getLens().getLensShape();
    }

    /** @return the paint */
    public Paint getPaint() {
      return paint;
    }

    /** @param paint the paint to set */
    public void setPaint(Paint paint) {
      this.paint = paint;
    }

    public void paint(Graphics g) {

      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(paint);
      g2d.draw(lensShape);
      int centerX = (int) Math.round(lensShape.getCenterX());
      int centerY = (int) Math.round(lensShape.getCenterY());
      g.drawOval(centerX - 10, centerY - 10, 20, 20);
    }

    public boolean useTransform() {
      return true;
    }
  }

  /** @return the lensPaintable */
  public LensPaintable getLensPaintable() {
    return lensPaintable;
  }

  /** @param lensPaintable the lens to set */
  public void setLensPaintable(LensPaintable lensPaintable) {
    this.lensPaintable = lensPaintable;
  }

  /** @return the lensControls */
  public LensControls getLensControls() {
    return lensControls;
  }

  /** @param lensControls the lensControls to set */
  public void setLensControls(LensControls lensControls) {
    this.lensControls = lensControls;
  }
}
