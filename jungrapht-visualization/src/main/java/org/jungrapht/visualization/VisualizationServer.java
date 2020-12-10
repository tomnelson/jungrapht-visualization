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

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.control.GraphElementAccessor;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.control.TransformSupport;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.jungrapht.visualization.layout.event.LayoutSizeChange;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.event.ModelChange;
import org.jungrapht.visualization.layout.event.ViewChange;
import org.jungrapht.visualization.renderers.ModalRenderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.spatial.Spatial;

/**
 * The interface for the visualization view
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public interface VisualizationServer<V, E>
    extends VisualizationComponent,
        ViewChange.Listener,
        ModelChange.Listener,
        ChangeListener,
        LayoutStateChange.Listener,
        LayoutSizeChange.Listener,
        RenderContextStateChange.Listener {

  /**
   * A builder for creating instances of a {@link VisualizationServer} with user defined properties
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  class Builder<V, E, T extends DefaultVisualizationServer<V, E>, B extends Builder<V, E, T, B>> {
    /** the {@link Graph} to be visualized */
    protected Graph<V, E> graph = GraphTypeBuilder.<V, E>directed().buildGraph();
    /** the bounds of the graph layout area */
    protected Dimension layoutSize = new Dimension(600, 600);
    /** the size of the viewer window */
    protected Dimension viewSize = new Dimension(600, 600);
    /** the algorithm to apply to position the vertices */
    protected LayoutAlgorithm<V> layoutAlgorithm = new LayoutAlgorithm.NoOp<>();

    /** the model to hold state for the visualization */
    protected VisualizationModel<V, E> visualizationModel;

    protected Function<Graph<V, ?>, Pair<Integer>> initialDimensionFunction;

    /** create an instance with no args */
    protected Builder() {
      PropertyLoader.load();
    }

    /**
     * create an instance of the builder with the passed {@link Graph}
     *
     * @param graph the graph to visualize
     */
    protected Builder(Graph<V, E> graph) {
      this.graph = graph;
    }

    /**
     * create an instance of the builder if the visualizationModel is null, a new one will be
     * created from the graph and layout size, both of which must not be null
     *
     * @param visualizationModel the model to hold visualization state
     */
    protected Builder(VisualizationModel<V, E> visualizationModel) {
      this.visualizationModel = visualizationModel;
    }

    /** @return this builder cast to type B */
    protected B self() {
      return (B) this;
    }

    /**
     * @param layoutSize the width height bounds of the graph layout area
     * @return this builder for method chaining
     */
    public B layoutSize(Dimension layoutSize) {
      this.layoutSize = layoutSize;
      return self();
    }

    /**
     * @param viewSize the preferred size of the view
     * @return this builder for method chaining
     */
    public B viewSize(Dimension viewSize) {
      this.viewSize = viewSize;
      return self();
    }

    /**
     * @param layoutAlgorithm the algorithm to apply to place the graph vertices
     * @return this builder for chaining
     */
    public B layoutAlgorithm(LayoutAlgorithm<V> layoutAlgorithm) {
      this.layoutAlgorithm = layoutAlgorithm;
      return self();
    }

    public B initialDimensionFunction(
        Function<Graph<V, ?>, Pair<Integer>> initialDimensionFunction) {
      this.initialDimensionFunction = initialDimensionFunction;
      return (B) this;
    }

    /** @return a new instance of a {@link DefaultVisualizationServer} */
    public T build() {
      return (T) new DefaultVisualizationServer<>(this);
    }
  }

  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder to create a VisualizationServer instance
   */
  static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  /**
   * @param graph the graph to visualize
   * @param <V> the vertex type
   * @param <E> the edge type
   * @return the builder
   */
  static <V, E> Builder<V, E, ?, ?> builder(Graph<V, E> graph) {
    return new Builder(graph);
  }

  /**
   * @param visualizationModel the visualization model
   * @param <V> the vertex type
   * @param <E> the edge type
   * @return the builder
   */
  static <V, E> Builder<V, E, ?, ?> builder(VisualizationModel<V, E> visualizationModel) {
    return new Builder(visualizationModel);
  }

  /**
   * Specify whether this class uses its offscreen image or not.
   *
   * @param doubleBuffered if true, then doubleBuffering in the superclass is set to 'false'
   */
  void setDoubleBuffered(boolean doubleBuffered);

  /** resets all transforms in the visualization */
  void reset();

  /**
   * Returns whether this class uses double buffering. The superclass will be the opposite state.
   *
   * @return the double buffered state
   */
  boolean isDoubleBuffered();

  Shape viewOnLayout();

  Spatial<V> getVertexSpatial();

  void setVertexSpatial(Spatial<V> spatial);

  Spatial<E> getEdgeSpatial();

  void setEdgeSpatial(Spatial<E> spatial);

  TransformSupport<V, E> getTransformSupport();

  void setTransformSupport(TransformSupport<V, E> transformSupport);

  /** @return the model. */
  VisualizationModel<V, E> getVisualizationModel();

  /** @param visualizationModel the model for this class to use */
  void setVisualizationModel(VisualizationModel<V, E> visualizationModel);

  /**
   * In response to changes from the model, repaint the view, then fire an event to any listeners.
   * Examples of listeners are the VisualizationScrollPane and the BirdsEyeVisualizationViewer
   *
   * @param e the change event
   */
  void stateChanged(ChangeEvent e);

  /** @return the renderer used by this instance. */
  ModalRenderer<V, E> getRenderer();

  /**
   * Makes the component visible if {@code aFlag} is true, or invisible if false.
   *
   * @param aFlag true iff the component should be visible
   * @see javax.swing.JComponent#setVisible(boolean)
   */
  void setVisible(boolean aFlag);

  /** @return the renderingHints */
  Map<Key, Object> getRenderingHints();

  /** @param renderingHints The renderingHints to set. */
  void setRenderingHints(Map<Key, Object> renderingHints);

  /** @param paintable The paintable to add. */
  void prependPreRenderPaintable(Paintable paintable);

  /** @param paintable The paintable to add. */
  void addPreRenderPaintable(Paintable paintable);

  /** @param paintable The paintable to remove. */
  void removePreRenderPaintable(Paintable paintable);

  /** @param paintable The paintable to add. */
  void addPostRenderPaintable(Paintable paintable);

  /** @param paintable The paintable to remove. */
  void removePostRenderPaintable(Paintable paintable);

  /**
   * Adds a <code>ChangeListener</code>.
   *
   * @param l the listener to be added
   */
  void addChangeListener(ChangeListener l);

  /**
   * Removes a ChangeListener.
   *
   * @param l the listener to be removed
   */
  void removeChangeListener(ChangeListener l);

  /**
   * Returns an array of all the <code>ChangeListener</code>s added with addChangeListener().
   *
   * @return all of the <code>ChangeListener</code>s added or an empty array if no listeners have
   *     been added
   */
  ChangeListener[] getChangeListeners();

  /**
   * Notifies all listeners that have registered interest for notification on this event type. The
   * event instance is lazily created.
   *
   * @see EventListenerList
   */
  void fireStateChanged();

  /** @return the vertex MutableSelectedState instance */
  MutableSelectedState<V> getSelectedVertexState();

  Set<V> getSelectedVertices();

  /** @return the edge MutableSelectedState instance */
  MutableSelectedState<E> getSelectedEdgeState();

  Set<E> getSelectedEdges();

  void setSelectedVertexState(MutableSelectedState<V> selectedVertexState);

  void setSelectedEdgeState(MutableSelectedState<E> selectedEdgeState);

  /** @return the GraphElementAccessor */
  GraphElementAccessor<V, E> getPickSupport();

  /** @param pickSupport The pickSupport to set. */
  void setPickSupport(GraphElementAccessor<V, E> pickSupport);

  /** @return the x,y coordinates of the view center */
  Point2D getCenter();

  /** @return the {@link RenderContext} used to draw the graph */
  RenderContext<V, E> getRenderContext();

  /** @param renderContext the {@link RenderContext} used to draw the graph */
  void setRenderContext(RenderContext<V, E> renderContext);

  void repaint();

  /** an interface for the preRender and postRender */
  interface Paintable {
    void paint(Graphics g);

    boolean useTransform();
  }

  /**
   * scale the graph layout to fit withon the view window
   *
   * @param scaler the {@link ScalingControl} to change the view scale
   */
  void scaleToLayout(ScalingControl scaler);

  /** scale the graph visualization to fit within the view window */
  void scaleToLayout();

  @Deprecated
  void resizeToLayout();

  /**
   * scale the graph layout to fit withon the view window
   *
   * @param scaler the {@link ScalingControl} to change the view scale
   */
  void scaleToLayout(ScalingControl scaler, boolean resizeToPoints);

  /** scale the graph visualization to fit within the view window */
  void scaleToLayout(boolean resizeToPoints);

  void setInitialDimensionFunction(Function<Graph<V, ?>, Pair<Integer>> initialDimensionFunction);
}
