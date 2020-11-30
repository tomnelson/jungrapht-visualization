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
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensTransformer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends PrevTranslatingGraphMousePlugin and adds the capability to drag and resize the viewing
 * lens in the graph view. Mouse1 in the center moves the lens, mouse1 on the edge resizes the lens.
 * The default mouse button and modifiers can be overridden in the constructor.
 *
 * @author Tom Nelson
 */
public class LensTranslatingGraphMousePlugin extends TranslatingGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(LensTranslatingGraphMousePlugin.class);
  protected boolean dragOnLens;
  protected boolean dragOnEdge;
  protected double edgeOffset;
  /** create an instance with default modifiers */
  public LensTranslatingGraphMousePlugin() {
    this(MouseEvent.BUTTON1_DOWN_MASK);
  }

  /**
   * create an instance with passed modifer value
   *
   * @param translatingMask the mouse event modifier to activate this function
   */
  public LensTranslatingGraphMousePlugin(int translatingMask) {
    super(translatingMask);
  }

  /**
   * Check the event modifiers. Set the 'down' point for later use. If this event satisfies the
   * modifiers, change the cursor to the system 'move cursor'
   *
   * @param e the event
   */
  public void mousePressed(MouseEvent e) {
    log.info("mousePressed in {}", this.getClass().getName());

    VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW);
    MutableTransformer layoutTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    Point2D p = e.getPoint();
    if (viewTransformer instanceof LensTransformer) {
      //        viewTransformer = ((LensTransformer) viewTransformer).getDelegate();
      p = ((LensTransformer) viewTransformer).getDelegate().inverseTransform(p);
    } else {
      p = viewTransformer.inverseTransform(p);
    }
    boolean accepted = e.getModifiersEx() == translatingMask;
    if (accepted) {
      vv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      if (layoutTransformer instanceof LensTransformer) {
        Lens lens = ((LensTransformer) layoutTransformer).getLens();
        if (testViewCenter(lens, p)) {
          e.consume();
        }
      }
      if (viewTransformer instanceof LensTransformer) {
        Lens lens = ((LensTransformer) viewTransformer).getLens();
        if (testViewCenter(lens, p)) {
          e.consume();
        }
      }
      vv.repaint();
    }
    //    super.mousePressed(e);
  }

  /**
   * called to change the location of the lens
   *
   * @param lens
   * @param point
   */
  private void setViewCenter(Lens lens, Point2D point) {
    lens.setCenter(point);
  }

  /**
   * called to change the radius of the lens
   *
   * @param lens
   * @param point
   */
  private void setViewRadius(Lens lens, Point2D point) {
    double distanceFromCenter = lens.getDistanceFromCenter(point);
    lens.setRadius(distanceFromCenter + edgeOffset);
  }

  /**
   * called to set up translating the lens center or changing the layoutSize
   *
   * @param lens
   * @param point
   */
  private boolean testViewCenter(Lens lens, Point2D point) {
    double closeness = lens.getRadius() / 10;
    double distanceFromCenter = lens.getDistanceFromCenter(point);
    if (distanceFromCenter < closeness) {
      lens.setCenter(point);
      dragOnLens = true;
    } else if (Math.abs(distanceFromCenter - lens.getRadius()) < closeness) {
      edgeOffset = lens.getRadius() - distanceFromCenter;
      lens.setRadius(distanceFromCenter + edgeOffset);
      dragOnEdge = true;
    }
    return dragOnLens || dragOnEdge;
  }

  /** unset the 'down' point and change the cursoe back to the system default cursor */
  public void mouseReleased(MouseEvent e) {
    log.info("mouseReleased in {}", this.getClass().getName());
    if (dragOnEdge || dragOnLens) {
      e.consume();
    }
    //    super.mouseReleased(e);
    dragOnLens = false;
    dragOnEdge = false;
    edgeOffset = 0;
  }

  /**
   * check the modifiers. If accepted, move or resize the lens according to the dragging of the
   * mouse pointer
   *
   * @param e the event
   */
  public void mouseDragged(MouseEvent e) {
    boolean accepted = e.getModifiersEx() == translatingMask;
    if (accepted) {

      VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
      MultiLayerTransformer multiLayerTransformer =
          vv.getRenderContext().getMultiLayerTransformer();
      MutableTransformer layoutTransformer =
          multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.LAYOUT);

      MutableTransformer viewTransformer =
          multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW);
      Lens lens =
          (layoutTransformer instanceof LensTransformer)
              ? ((LensTransformer) layoutTransformer).getLens()
              : (viewTransformer instanceof LensTransformer)
                  ? ((LensTransformer) viewTransformer).getLens()
                  : null;
      if (lens != null) {
        Point2D p = e.getPoint();
        if (viewTransformer instanceof LensTransformer) {
          p = ((LensTransformer) viewTransformer).getDelegate().inverseTransform(p);
        } else {
          p = viewTransformer.inverseTransform(p);
        }

        vv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        if (dragOnLens) {
          setViewCenter(lens, p);
          e.consume();
          vv.repaint();

        } else if (dragOnEdge) {
          setViewRadius(lens, p);
          e.consume();
          vv.repaint();

        } else {

          //          super.mouseDragged(e);
        }
      }
    }
  }
}
