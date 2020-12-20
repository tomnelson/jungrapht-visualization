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
import java.awt.geom.Point2D;
import java.util.Map;
import javax.swing.JOptionPane;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LabelEditingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener {

  private static final Logger log = LoggerFactory.getLogger(LabelEditingGraphMousePlugin.class);
  /** the selected Vertex, if any */
  protected V vertex;

  /** the selected Edge, if any */
  protected E edge;

  /** Holds vertex String associations that may be used in the vertex label function */
  protected Map<V, String> vertexLabelMap;

  /** Holds edge to String associations that may be used in the edge label function */
  protected Map<E, String> edgeLabelMap;

  protected int selectionModifiers;

  /** create an instance with default settings */
  public LabelEditingGraphMousePlugin(Map<V, String> vertexLabelMap, Map<E, String> edgeLabelMap) {
    this(vertexLabelMap, edgeLabelMap, 0);
  }

  /**
   * create an instance with overrides
   *
   * @param selectionModifiers for primary selection
   */
  public LabelEditingGraphMousePlugin(
      Map<V, String> vertexLabelMap, Map<E, String> edgeLabelMap, int selectionModifiers) {
    this.selectionModifiers = selectionModifiers;
    this.vertexLabelMap = vertexLabelMap;
    this.edgeLabelMap = edgeLabelMap;
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  }

  /**
   * If the mouse is double-clicked on a vertex, show a popup to request a new label for the vertex.
   * The label becomes the value for the vertex key in the vertexLabelMap
   *
   * @param e the event
   */
  @SuppressWarnings("unchecked")
  public void mouseClicked(MouseEvent e) {
    log.trace("mouseClicked  count {} in {}", e.getClickCount(), this.getClass().getName());
    if (e.getClickCount() == 2) {
      VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
      LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
      GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
      if (pickSupport != null) {
        // p is the screen point for the mouse event
        Point2D p = e.getPoint();

        V vertex = pickSupport.getVertex(layoutModel, p.getX(), p.getY());
        if (vertex != null) {
          String newLabel = JOptionPane.showInputDialog("New Vertex Label for " + vertex);
          if (newLabel != null) {
            vertexLabelMap.put(vertex, newLabel);
            vv.repaint();
          }
          e.consume();
          return;
        }
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
            e.consume();
          }
          return;
        }
      }
    }
  }

  public void mouseReleased(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}
}
