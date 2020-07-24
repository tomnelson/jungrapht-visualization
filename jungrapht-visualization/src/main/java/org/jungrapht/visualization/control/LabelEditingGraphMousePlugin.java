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
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.function.Function;
import javax.swing.JOptionPane;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LabelEditingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener {

  /** the selected Vertex, if any */
  protected V vertex;

  /** the selected Edge, if any */
  protected E edge;

  protected Map<V, String> vertexLabelMap;

  protected Map<E, String> edgeLabelMap;

  /** create an instance with default settings */
  public LabelEditingGraphMousePlugin(Map<V, String> vertexLabelMap, Map<E, String> edgeLabelMap) {
    this(vertexLabelMap, edgeLabelMap, InputEvent.BUTTON1_DOWN_MASK);
  }

  /**
   * create an instance with overrides
   *
   * @param selectionModifiers for primary selection
   */
  public LabelEditingGraphMousePlugin(
      Map<V, String> vertexLabelMap, Map<E, String> edgeLabelMap, int selectionModifiers) {
    super(selectionModifiers);
    this.vertexLabelMap = vertexLabelMap;
    this.edgeLabelMap = edgeLabelMap;
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
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
  @SuppressWarnings("unchecked")
  public void mouseClicked(MouseEvent e) {
    if (e.getModifiersEx() == modifiers && e.getClickCount() == 2) {
      VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
      LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
      GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
      if (pickSupport != null) {
        Function<V, String> vs = vv.getRenderContext().getVertexLabelFunction();
        // p is the screen point for the mouse event
        Point2D p = e.getPoint();

        V vertex = pickSupport.getVertex(layoutModel, p.getX(), p.getY());
        if (vertex != null) {
          String newLabel = vs.apply(vertex);
          newLabel = JOptionPane.showInputDialog("New Vertex Label for " + vertex);
          if (newLabel != null) {
            vertexLabelMap.put(vertex, newLabel);
            vv.repaint();
          }
          return;
        }
        //        }
        Function<E, String> es = vv.getRenderContext().getEdgeLabelFunction();
        // p is the screen point for the mouse event
        p = e.getPoint();
        // take away the view transform
        Point2D ip =
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .inverseTransform(MultiLayerTransformer.Layer.VIEW, p);
        E edge = pickSupport.getEdge(layoutModel, ip.getX(), ip.getY());
        if (edge != null) {
          String newLabel = JOptionPane.showInputDialog("New Edge Label for " + edge);
          if (newLabel != null) {
            edgeLabelMap.put(edge, newLabel);
            vv.repaint();
          }
          return;
        }
      }
      e.consume();
    }
  }

  /**
   * If the mouse is dragging a rectangle, pick the Vertices contained in that rectangle
   *
   * <p>clean up settings from mousePressed
   */
  public void mouseReleased(MouseEvent e) {}

  /**
   * If the mouse is over a selected vertex, drag all selected vertices with the mouse. If the mouse
   * is not over a Vertex, draw the rectangle to select multiple Vertices
   */
  public void mousePressed(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}
}
