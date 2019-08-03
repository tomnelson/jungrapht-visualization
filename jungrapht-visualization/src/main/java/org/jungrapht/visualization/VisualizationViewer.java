/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization;

import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.control.GraphMouseListener;

/**
 * Adds mouse behaviors and tooltips to the graph VisualizationServer base class
 *
 * @author Tom Nelson
 */
public interface VisualizationViewer<V, E> extends VisualizationServer<V, E> {

  class Builder<V, E, T extends DefaultVisualizationViewer<V, E>, B extends Builder<V, E, T, B>>
      extends DefaultVisualizationServer.Builder<V, E, T, B> {

    protected Builder(Graph<V, E> graph) {
      super(graph);
    }

    protected Builder(VisualizationModel<V, E> visualizationModel) {
      super(visualizationModel);
    }

    public T build() {
      return (T) new DefaultVisualizationViewer<>(this);
    }
  }

  static <V, E> Builder<V, E, ?, ?> builder(Graph<V, E> graph) {
    return new Builder(graph);
  }

  static <V, E> Builder<V, E, ?, ?> builder(VisualizationModel<V, E> visualizationModel) {
    return new Builder(visualizationModel);
  }

  /**
   * a setter for the GraphMouse. This will remove any previous GraphMouse (including the one that
   * is added in the initMouseClicker method.
   *
   * @param graphMouse new value
   */
  void setGraphMouse(GraphMouse graphMouse);

  /** @return the current <code>GraphMouse</code> */
  GraphMouse getGraphMouse();

  /**
   * This is the interface for adding a mouse listener. The GEL will be called back with mouse
   * clicks on vertices.
   *
   * @param graphMouseListener the mouse listener to add
   */
  void addGraphMouseListener(GraphMouseListener<V> graphMouseListener);

  /**
   * Override to request focus on mouse enter, if a key listener is added
   *
   * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
   */
  void addKeyListener(KeyListener l);

  /** @param edgeToolTipFunction the edgeToolTipFunction to set */
  void setEdgeToolTipFunction(Function<E, String> edgeToolTipFunction);

  /** @param mouseEventToolTipFunction the mouseEventToolTipFunction to set */
  void setMouseEventToolTipFunction(Function<MouseEvent, String> mouseEventToolTipFunction);

  /** @param vertexToolTipFunction the vertexToolTipFunction to set */
  void setVertexToolTipFunction(Function<V, String> vertexToolTipFunction);

  /** called by the superclass to display tooltips */
  String getToolTipText(MouseEvent event);

  void setToolTipText(String toolTipText);

  /**
   * a convenience type to represent a class that processes all types of mouse events for the graph
   */
  interface GraphMouse extends MouseListener, MouseMotionListener, MouseWheelListener {}
}
