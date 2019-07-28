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

import com.google.common.collect.Maps;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.event.LayoutChange;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.event.LayoutVertexPositionChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that combines multiple other layouts so that they may be
 * manipulated as one layout. The relaxer thread will step each layout in sequence.
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 */
public class AggregateLayoutModel<V> implements LayoutModel<V> {

  private static final Logger log = LoggerFactory.getLogger(AggregateLayoutModel.class);
  protected final LayoutModel<V> delegate;
  protected Map<LayoutModel<V>, Point> layouts = Maps.newHashMap();

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

    for (LayoutChange.Listener changeListener :
        delegate.getLayoutChangeSupport().getLayoutChangeListeners()) {
      newLayoutModel.getLayoutChangeSupport().addLayoutChangeListener(changeListener);
    }
  }

  private void disconnectListeners(LayoutModel<V> newLayoutModel) {
    newLayoutModel.getLayoutStateChangeSupport().getLayoutStateChangeListeners().clear();
    newLayoutModel.getLayoutChangeSupport().getLayoutChangeListeners().clear();
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
  public void setSize(int width, int height) {
    delegate.setSize(width, height);
  }

  @Override
  public void stopRelaxer() {
    delegate.stopRelaxer();
    for (LayoutModel<V> childLayoutModel : layouts.keySet()) {
      childLayoutModel.stopRelaxer();
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

  @Override
  public CompletableFuture getTheFuture() {
    return delegate.getTheFuture();
  }

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
      if (layoutModel.getGraph().vertexSet().contains(vertex)) {
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
  public LayoutStateChange.Support getLayoutStateChangeSupport() {
    return delegate.getLayoutStateChangeSupport();
  }

  /**
   * Returns the location of the vertex. The location is specified first by the sublayouts, and then
   * by the base layout if no sublayouts operate on this vertex.
   *
   * @return the location of the vertex
   */
  public Point apply(V vertex) {
    for (LayoutModel<V> layoutModel : layouts.keySet()) {
      if (layoutModel.getGraph().vertexSet().contains(vertex)) {
        Point center = layouts.get(layoutModel);
        // transform by the layout itself, but offset to the
        // center of the sublayout
        int width = layoutModel.getWidth();
        int height = layoutModel.getHeight();
        AffineTransform at =
            AffineTransform.getTranslateInstance(center.x - width / 2, center.y - height / 2);
        Point vertexCenter = layoutModel.apply(vertex);
        log.trace("sublayout center is {}", vertexCenter);
        double[] srcPoints = new double[] {vertexCenter.x, vertexCenter.y};
        double[] destPoints = new double[2];
        at.transform(srcPoints, 0, destPoints, 0, 1);
        return Point.of(destPoints[0], destPoints[1]);
      }
    }
    return delegate.apply(vertex);
  }

  @Override
  public LayoutChange.Support getLayoutChangeSupport() {
    return delegate.getLayoutChangeSupport();
  }

  @Override
  public LayoutVertexPositionChange.Support<V> getLayoutVertexPositionSupport() {
    return delegate.getLayoutVertexPositionSupport();
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.Event<V> evt) {}

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.GraphEvent<V> evt) {}
}
