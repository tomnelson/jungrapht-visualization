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

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;
import java.util.Optional;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.LensGraphMouse;
import org.jungrapht.visualization.control.LensTransformSupport;
import org.jungrapht.visualization.control.ModalLensGraphMouse;
import org.jungrapht.visualization.layout.GraphElementAccessor;

/**
 * A class to make it easy to add an examining lens to a jungrapht graph application. See
 * HyperbolicTransformerDemo, ViewLensSupport and LayoutLensSupport for examples of how to use it.
 *
 * @author Tom Nelson
 */
public abstract class AbstractLensSupport<V, E, M extends LensGraphMouse>
    implements LensSupport<M> {

  private static final String LENS_STROKE_WIDTH = PREFIX + "lensStrokeWidth";

  public abstract static class Builder<
          V,
          E,
          M extends LensGraphMouse,
          T extends AbstractLensSupport<V, E, M>,
          B extends Builder<V, E, M, T, B>>
      implements LensSupport.Builder<M, B> {

    protected VisualizationViewer<V, E> vv;
    protected VisualizationViewer.GraphMouse graphMouse;
    protected M lensGraphMouse = (M) new ModalLensGraphMouse();
    protected String defaultToolTipText;
    protected Runnable killSwitch;
    protected LensTransformer lensTransformer;
    protected GraphElementAccessor<V, E> pickSupport;

    public B self() {
      return (B) this;
    }

    protected Builder(VisualizationViewer<V, E> vv) {
      this.vv = vv;
    }

    public B graphMouse(VisualizationViewer.GraphMouse graphMouse) {
      this.graphMouse = graphMouse;
      return self();
    }

    public B lensGraphMouse(M lensGraphMouse) {
      this.lensGraphMouse = lensGraphMouse;
      return self();
    }

    public B defaultToolTipText(String defaultToolTipText) {
      this.defaultToolTipText = defaultToolTipText;
      return self();
    }

    public B killSwitch(Runnable killSwitch) {
      this.killSwitch = killSwitch;
      return self();
    }

    public B lensTransformer(LensTransformer lensTransformer) {
      this.lensTransformer = lensTransformer;
      return self();
    }

    public B pickSupport(GraphElementAccessor<V, E> pickSupport) {
      this.pickSupport = pickSupport;
      return self();
    }

    public abstract T build();
  }

  protected VisualizationViewer<V, E> vv;
  protected VisualizationViewer.GraphMouse graphMouse;
  protected LensTransformer lensTransformer;
  protected M lensGraphMouse;
  protected LensPaintable lensPaintable;
  protected LensControls lensControls;
  protected String defaultToolTipText;
  boolean active;
  Runnable manager;
  protected GraphElementAccessor<V, E> pickSupport;

  protected static final String instructions =
      "<html><center>Mouse-Drag the Lens center to move it<p>"
          + "Mouse-Drag the Lens edge to resize it<p>"
          + "Ctrl+MouseWheel to change magnification</center></html>";

  protected AbstractLensSupport(Builder<V, E, M, ?, ?> builder) {
    this.vv = builder.vv;
    this.lensGraphMouse = builder.lensGraphMouse;
    this.defaultToolTipText =
        Optional.ofNullable(builder.defaultToolTipText).orElse(vv.getToolTipText());
    this.graphMouse = Optional.ofNullable(builder.graphMouse).orElse(vv.getGraphMouse());
    this.lensGraphMouse = builder.lensGraphMouse;
    this.lensGraphMouse.setKillSwitch(
        Optional.ofNullable(builder.killSwitch).orElse(this::deactivate));
    this.lensTransformer = builder.lensTransformer;
    this.pickSupport = Optional.ofNullable(builder.pickSupport).orElse(vv.getPickSupport());
  }
  /**
   * create the base class, setting common members and creating a custom GraphMouse
   *
   * @param vv the VisualizationViewer to work on
   * @param lensGraphMouse the GraphMouse instance to use for the lens
   */
  public AbstractLensSupport(VisualizationViewer<V, E> vv, M lensGraphMouse) {
    this.vv = vv;
    this.graphMouse = vv.getGraphMouse();
    this.defaultToolTipText = vv.getToolTipText();
    this.lensGraphMouse = lensGraphMouse;
    this.lensGraphMouse.setKillSwitch(this::deactivate);
  }

  public void setManager(Runnable manager) {
    this.manager = manager;
  }

  public boolean allowed() {
    return !(vv.getTransformSupport() instanceof LensTransformSupport);
  }

  public void activate(boolean state) {
    active = state;
    if (state) {
      activate();
      //      manager.run();
    } else {
      deactivate();
    }
  }

  public boolean isActive() {
    return active;
  }

  public LensTransformer getLensTransformer() {
    return lensTransformer;
  }

  /** @return the hyperbolicGraphMouse. */
  public M getGraphMouse() {
    return lensGraphMouse;
  }

  /**
   * the background for the hyperbolic projection
   *
   * @author Tom Nelson
   */
  public static class LensPaintable implements VisualizationServer.Paintable {
    RectangularShape lensShape;

    Paint paint = Color.getColor("jungrapht.lensColor", Color.decode("0xFAFAFA"));

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
    Paint lensControlsDrawColor = Color.getColor("jungrapht.lensControlsColor", Color.gray);
    Paint lensControlsFillColor = Color.getColor("jungrapht.lensColor", Color.decode("0xFAFAFA"));
    float lensStrokeWidth = Float.parseFloat(System.getProperty(LENS_STROKE_WIDTH, "2.0f"));

    public LensControls(LensTransformer lensTransformer) {
      this.lensShape = lensTransformer.getLens().getLensShape();
    }

    /** @return the paint */
    public Paint getPaint() {
      return lensControlsDrawColor;
    }

    /** @param paint the paint to set */
    public void setPaint(Paint paint) {
      this.lensControlsDrawColor = paint;
    }

    public void paint(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      RenderingHints savedRenderingHints = g2d.getRenderingHints();
      RenderingHints renderingHints = (RenderingHints) savedRenderingHints.clone();
      renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHints(renderingHints);
      g2d.setPaint(lensControlsDrawColor);
      Stroke savedStroke = g2d.getStroke();
      // if the transform scale is small, make the stroke wider so it is still visible
      g2d.setStroke(
          new BasicStroke(Math.max(lensStrokeWidth, (int) (1.0 / g2d.getTransform().getScaleX()))));
      g2d.draw(lensShape);
      int centerX = (int) Math.round(lensShape.getCenterX());
      int centerY = (int) Math.round(lensShape.getCenterY());
      g2d.draw(
          new Ellipse2D.Double(
              centerX - lensShape.getWidth() / 40,
              centerY - lensShape.getHeight() / 40,
              lensShape.getWidth() / 20,
              lensShape.getHeight() / 20));

      g2d.draw(
          new Ellipse2D.Double(
              lensShape.getMinX() + lensShape.getWidth() / 40,
              lensShape.getMinY() + lensShape.getHeight() / 40,
              lensShape.getWidth() - lensShape.getWidth() / 20,
              lensShape.getHeight() - lensShape.getHeight() / 20));
      // kill 'button' shape
      Ellipse2D killShape =
          new Ellipse2D.Double(
              lensShape.getMinX() + lensShape.getWidth(),
              lensShape.getMinY(),
              lensShape.getWidth() / 20,
              lensShape.getHeight() / 20);

      g2d.setPaint(lensControlsFillColor);
      g2d.fill(killShape);
      // kill button 'X'
      double radius = killShape.getWidth() / 2;
      double xmin = killShape.getCenterX() - radius * Math.cos(Math.PI / 4);
      double ymin = killShape.getCenterY() - radius * Math.sin(Math.PI / 4);
      double xmax = killShape.getCenterX() + radius * Math.cos(Math.PI / 4);
      double ymax = killShape.getCenterY() + radius * Math.sin(Math.PI / 4);
      g2d.setPaint(lensControlsDrawColor);
      g2d.draw(new Line2D.Double(xmin, ymin, xmax, ymax));
      g2d.draw(new Line2D.Double(xmin, ymax, xmax, ymin));
      g2d.setStroke(savedStroke);
      g2d.setRenderingHints(savedRenderingHints);
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
