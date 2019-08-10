package org.jungrapht.visualization.layout.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.event.LayoutVertexPositionChange;
import org.jungrapht.visualization.layout.event.ModelChange;
import org.jungrapht.visualization.layout.event.ViewChange;

public class TestLayoutModel<V> implements LayoutModel<V> {

  Graph<V, ?> graph;
  int width;
  int height;
  int preferredWidth;
  int preferredHeight;
  Map<V, Point> locations = new HashMap<>();

  public TestLayoutModel(Graph<V, ?> graph, int width, int height) {
    this.graph = graph;
    this.width = this.preferredWidth = width;
    this.height = this.preferredHeight = height;
  }
  /** @return the width of the layout area */
  @Override
  public int getWidth() {
    return width;
  }

  /** @return the height of the layout area */
  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getPreferredWidth() {
    return width;
  }

  @Override
  public int getPreferredHeight() {
    return height;
  }

  /**
   * allow the passed LayoutAlgorithm to operate on this LayoutModel
   *
   * @param layoutAlgorithm the algorithm to apply to this model's Points
   */
  @Override
  public void accept(LayoutAlgorithm<V> layoutAlgorithm) {
    setSize(preferredWidth, preferredHeight);
    layoutAlgorithm.visit(this);
  }

  /**
   * @param width to set
   * @param helght to set
   */
  @Override
  public void setSize(int width, int helght) {
    this.width = width;
    this.height = helght;
  }

  /** stop a relaxer Thread from continuing to operate */
  @Override
  public void stopRelaxer() {}

  /**
   * indicates that there is a relaxer thread operating on this LayoutModel
   *
   * @param relaxing
   */
  @Override
  public void setRelaxing(boolean relaxing) {}

  /**
   * indicates that there is a relaxer thread operating on this LayoutModel
   *
   * @return relaxing
   */
  @Override
  public boolean isRelaxing() {
    return false;
  }

  /**
   * a handle to the relaxer thread; may be used to attach a process to run after relax is complete
   *
   * @return the CompletableFuture
   */
  @Override
  public CompletableFuture getTheFuture() {
    return null;
  }

  /**
   * @param vertex the vertex whose locked state is being queried
   * @return <code>true</code> if the position of vertex <code>v</code> is locked
   */
  @Override
  public boolean isLocked(V vertex) {
    return false;
  }

  /**
   * Changes the layout coordinates of {@code vertex} to {@code location}.
   *
   * @param vertex the vertex whose location is to be specified
   * @param location the coordinates of the specified location
   */
  @Override
  public void set(V vertex, Point location) {
    locations.put(vertex, location);
  }

  /**
   * Changes the layout coordinates of {@code vertex} to {@code x, y}.
   *
   * @param vertex the vertex to set location for
   * @param x coordinate to set
   * @param y coordinate to set
   */
  @Override
  public void set(V vertex, double x, double y) {
    locations.put(vertex, Point.of(x, y));
  }

  /**
   * @param vertex the vertex of interest
   * @return the Point location for vertex
   */
  @Override
  public Point get(V vertex) {
    return locations.get(vertex);
  }

  /** @return the {@code Graph} that this model is mediating */
  @Override
  public <E> Graph<V, E> getGraph() {
    return (Graph<V, E>) graph;
  }

  /** @param graph the {@code Graph} to set */
  @Override
  public void setGraph(Graph<V, ?> graph) {
    this.graph = graph;
  }

  @Override
  public void lock(V vertex, boolean locked) {}

  @Override
  public void lock(boolean locked) {}

  @Override
  public boolean isLocked() {
    return false;
  }

  @Override
  public void setInitializer(Function<V, Point> initializer) {}

  @Override
  public Point apply(V v) {
    return locations.get(v);
  }

  @Override
  public LayoutStateChange.Support getLayoutStateChangeSupport() {
    return null;
  }

  @Override
  public LayoutVertexPositionChange.Support<V> getLayoutVertexPositionSupport() {
    return null;
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.Event<V> evt) {}

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.GraphEvent<V> evt) {}

  @Override
  public ModelChange.Support getModelChangeSupport() {
    return null;
  }

  @Override
  public ViewChange.Support getViewChangeSupport() {
    return null;
  }
}
