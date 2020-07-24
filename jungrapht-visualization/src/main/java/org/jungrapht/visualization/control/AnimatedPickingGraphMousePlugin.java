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
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import javax.swing.JComponent;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.util.VertexLocationAnimator;

/**
 * AnimatedPickingGraphMousePlugin supports the picking of one Graph Vertex. When the mouse is
 * released, the graph is translated so that the selected Vertex is moved to the center of the view.
 * This translation is conducted in an animation Thread so that the graph slides to its new position
 *
 * @author Tom Nelson
 */
public class AnimatedPickingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  /** the selected Vertex */
  protected V vertex;

  /** Creates an instance with default modifiers of BUTTON1_DOWN_MASK and CTRL_DOWN_MASK */
  public AnimatedPickingGraphMousePlugin() {
    this(InputEvent.BUTTON1_DOWN_MASK | InputEvent.CTRL_DOWN_MASK);
  }

  /**
   * Creates an instance with the specified mouse event modifiers.
   *
   * @param selectionModifiers the mouse event modifiers to use.
   */
  public AnimatedPickingGraphMousePlugin(int selectionModifiers) {
    super(selectionModifiers);
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  }

  /**
   * If the event occurs on a Vertex, pick that single Vertex
   *
   * @param e the event
   */
  @SuppressWarnings("unchecked")
  public void mousePressed(MouseEvent e) {
    if (e.getModifiersEx() == modifiers) {
      VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
      LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
      GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
      MutableSelectedState<V> pickedVertexState = vv.getSelectedVertexState();
      if (pickSupport != null && pickedVertexState != null) {
        // p is the screen point for the mouse event
        Point2D p = e.getPoint();
        vertex = pickSupport.getVertex(layoutModel, p.getX(), p.getY());
        if (vertex != null) {
          pickedVertexState.select(vertex);
        }
      }
      e.consume();
    }
  }

  /**
   * If a Vertex was selected in the mousePressed event, start a Thread to animate the translation
   * of the graph so that the selected Vertex moves to the center of the view
   *
   * @param e the event
   */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK) {
      VertexLocationAnimator.scrollVertexToCenter((VisualizationViewer) e.getSource(), vertex);
    }
  }

  public void mouseClicked(MouseEvent e) {}

  /** show a special cursor while the mouse is inside the window */
  public void mouseEntered(MouseEvent e) {
    JComponent c = (JComponent) e.getSource();
    c.setCursor(cursor);
  }

  /** revert to the default cursor when the mouse leaves this window */
  public void mouseExited(MouseEvent e) {
    JComponent c = (JComponent) e.getSource();
    c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  public void mouseMoved(MouseEvent e) {}

  public void mouseDragged(MouseEvent arg0) {}
}
