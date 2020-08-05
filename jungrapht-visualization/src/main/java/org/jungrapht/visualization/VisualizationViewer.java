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
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.GraphMouseListener;

/**
 * Adds mouse behaviors and tooltips to the graph VisualizationServer base class
 *
 * @author Tom Nelson
 */
public interface VisualizationViewer<V, E> extends VisualizationServer<V, E> {

  /**
   * Builder for VisualizationViewer instances
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type of VisualizationViewer to build
   * @param <B> the builder typw
   */
  class Builder<V, E, T extends DefaultVisualizationViewer<V, E>, B extends Builder<V, E, T, B>>
      extends DefaultVisualizationServer.Builder<V, E, T, B> {

    protected GraphMouse graphMouse = new DefaultGraphMouse();
    /** create an instance with no initial args */
    protected Builder() {}

    public B graphMouse(GraphMouse graphMouse) {
      this.graphMouse = graphMouse;
      return self();
    }

    /** @param graph the graph to be visualized */
    protected Builder(Graph<V, E> graph) {
      super(graph);
    }

    /** @param visualizationModel the model for visualization state */
    protected Builder(VisualizationModel<V, E> visualizationModel) {
      super(visualizationModel);
    }

    public T build() {
      return (T) new DefaultVisualizationViewer<>(this);
    }
  }

  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return the builder
   */
  static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }
  /**
   * @param graph the graph to be visualized
   * @param <V> the vertex type
   * @param <E> the edge type
   * @return the builder
   */
  static <V, E> Builder<V, E, ?, ?> builder(Graph<V, E> graph) {
    return new Builder<>(graph);
  }

  /**
   * @param visualizationModel the model to hold visualization state
   * @param <V> the vertex type
   * @param <E> the edge type
   * @return the builder
   */
  static <V, E> Builder<V, E, ?, ?> builder(VisualizationModel<V, E> visualizationModel) {
    return new Builder<>(visualizationModel);
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
  interface GraphMouse extends MouseListener, MouseMotionListener, MouseWheelListener {
    default void loadPlugins() {}
  }
}
