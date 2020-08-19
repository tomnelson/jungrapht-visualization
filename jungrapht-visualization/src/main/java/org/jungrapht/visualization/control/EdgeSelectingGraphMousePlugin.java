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

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

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
import javax.swing.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.selection.ShapePickSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EdgeSelectingGraphMousePlugin supports the selecting of graph elements with the mouse.
 * MouseButtonOne selects a single vertex or edge, and MouseButtonTwo adds to the set of selected
 * Vertices or EdgeType. If a Vertex is selected and the mouse is dragged while on the selected
 * Vertex, then that Vertex will be repositioned to follow the mouse until the button is released.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class EdgeSelectingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin.Selecting
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(EdgeSelectingGraphMousePlugin.class);

  private static final int TOO_CLOSE_LIMIT = 5;

  private static final String PICK_AREA_SIZE = PREFIX + "pickAreaSize";

  protected int pickSize = Integer.getInteger(PICK_AREA_SIZE, 4);

  /** the selected Edge, if any */
  protected E edge;

  protected int selectionModifiers;

  /** additional modifiers for the action of adding to an existing selection */
  protected int addToSelectionModifiers;

  protected Rectangle2D footprintRectangle = new Rectangle2D.Float();
  protected VisualizationViewer.Paintable pickFootprintPaintable;

  /** color for the picking rectangle */
  protected Color lensColor = Color.cyan;

  protected Point2D deltaDown; // what's that flower you have on...

  /** create an instance with default settings */
  public EdgeSelectingGraphMousePlugin() {
    this(
        InputEvent.BUTTON1_DOWN_MASK,
        InputEvent.CTRL_DOWN_MASK, // select or drag select in rectangle
        InputEvent.SHIFT_DOWN_MASK);
  }

  /**
   * create an instance with overrides
   *
   * @param selectionModifiers for primary selection
   * @param addToSelectionModifiers for additional selection
   */
  public EdgeSelectingGraphMousePlugin(
      int modifiers, int selectionModifiers, int addToSelectionModifiers) {
    super(modifiers);
    this.selectionModifiers = selectionModifiers;
    this.addToSelectionModifiers = addToSelectionModifiers;
    this.pickFootprintPaintable = new FootprintPaintable();
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  }

  /** @return Returns the lensColor. */
  public Color getLensColor() {
    return lensColor;
  }

  /** @param lensColor The lensColor to set. */
  public void setLensColor(Color lensColor) {
    this.lensColor = lensColor;
  }

  class FootprintPaintable implements VisualizationServer.Paintable {

    public void paint(Graphics g) {
      Color oldColor = g.getColor();
      g.setColor(lensColor);
      ((Graphics2D) g).draw(footprintRectangle);
      g.setColor(oldColor);
    }

    public boolean useTransform() {
      return true;
    }
  }

  /**
   * For primary modifiers (default, MouseButton1): pick a single Vertex or Edge that is under the
   * mouse pointer. If no Vertex or edge is under the pointer, unselect all selected Vertices and
   * edges, and set up to draw a rectangle for multiple selection of contained Vertices. For
   * additional selection (default Shift+MouseButton1): Add to the selection, a single Vertex or
   * Edge that is under the mouse pointer. If a previously selected Vertex or Edge is under the
   * pointer, it is un-selected. If no vertex or Edge is under the pointer, set up to draw a
   * multiple selection rectangle (as above) but do not unpick previously selected elements.
   *
   * @param e the event
   */
  public void mousePressed(MouseEvent e) {
    down = e.getPoint();
    log.trace("mouse pick at screen coords {}", e.getPoint());
    deltaDown = down;
    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    TransformSupport<V, E> transformSupport = vv.getTransformSupport();
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    MutableSelectedState<E> selectedEdgeState = vv.getSelectedEdgeState();

    // a rectangle in the view coordinate system.
    this.footprintRectangle =
        new Rectangle2D.Float(
            (float) e.getPoint().x - pickSize / 2,
            (float) e.getPoint().y - pickSize / 2,
            pickSize,
            pickSize);

    vv.addPostRenderPaintable(pickFootprintPaintable);
    vv.repaint();

    // subclass can override to account for view distortion effects
    // layoutPoint is the mouse event point projected on the layout coordinate system
    Point2D layoutPoint = transformSupport.inverseTransform(vv, down);
    log.trace("layout coords of mouse click {}", layoutPoint);
    if (e.getModifiersEx() == (modifiers | selectionModifiers)) { // default button1 down and ctrl

      if (pickSupport instanceof ShapePickSupport) {
        ShapePickSupport<V, E> shapePickSupport = (ShapePickSupport<V, E>) pickSupport;
        this.edge = shapePickSupport.getEdge(layoutModel, footprintRectangle);
      } else {
        this.edge = pickSupport.getEdge(layoutModel, layoutPoint.getX(), layoutPoint.getY());
      }

      if (edge != null) {

        log.trace("mousePressed set the edge to {}", edge);
        if (!selectedEdgeState.isSelected(edge)) {
          selectedEdgeState.clear();
          selectedEdgeState.select(edge);
        }
        e.consume();
        return;
      }

      // got no edge, clear all selections
      selectedEdgeState.clear();
      return;

    } else if (e.getModifiersEx() == (modifiers | selectionModifiers | addToSelectionModifiers)) {

      this.edge = pickSupport.getEdge(layoutModel, layoutPoint.getX(), layoutPoint.getY());

      if (edge != null) {
        log.trace("mousePressed set the edge to {}", edge);
        if (selectedEdgeState.isSelected(edge)) {
          selectedEdgeState.deselect(edge);
        } else {
          selectedEdgeState.select(edge);
        }
        e.consume();
        return;
      }
    } else {
      down = null;
    }
    if (edge != null) {
      e.consume();
    }
  }

  /**
   * If the mouse is dragging a rectangle, pick the Vertices contained in that rectangle
   *
   * <p>clean up settings from mousePressed
   */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    Point2D out = e.getPoint();

    VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    down = null;
    edge = null;
    vv.removePostRenderPaintable(pickFootprintPaintable);
    vv.repaint();
  }

  /** no op */
  public void mouseDragged(MouseEvent e) {}

  /**
   * override to consider Lens effects
   *
   * @param vv
   * @param p
   * @return
   */
  protected Point2D inverseTransform(VisualizationViewer<V, E> vv, Point2D p) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.inverseTransform(p);
  }

  /**
   * override to consider Lens effects
   *
   * @param vv
   * @param shape
   * @return
   */
  protected Shape transform(VisualizationViewer<V, E> vv, Shape shape) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.transform(shape);
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
}
