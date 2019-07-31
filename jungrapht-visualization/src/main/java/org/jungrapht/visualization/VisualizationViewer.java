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

import static org.jungrapht.visualization.MultiLayerTransformer.Layer;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.function.Function;
import javax.swing.ToolTipManager;
import org.jgrapht.Graph;
import org.jungrapht.visualization.control.GraphMouseListener;
import org.jungrapht.visualization.control.MouseListenerTranslator;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * Adds mouse behaviors and tooltips to the graph visualization base class
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class VisualizationViewer<V, E> extends BasicVisualizationServer<V, E> {

  public static class Builder<
          V, E, T extends VisualizationViewer<V, E>, B extends Builder<V, E, T, B>>
      extends BasicVisualizationServer.Builder<V, E, T, B> {

    protected Builder(Graph<V, E> graph) {
      super(graph);
    }

    protected Builder(VisualizationModel<V, E> visualizationModel) {
      super(visualizationModel);
    }

    public T build() {
      return (T) new VisualizationViewer<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder(Graph<V, E> graph) {
    return new Builder(graph);
  }

  public static <V, E> Builder<V, E, ?, ?> builder(VisualizationModel<V, E> visualizationModel) {
    return new Builder(visualizationModel);
  }

  protected VisualizationViewer(Builder<V, E, ?, ?> builder) {
    this(
        builder.graph,
        builder.visualizationModel,
        builder.layoutAlgorithm,
        builder.layoutSize,
        builder.viewSize);
  }

  protected VisualizationViewer(
      Graph<V, E> graph,
      VisualizationModel<V, E> visualizationModel,
      LayoutAlgorithm<V> layoutAlgorithm,
      Dimension layoutSize,
      Dimension viewSize) {
    super(graph, visualizationModel, layoutAlgorithm, layoutSize, viewSize);
    addMouseListener(requestFocusListener);
  }

  protected Function<V, String> vertexToolTipFunction;
  protected Function<E, String> edgeToolTipFunction;
  protected Function<MouseEvent, String> mouseEventToolTipFunction;

  /** provides MouseListener, MouseMotionListener, and MouseWheelListener events to the graph */
  protected GraphMouse graphMouse;

  protected MouseListener requestFocusListener =
      new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          requestFocusInWindow();
        }
      };

  /**
   * a setter for the GraphMouse. This will remove any previous GraphMouse (including the one that
   * is added in the initMouseClicker method.
   *
   * @param graphMouse new value
   */
  public void setGraphMouse(GraphMouse graphMouse) {
    this.graphMouse = graphMouse;
    MouseListener[] ml = getMouseListeners();
    for (MouseListener aMl : ml) {
      if (aMl instanceof GraphMouse) {
        removeMouseListener(aMl);
      }
    }
    MouseMotionListener[] mml = getMouseMotionListeners();
    for (MouseMotionListener aMml : mml) {
      if (aMml instanceof GraphMouse) {
        removeMouseMotionListener(aMml);
      }
    }
    MouseWheelListener[] mwl = getMouseWheelListeners();
    for (MouseWheelListener aMwl : mwl) {
      if (aMwl instanceof GraphMouse) {
        removeMouseWheelListener(aMwl);
      }
    }
    addMouseListener(graphMouse);
    addMouseMotionListener(graphMouse);
    addMouseWheelListener(graphMouse);
  }

  /** @return the current <code>GraphMouse</code> */
  public GraphMouse getGraphMouse() {
    return graphMouse;
  }

  /**
   * This is the interface for adding a mouse listener. The GEL will be called back with mouse
   * clicks on vertices.
   *
   * @param gel the mouse listener to add
   */
  public void addGraphMouseListener(GraphMouseListener<V> gel) {
    addMouseListener(new MouseListenerTranslator<>(gel, this));
  }

  /**
   * Override to request focus on mouse enter, if a key listener is added
   *
   * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
   */
  @Override
  public synchronized void addKeyListener(KeyListener l) {
    super.addKeyListener(l);
  }

  /** @param edgeToolTipFunction the edgeToolTipFunction to set */
  public void setEdgeToolTipFunction(Function<E, String> edgeToolTipFunction) {
    this.edgeToolTipFunction = edgeToolTipFunction;
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /** @param mouseEventToolTipFunction the mouseEventToolTipFunction to set */
  public void setMouseEventToolTipFunction(Function<MouseEvent, String> mouseEventToolTipFunction) {
    this.mouseEventToolTipFunction = mouseEventToolTipFunction;
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /** @param vertexToolTipFunction the vertexToolTipFunction to set */
  public void setVertexToolTipFunction(Function<V, String> vertexToolTipFunction) {
    this.vertexToolTipFunction = vertexToolTipFunction;
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /** called by the superclass to display tooltips */
  public String getToolTipText(MouseEvent event) {
    LayoutModel<V> layoutModel = getModel().getLayoutModel();
    Point2D p = null;
    if (vertexToolTipFunction != null) {
      p = getTransformSupport().inverseTransform(this, event.getPoint());
      V vertex = getPickSupport().getVertex(layoutModel, p.getX(), p.getY());
      if (vertex != null) {
        return vertexToolTipFunction.apply(vertex);
      }
    }
    if (edgeToolTipFunction != null) {
      if (p == null) {
        p = renderContext.getMultiLayerTransformer().inverseTransform(Layer.VIEW, event.getPoint());
      }
      E edge = getPickSupport().getEdge(layoutModel, p.getX(), p.getY());
      if (edge != null) {
        return edgeToolTipFunction.apply(edge);
      }
    }
    if (mouseEventToolTipFunction != null) {
      return mouseEventToolTipFunction.apply(event);
    }
    return super.getToolTipText(event);
  }

  /**
   * a convenience type to represent a class that processes all types of mouse events for the graph
   */
  public interface GraphMouse extends MouseListener, MouseMotionListener, MouseWheelListener {}
}
