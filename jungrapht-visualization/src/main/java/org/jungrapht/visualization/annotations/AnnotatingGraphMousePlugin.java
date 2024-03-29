/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.annotations;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.AbstractGraphMousePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnnotatingGraphMousePlugin can create Shape and Text annotations in a layer of the graph
 * visualization.
 *
 * @author Tom Nelson
 */
public class AnnotatingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(AnnotatingGraphMousePlugin.class);
  /** additional modifiers for the action of adding to an existing selection */
  protected int additionalModifiers;

  /** used to draw a Shape annotation */
  protected RectangularShape rectangularShape = new Rectangle2D.Double();

  /** the Paintable for the Shape annotation */
  protected VisualizationServer.Paintable lensPaintable;

  /** a Paintable to store all Annotations */
  protected AnnotationManager annotationManager;

  /** color for annotations */
  protected Color annotationColor = Color.cyan;

  /** layer for annotations */
  protected Annotation.Layer layer = Annotation.Layer.LOWER;

  protected boolean fill;

  /** holds rendering transforms */
  protected MultiLayerTransformer basicTransformer;

  /** holds rendering settings */
  protected RenderContext<V, E> rc;

  /** set to true when the AnnotationPaintable has been added to the view component */
  protected boolean added = false;

  //  protected int modifiers;

  /**
   * Create an instance with defaults for primary (button 1) and secondary (button 1 + shift)
   * selection.
   *
   * @param rc the RenderContext for which this plugin will be used
   */
  public AnnotatingGraphMousePlugin(RenderContext<V, E> rc) {
    this(
        rc,
        InputEvent.BUTTON1_DOWN_MASK,
        InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
  }

  /**
   * Create an instance with the specified primary and secondary selection mechanisms.
   *
   * @param rc the RenderContext for which this plugin will be used
   * @param selectionModifiers for primary selection
   * @param additionalModifiers for additional selection
   */
  public AnnotatingGraphMousePlugin(
      RenderContext<V, E> rc, int selectionModifiers, int additionalModifiers) {
    this.modifiers = selectionModifiers;
    this.rc = rc;
    this.basicTransformer = rc.getMultiLayerTransformer();
    this.additionalModifiers = additionalModifiers;
    this.lensPaintable = new LensPaintable();
    this.annotationManager = new AnnotationManager(rc);
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  }

  /** @return Returns the lensColor. */
  public Color getAnnotationColor() {
    return annotationColor;
  }

  /** @param lensColor The lensColor to set. */
  public void setAnnotationColor(Color lensColor) {
    this.annotationColor = lensColor;
  }

  /** the Paintable that draws a Shape annotation only while it is being created */
  class LensPaintable implements VisualizationServer.Paintable {

    public void paint(Graphics g) {
      Color oldColor = g.getColor();
      g.setColor(annotationColor);
      ((Graphics2D) g).draw(rectangularShape);
      g.setColor(oldColor);
    }

    public boolean useTransform() {
      return false;
    }
  }

  /**
   * Sets the location for an Annotation. Will either pop up a dialog to prompt for text input for a
   * text annotation, or begin the process of drawing a Shape annotation
   *
   * @param e the event
   */
  @SuppressWarnings("unchecked")
  public void mousePressed(MouseEvent e) {
    log.trace("mousePressed in {}", this.getClass().getName());
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    down = e.getPoint();

    if (!added) {
      vv.addPreRenderPaintable(annotationManager.getLowerAnnotationPaintable());
      vv.addPostRenderPaintable(annotationManager.getUpperAnnotationPaintable());
      added = true;
    }

    if (e.isPopupTrigger()) {
      String annotationString = JOptionPane.showInputDialog(vv.getComponent(), "Annotation:");
      if (annotationString != null && annotationString.length() > 0) {
        Point2D p = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
        Annotation<String> annotation =
            new Annotation<>(annotationString, layer, annotationColor, fill, p);
        annotationManager.add(layer, annotation);
      }
    } else if (e.getModifiersEx() == (modifiers | additionalModifiers)) {
      Annotation<?> annotation = annotationManager.getAnnotation(down);
      annotationManager.remove(annotation);
    } else if (e.getModifiersEx() == modifiers) {
      rectangularShape.setFrameFromDiagonal(down, down);
      vv.addPostRenderPaintable(lensPaintable);
    }
    vv.repaint();
  }

  /** Completes the process of adding a Shape annotation and removed the transient paintable */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    log.trace("mouseReleased in {}", this.getClass().getName());
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    if (e.isPopupTrigger()) {
      String annotationString = JOptionPane.showInputDialog(vv, "Annotation:");
      if (annotationString != null && annotationString.length() > 0) {
        Point2D p = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
        Annotation<String> annotation =
            new Annotation<>(annotationString, layer, annotationColor, fill, p);
        annotationManager.add(layer, annotation);
      }
    } else {
      if (down != null) {
        Point2D out = e.getPoint();
        RectangularShape arect = (RectangularShape) rectangularShape.clone();
        arect.setFrameFromDiagonal(down, out);
        Shape s = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(arect);
        Annotation<Shape> annotation = new Annotation<>(s, layer, annotationColor, fill, out);
        annotationManager.add(layer, annotation);
      }
    }
    down = null;
    vv.removePostRenderPaintable(lensPaintable);
    vv.repaint();
  }

  /**
   * Draws the transient Paintable that will become a Shape annotation when the mouse button is
   * released
   */
  @SuppressWarnings("unchecked")
  public void mouseDragged(MouseEvent e) {
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();

    Point2D out = e.getPoint();
    if (e.getModifiersEx() == additionalModifiers) {
      rectangularShape.setFrameFromDiagonal(down, out);

    } else if (e.getModifiersEx() == modifiers) {
      rectangularShape.setFrameFromDiagonal(down, out);
    }
    rectangularShape.setFrameFromDiagonal(down, out);
    vv.repaint();
  }

  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {
    JComponent c = (JComponent) e.getSource();
    c.setCursor(cursor);
  }

  public void mouseExited(MouseEvent e) {
    JComponent c = (JComponent) e.getSource();
    c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  public void mouseMoved(MouseEvent e) {}

  /** @return the rect */
  public RectangularShape getRectangularShape() {
    return rectangularShape;
  }

  /** @param rect the rect to set */
  public void setRectangularShape(RectangularShape rect) {
    this.rectangularShape = rect;
  }

  /** @return the layer */
  public Annotation.Layer getLayer() {
    return layer;
  }

  /** @param layer the layer to set */
  public void setLayer(Annotation.Layer layer) {
    this.layer = layer;
  }

  /** @return the fill */
  public boolean isFill() {
    return fill;
  }

  /** @param fill the fill to set */
  public void setFill(boolean fill) {
    this.fill = fill;
  }
}
