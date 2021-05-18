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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TranslatingGraphMousePlugin uses a MouseButtonOne press and drag gesture to translate the graph
 * display in the x and y direction. The default MouseButtonOne modifier can be overridden to cause
 * a different mouse gesture to translate the display.
 *
 * @author Tom Nelson
 */
public class TranslatingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(TranslatingGraphMousePlugin.class);

  public static class Builder<
      V, E, T extends TranslatingGraphMousePlugin<V, E>, B extends Builder<V, E, T, B>> {
    protected int translatingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "translatingMask", "MB1"));

    protected Builder() {}

    public B self() {
      return (B) this;
    }

    public B translatingMask(int translatingMask) {
      this.translatingMask = translatingMask;
      return self();
    }

    public T build() {
      return (T) new TranslatingGraphMousePlugin<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  protected int translatingMask;

  public TranslatingGraphMousePlugin() {
    this(builder());
  }

  /**
   * create an instance with passed translatingMask value
   *
   * @param builder the builder to use
   */
  public TranslatingGraphMousePlugin(Builder<V, E, ?, ?> builder) {
    this.translatingMask = builder.translatingMask;
    log.trace("setModifiers({})", translatingMask);
    this.cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
  }

  /**
   * Check the event modifiers. Set the 'down' point for later use. If this event satisfies the
   * modifiers, change the cursor to the system 'move cursor'
   *
   * @param e the event
   */
  public void mousePressed(MouseEvent e) {
    log.trace("mousePressed in {}", this.getClass().getName());
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    boolean accepted = e.getModifiersEx() == translatingMask;
    down = e.getPoint();
    if (accepted) {
      vv.setCursor(cursor);
      e.consume();
    }
  }

  /** unset the 'down' point and change the cursor back to the system default cursor */
  public void mouseReleased(MouseEvent e) {
    log.trace("mouseReleased in {}", this.getClass().getName());
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    down = null;
    vv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    if (e.getModifiersEx() == translatingMask) {
      e.consume();
    }
  }

  /**
   * check the modifiers. If accepted, translate the graph according to the dragging of the mouse
   * pointer
   *
   * @param e the event
   */
  public void mouseDragged(MouseEvent e) {
    if (down == null) {
      return;
    }
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    boolean accepted = e.getModifiersEx() == translatingMask;
    if (accepted) {
      MutableTransformer modelTransformer =
          vv.getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
      vv.setCursor(cursor);
      try {
        Point2D q = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
        Point2D p = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint());
        float dx = (float) (p.getX() - q.getX());
        float dy = (float) (p.getY() - q.getY());

        modelTransformer.translate(dx, dy);
        down.x = e.getX();
        down.y = e.getY();
      } catch (RuntimeException ex) {
        log.error("down = {}, e = {}", down, e);
        throw ex;
      }

      e.consume();
      vv.repaint();
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

  public String toString() {
    return getClass().getSimpleName()
        + "\n translatingMask :"
        + Modifiers.maskStrings.get(translatingMask);
  }
}
