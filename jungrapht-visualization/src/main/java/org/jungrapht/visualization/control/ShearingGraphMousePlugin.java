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

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ShearingGraphMousePlugin allows the user to drag with the mouse to shear the transform either in
 * the horizontal or vertical direction. By default, the control or meta key must be depressed to
 * activate shearing.
 *
 * @author Tom Nelson
 */
public class ShearingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(ShearingGraphMousePlugin.class);

  public static class Builder<
      V, E, T extends ShearingGraphMousePlugin, B extends Builder<V, E, T, B>> {
    protected int shearingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "shearingMask", "MB1_MENU"));

    protected Builder() {}

    public B self() {
      return (B) this;
    }

    public B shearingMask(int shearingMask) {
      this.shearingMask = shearingMask;
      return self();
    }

    public T build() {
      return (T) new ShearingGraphMousePlugin<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected int shearingMask;

  /** create an instance with default modifier values */
  public ShearingGraphMousePlugin() {
    this(ShearingGraphMousePlugin.builder());
  }

  /**
   * create an instance with passed modifier values
   *
   * @param builder the builder to use
   */
  public ShearingGraphMousePlugin(Builder<V, E, ?, ?> builder) {
    this.shearingMask = builder.shearingMask;
    this.cursor = createCursor();
  }

  protected Cursor createCursor() {
    Dimension cd = Toolkit.getDefaultToolkit().getBestCursorSize(16, 16);
    if (cd.width == 0 || cd.height == 0) {
      // custom cursors not supported
      log.warn("Custom Cursor not supported");
      return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }
    BufferedImage cursorImage = new BufferedImage(cd.width, cd.height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = cursorImage.createGraphics();
    g.addRenderingHints(
        Collections.singletonMap(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    g.setColor(new Color(0, 0, 0, 0));
    g.fillRect(0, 0, 16, 16);

    int left = 0;
    int top = 0;
    int right = 15;
    int bottom = 15;

    g.setColor(Color.white);
    g.setStroke(new BasicStroke(3));
    g.drawLine(left + 2, top + 5, right - 2, top + 5);
    g.drawLine(left + 2, bottom - 5, right - 2, bottom - 5);
    g.drawLine(left + 2, top + 5, left + 4, top + 3);
    g.drawLine(left + 2, top + 5, left + 4, top + 7);
    g.drawLine(right - 2, bottom - 5, right - 4, bottom - 3);
    g.drawLine(right - 2, bottom - 5, right - 4, bottom - 7);

    g.setColor(Color.black);
    g.setStroke(new BasicStroke(1));
    g.drawLine(left + 2, top + 5, right - 2, top + 5);
    g.drawLine(left + 2, bottom - 5, right - 2, bottom - 5);
    g.drawLine(left + 2, top + 5, left + 4, top + 3);
    g.drawLine(left + 2, top + 5, left + 4, top + 7);
    g.drawLine(right - 2, bottom - 5, right - 4, bottom - 3);
    g.drawLine(right - 2, bottom - 5, right - 4, bottom - 7);
    g.dispose();
    return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(), "RotateCursor");
  }

  /**
   * check the mouse event modifiers against the instance member modifiers. Default implementation
   * checks equality. Can be overridden to test with a mask
   *
   * @param e
   */
  @Override
  public boolean checkModifiers(MouseEvent e) {
    return e.getModifiersEx() == this.shearingMask;
  }

  public void mousePressed(MouseEvent e) {
    log.trace("mousePressed in {}", this.getClass().getName());
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    boolean accepted = e.getModifiersEx() == shearingMask;
    down = e.getPoint();
    if (accepted) {
      vv.setCursor(cursor);
    }
  }

  public void mouseReleased(MouseEvent e) {
    log.trace("mouseReleased in {}", this.getClass().getName());
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    down = null;
    vv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  public void mouseDragged(MouseEvent e) {
    if (down == null) {
      return;
    }
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    boolean accepted = e.getModifiersEx() == shearingMask;
    if (accepted) {
      MutableTransformer modelTransformer =
          vv.getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
      vv.setCursor(cursor);
      Point2D q = down;
      Point2D p = e.getPoint();
      float dx = (float) (p.getX() - q.getX());
      float dy = (float) (p.getY() - q.getY());

      Dimension d = vv.getSize();
      double shx = 2.f * dx / d.height;
      double shy = 2.f * dy / d.width;
      Point2D center = vv.getCenter();
      if (p.getX() < center.getX()) {
        shy = -shy;
      }
      if (p.getY() < center.getY()) {
        shx = -shx;
      }
      modelTransformer.shear(shx, shy, center);
      down.x = e.getX();
      down.y = e.getY();

      e.consume();
    }
  }

  public void mouseClicked(MouseEvent e) {
    // TODO Auto-generated method stub

  }

  public void mouseEntered(MouseEvent e) {
    // TODO Auto-generated method stub

  }

  public void mouseExited(MouseEvent e) {
    // TODO Auto-generated method stub

  }

  public void mouseMoved(MouseEvent e) {
    // TODO Auto-generated method stub

  }
}
