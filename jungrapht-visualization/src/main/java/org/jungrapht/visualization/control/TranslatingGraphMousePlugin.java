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
public class TranslatingGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(TranslatingGraphMousePlugin.class);
  /** */
  public TranslatingGraphMousePlugin() {
    this(MouseEvent.BUTTON1_DOWN_MASK);
  }

  /**
   * create an instance with passed modifer value
   *
   * @param modifiers the mouse event modifier to activate this function
   */
  public TranslatingGraphMousePlugin(int modifiers) {
    super(modifiers);
    log.info("setModifiers({})", modifiers);
    this.cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
  }

  /**
   * Check the event modifiers. Set the 'down' point for later use. If this event satisfies the
   * modifiers, change the cursor to the system 'move cursor'
   *
   * @param e the event
   */
  public void mousePressed(MouseEvent e) {
    VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
    boolean accepted = checkModifiers(e);
    down = e.getPoint();
    if (accepted) {
      vv.setCursor(cursor);
    }
  }

  /** unset the 'down' point and change the cursoe back to the system default cursor */
  public void mouseReleased(MouseEvent e) {
    VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
    down = null;
    vv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  /**
   * check the modifiers. If accepted, translate the graph according to the dragging of the mouse
   * pointer
   *
   * @param e the event
   */
  public void mouseDragged(MouseEvent e) {
    VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
    boolean accepted = checkModifiers(e);
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
}
