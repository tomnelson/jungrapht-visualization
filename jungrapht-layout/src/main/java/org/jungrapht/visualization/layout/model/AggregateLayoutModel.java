/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 *
 *
 */
package org.jungrapht.visualization.layout.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.jungrapht.visualization.layout.event.LayoutSizeChange;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.event.LayoutVertexPositionChange;
import org.jungrapht.visualization.layout.event.ModelChange;
import org.jungrapht.visualization.layout.event.ViewChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code LayoutModel} implementation that combines multiple other layoutModels so that they may
 * be manipulated as one layoutModel. The relaxer thread will step each layoutmodel's algorithm in
 * sequence.
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 */
public class AggregateLayoutModel<V> implements LayoutModel<V> {

  private static final Logger log = LoggerFactory.getLogger(AggregateLayoutModel.class);
  protected final LayoutModel<V> delegate;
  protected Map<LayoutModel<V>, Point> layouts = new HashMap<>();

  /**
   * Creates an instance backed by the specified {@code delegate}.
   *
   * @param delegate the layout to which this instance is delegating
   */
  public AggregateLayoutModel(LayoutModel<V> delegate) {
    this.delegate = delegate;
  }

  /**
   * Adds the passed layout as a sublayout, and specifies the center of where this sublayout should
   * appear.
   *
   * @param layoutModel the layout model to use as a sublayout
   * @param center the center of the coordinates for the sublayout model
   */
  public void put(LayoutModel<V> layoutModel, Point center) {
    if (log.isTraceEnabled()) {
      log.trace("put layout: {} at {}", layoutModel, center);
    }
    layouts.put(layoutModel, center);
    connectListeners(layoutModel);
  }

  private void connectListeners(LayoutModel<V> newLayoutModel) {
    for (LayoutStateChange.Listener layoutStateChangeListener :
        delegate.getLayoutStateChangeSupport().getLayoutStateChangeListeners()) {
      newLayoutModel
          .getLayoutStateChangeSupport()
          .addLayoutStateChangeListener(layoutStateChangeListener);
    }

    for (ModelChange.Listener changeListener :
        delegate.getModelChangeSupport().getModelChangeListeners()) {
      newLayoutModel.getModelChangeSupport().addModelChangeListener(changeListener);
    }
  }

  private void disconnectListeners(LayoutModel<V> newLayoutModel) {
    newLayoutModel.getLayoutStateChangeSupport().getLayoutStateChangeListeners().clear();
    newLayoutModel.getModelChangeSupport().getModelChangeListeners().clear();
  }

  /**
   * @param layout the layout whose center is to be returned
   * @return the center of the passed layout
   */
  public Point get(LayoutModel<V> layout) {
    return layouts.get(layout);
  }

  @Override
  public void accept(LayoutAlgorithm<V> layoutAlgorithm) {
    delegate.accept(layoutAlgorithm);
  }

  @Override
  public Map<V, Point> getLocations() {
    return delegate.getLocations();
  }

  @Override
  public void setInitialDimensionFunction(
      Function<Graph<V, ?>, Pair<Integer>> initialDimensionFunction) {
    delegate.setInitialDimensionFunction(initialDimensionFunction);
  }

  @Override
  public void setSize(int width, int height) {
    delegate.setSize(width, height);
  }

  @Override
  public void setPreferredSize(int width, int height) {
    delegate.setPreferredSize(width, height);
  }

  @Override
  public void stop() {
    delegate.stop();
    for (LayoutModel<V> childLayoutModel : layouts.keySet()) {
      childLayoutModel.stop();
    }
  }

  @Override
  public void setRelaxing(boolean relaxing) {
    delegate.setRelaxing(relaxing);
  }

  @Override
  public boolean isRelaxing() {
    return delegate.isRelaxing();
  }

  //  @Override
  //  public Future getTheFuture() {
  //    return delegate.getTheFuture();
  //  }

  @Override
  public void set(V vertex, Point location) {
    delegate.set(vertex, location);
  }

  @Override
  public void set(V vertex, double x, double y) {
    delegate.set(vertex, Point.of(x, y));
  }

  @Override
  public Point get(V vertex) {
    return delegate.get(vertex);
  }

  @Override
  public <E> Graph<V, E> getGraph() {
    return delegate.getGraph();
  }

  @Override
  public void setGraph(Graph<V, ?> graph) {
    delegate.setGraph(graph);
  }

  /**
   * Removes {@code layout} from this instance.
   *
   * @param layout the layout to remove
   */
  public void remove(LayoutModel<V> layout) {
    layouts.remove(layout);
  }

  /** Removes all layouts from this instance. */
  public void removeAll() {
    layouts.clear();
  }

  @Override
  public int getWidth() {
    return delegate.getWidth();
  }

  @Override
  public int getHeight() {
    return delegate.getHeight();
  }

  @Override
  public int getPreferredWidth() {
    return delegate.getPreferredWidth();
  }

  @Override
  public int getPreferredHeight() {
    return delegate.getPreferredHeight();
  }

  /**
   * @param vertex the vertex whose locked state is to be returned
   * @return true if v is locked in any of the layouts, and false otherwise
   */
  public boolean isLocked(V vertex) {
    for (LayoutModel<V> layoutModel : layouts.keySet()) {
      if (layoutModel.isLocked(vertex)) {
        return true;
      }
    }
    return delegate.isLocked(vertex);
  }

  /**
   * Locks this vertex in the main layout and in any sublayouts whose graph contains this vertex.
   *
   * @param vertex the vertex whose locked state is to be set
   * @param state {@code true} if the vertex is to be locked, and {@code false} if unlocked
   */
  public void lock(V vertex, boolean state) {
    for (LayoutModel<V> layoutModel : layouts.keySet()) {
      if (layoutModel.getGraph().containsVertex(vertex)) {
        layoutModel.lock(vertex, state);
      }
    }
    delegate.lock(vertex, state);
  }

  @Override
  public void lock(boolean locked) {
    delegate.lock(locked);
    for (LayoutModel model : layouts.keySet()) {
      model.lock(locked);
    }
  }

  @Override
  public boolean isLocked() {
    return delegate.isLocked();
  }

  public void setInitializer(Function<V, Point> initializer) {
    delegate.setInitializer(initializer);
  }

  @Override
  public void resizeToSurroundingRectangle() {
    delegate.resizeToSurroundingRectangle();
  }

  @Override
  public LayoutStateChange.Support getLayoutStateChangeSupport() {
    return delegate.getLayoutStateChangeSupport();
  }

  @Override
  public LayoutSizeChange.Support<V> getLayoutSizeChangeSupport() {
    return delegate.getLayoutSizeChangeSupport();
  }
  /**
   * Returns the location of the vertex. The location is specified first by the sublayouts, and then
   * by the base layout if no sublayouts operate on this vertex.
   *
   * @return the location of the vertex
   */
  public Point apply(V vertex) {
    for (LayoutModel<V> layoutModel : layouts.keySet()) {
      if (layoutModel.getGraph().containsVertex(vertex)) {
        Point center = layouts.get(layoutModel);
        // transform by the layout itself, but offset to the
        // center of the sublayout
        int width = layoutModel.getWidth();
        int height = layoutModel.getHeight();
        double deltaX = center.x - width / 2;
        double deltaY = center.y - height / 2;
        //        AffineTransform at =
        //            AffineTransform.getTranslateInstance(center.x - width / 2, center.y - height / 2);
        Point vertexCenter = layoutModel.apply(vertex);
        log.trace("sublayout center is {}", vertexCenter);
        double[] srcPoints = new double[] {vertexCenter.x, vertexCenter.y};
        double[] destPoints = new double[2];
        Point translatedCenter = vertexCenter.add(deltaX, deltaY);
        //        at.transform(srcPoints, 0, destPoints, 0, 1);
        return translatedCenter;
      }
    }
    return delegate.apply(vertex);
  }

  @Override
  public ModelChange.Support getModelChangeSupport() {
    return delegate.getModelChangeSupport();
  }

  @Override
  public ViewChange.Support getViewChangeSupport() {
    return delegate.getViewChangeSupport();
  }

  @Override
  public LayoutVertexPositionChange.Support<V> getLayoutVertexPositionSupport() {
    return delegate.getLayoutVertexPositionSupport();
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.Event<V> evt) {}

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.GraphEvent<V> evt) {}

  @Override
  public void appendLayoutModel(LayoutModel<V> layoutModel) {
    delegate.appendLayoutModel(layoutModel);
  }
}
