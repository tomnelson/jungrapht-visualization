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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.control.GraphMouseListener;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.MouseListenerTranslator;
import org.jungrapht.visualization.control.MultiSelectionStrategy;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * Adds mouse behaviors and tooltips to the graph visualization base class
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
class DefaultVisualizationViewer<V, E> extends DefaultVisualizationServer<V, E>
    implements VisualizationViewer<V, E> {

  protected DefaultVisualizationViewer(VisualizationViewer.Builder builder) {
    this(
        builder.graph,
        builder.visualizationModel,
        builder.initialDimensionFunction,
        builder.graphMouse,
        builder.multiSelectionStrategySupplier,
        builder.layoutAlgorithm,
        builder.layoutSize,
        builder.viewSize);
  }

  protected DefaultVisualizationViewer(
      Graph<V, E> graph,
      VisualizationModel<V, E> visualizationModel,
      Function<Graph<V, ?>, Pair<Integer>> initialDimensionFunction,
      GraphMouse graphMouse,
      Supplier<MultiSelectionStrategy> multiSelectionStrategySupplier,
      LayoutAlgorithm<V> layoutAlgorithm,
      Dimension layoutSize,
      Dimension viewSize) {
    super(
        graph, visualizationModel, initialDimensionFunction, layoutAlgorithm, layoutSize, viewSize);
    addMouseListener(requestFocusListener);
    setMultiSelectionStrategySupplier(multiSelectionStrategySupplier);
    setGraphMouse(graphMouse);
    if (graphMouse instanceof ModalGraphMouse) {
      addKeyListener(((ModalGraphMouse) graphMouse).getModeKeyListener());
    }
  }

  protected Function<V, String> vertexToolTipFunction;
  protected Function<E, String> edgeToolTipFunction;
  protected Function<MouseEvent, String> mouseEventToolTipFunction;

  /** provides MouseListener, MouseMotionListener, and MouseWheelListener events to the graph */
  protected GraphMouse graphMouse;

  protected Supplier<MultiSelectionStrategy> multiSelectionStrategySupplier;

  protected MouseListener requestFocusListener =
      new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          requestFocusInWindow();
        }
      };

  public void setMultiSelectionStrategySupplier(
      Supplier<MultiSelectionStrategy> multiSelectionStrategySupplier) {
    this.multiSelectionStrategySupplier = multiSelectionStrategySupplier;
  }

  public Supplier<MultiSelectionStrategy> getMultiSelectionStrategySupplier() {
    return this.multiSelectionStrategySupplier;
  }

  /**
   * sets and configures the GraphMouse. This will remove any previous GraphMouse.
   *
   * @param graphMouse new value
   */
  public void setGraphMouse(GraphMouse graphMouse) {
    this.graphMouse = graphMouse;
    if (!this.graphMouse.isPluginsLoaded()) {
      this.graphMouse.loadPlugins();
    }
    Arrays.stream(getMouseListeners())
        .filter(aMl -> aMl instanceof GraphMouse)
        .forEach(this::removeMouseListener);
    Arrays.stream(getMouseMotionListeners())
        .filter(aMml -> aMml instanceof GraphMouse)
        .forEach(this::removeMouseMotionListener);
    Arrays.stream(getMouseWheelListeners())
        .filter(aMwl -> aMwl instanceof GraphMouse)
        .forEach(this::removeMouseWheelListener);

    addMouseListener(graphMouse);
    addMouseMotionListener(graphMouse);
    addMouseWheelListener(graphMouse);
    if (graphMouse instanceof ModalGraphMouse) {
      addKeyListener(((ModalGraphMouse) graphMouse).getModeKeyListener());
    }
  }

  /** @return the current <code>GraphMouse</code> */
  public GraphMouse getGraphMouse() {
    return graphMouse;
  }

  /**
   * Adds a listener
   *
   * @param graphMouseListener the mouse listener to add
   */
  @Override
  public void addGraphMouseListener(GraphMouseListener<V> graphMouseListener) {
    addMouseListener(new MouseListenerTranslator<>(graphMouseListener, this));
  }

  /**
   * Override to request focus on mouse enter, if a key listener is added
   *
   * @see Component#addKeyListener(KeyListener)
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
    LayoutModel<V> layoutModel = getVisualizationModel().getLayoutModel();
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
}
