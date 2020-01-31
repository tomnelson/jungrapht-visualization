/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
/*
 * Created on Dec 4, 2003
 */
package org.jungrapht.visualization.layout.algorithms;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.layout.algorithms.util.AfterRunnable;
import org.jungrapht.visualization.layout.algorithms.util.CircleLayoutReduceEdgeCrossing;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that positions vertices equally spaced on a regular circle.
 *
 * @author Masanori Harada
 * @author Tom Nelson - adapted to an algorithm
 */
public class CircleLayoutAlgorithm<V> implements LayoutAlgorithm<V>, AfterRunnable, Future {

  private static final Logger log = LoggerFactory.getLogger(CircleLayoutAlgorithm.class);

  protected static final String CIRCLE_REDUCE_EDGE_CROSSING = PREFIX + "circle.reduceEdgeCrossing";
  protected static final String CIRCLE_REDUCE_EDGE_CROSSING_MAX_EDGES =
      PREFIX + "circle.reduceEdgeCrossingMaxEdges";
  protected static final String CIRCLE_THREADED = PREFIX + "circle.threaded";

  protected double radius;
  protected boolean reduceEdgeCrossing;
  protected List<V> vertexOrderedList;
  protected Runnable after;
  private boolean threaded;
  CompletableFuture theFuture;
  protected int reduceEdgeCrossingMaxEdges;

  public static class Builder<V, T extends CircleLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected int radius;
    protected boolean reduceEdgeCrossing =
        Boolean.parseBoolean(System.getProperty(CIRCLE_REDUCE_EDGE_CROSSING, "true"));
    protected int reduceEdgeCrossingMaxEdges =
        Integer.getInteger(CIRCLE_REDUCE_EDGE_CROSSING_MAX_EDGES, 100);
    protected Runnable after = () -> {};
    protected boolean threaded = Boolean.parseBoolean(System.getProperty(CIRCLE_THREADED, "true"));

    B self() {
      return (B) this;
    }

    public B radius(int radius) {
      this.radius = radius;
      return self();
    }

    public B reduceEdgeCrossingMaxEdges(int reduceEdgeCrossingMaxEdges) {
      this.reduceEdgeCrossingMaxEdges = reduceEdgeCrossingMaxEdges;
      return self();
    }

    public B reduceEdgeCrossing(boolean reduceEdgeCrossing) {
      this.reduceEdgeCrossing = reduceEdgeCrossing;
      return self();
    }

    public B threaded(boolean threaded) {
      this.threaded = threaded;
      return self();
    }

    public B after(Runnable after) {
      this.after = after;
      return self();
    }

    public T build() {
      return (T) new CircleLayoutAlgorithm(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  protected CircleLayoutAlgorithm(Builder<V, ?, ?> builder) {
    this(
        builder.radius,
        builder.reduceEdgeCrossing,
        builder.reduceEdgeCrossingMaxEdges,
        builder.threaded,
        builder.after);
  }

  private CircleLayoutAlgorithm(
      int radius,
      boolean reduceEdgeCrossing,
      int reduceEdgeCrossingMaxEdges,
      boolean threaded,
      Runnable after) {
    this.radius = radius;
    this.reduceEdgeCrossing = reduceEdgeCrossing;
    this.reduceEdgeCrossingMaxEdges = reduceEdgeCrossingMaxEdges;
    this.threaded = threaded;
    this.after = after;
    this.reduceEdgeCrossingMaxEdges =
        Integer.getInteger(PREFIX + "circle.reduceEdgeCrossingMaxEdges", 100);
  }

  public CircleLayoutAlgorithm() {
    this(CircleLayoutAlgorithm.builder());
  }

  /** @return the radius of the circle. */
  public double getRadius() {
    return radius;
  }

  /**
   * Sets the radius of the circle. Must be called before {@code initialize()} is called.
   *
   * @param radius the radius of the circle
   */
  public void setRadius(double radius) {
    this.radius = radius;
  }

  private void computeVertexOrder(LayoutModel<V> layoutModel) {
    Graph<V, ?> graph = layoutModel.getGraph();
    if (reduceEdgeCrossing) {
      reduceEdgeCrossing = graph.edgeSet().size() < this.reduceEdgeCrossingMaxEdges;
    }
    if (this.reduceEdgeCrossing) {
      this.vertexOrderedList = new ArrayList<>();
      ReduceCrossingRunnable<V, ?> reduceCrossingRunnable =
          new ReduceCrossingRunnable<>(graph, this.vertexOrderedList);
      if (threaded) {
        theFuture =
            CompletableFuture.runAsync(reduceCrossingRunnable)
                .thenRun(
                    () -> {
                      log.trace("ReduceEdgeCrossing done");
                      layoutVertices(layoutModel);
                      this.run(); // run the after function
                      layoutModel.getViewChangeSupport().fireViewChanged();
                      // fire an event to say that the layout is done
                      layoutModel
                          .getLayoutStateChangeSupport()
                          .fireLayoutStateChanged(layoutModel, false);
                    });
      } else {
        reduceCrossingRunnable.run();
        layoutVertices(layoutModel);
        after.run();
        layoutModel.getViewChangeSupport().fireViewChanged();
        // fire an event to say that the layout is done
        layoutModel.getLayoutStateChangeSupport().fireLayoutStateChanged(layoutModel, false);
      }
    } else {
      this.vertexOrderedList = new ArrayList<>(graph.vertexSet());
      layoutVertices(layoutModel);
    }
    if (log.isTraceEnabled()) {
      log.trace(
          "crossing count {}",
          CircleLayoutReduceEdgeCrossing.countCrossings(graph, (V[]) vertexOrderedList.toArray()));
    }
  }

  /**
   * Sets the order of the vertices in the layout according to the ordering of {@code vertex_list}.
   *
   * @param vertexList a list specifying the ordering of the vertices
   */
  public void setVertexOrder(LayoutModel<V> layoutModel, List<V> vertexList) {
    Objects.requireNonNull(
        vertexList.containsAll(layoutModel.getGraph().vertexSet()),
        "Supplied list must include all vertices of the graph");
    this.vertexOrderedList = vertexList;
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    if (layoutModel != null) {
      computeVertexOrder(layoutModel);
    }
  }

  private void layoutVertices(LayoutModel<V> layoutModel) {
    double height = layoutModel.getHeight();
    double width = layoutModel.getWidth();

    if (radius <= 0) {
      radius = 0.45 * (Math.min(height, width));
    }

    int i = 0;
    for (V vertex : vertexOrderedList) {

      double angle = (2 * Math.PI * i) / vertexOrderedList.size();

      double posX = Math.cos(angle) * radius + width / 2;
      double posY = Math.sin(angle) * radius + height / 2;
      layoutModel.set(vertex, posX, posY);
      log.trace("set {} to {},{} ", vertex, posX, posY);

      i++;
    }
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (theFuture != null) {
      return theFuture.cancel(mayInterruptIfRunning);
    }
    return false;
  }

  @Override
  public boolean isCancelled() {
    if (theFuture != null) {
      return theFuture.isCancelled();
    }
    return false;
  }

  @Override
  public boolean isDone() {
    if (theFuture != null) {
      return theFuture.isDone();
    }
    return false;
  }

  @Override
  public Object get() throws InterruptedException, ExecutionException {
    if (theFuture != null) {
      return theFuture.get();
    }
    return null;
  }

  @Override
  public Object get(long l, TimeUnit timeUnit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return null;
  }

  @Override
  public void run() {
    after.run();
  }

  @Override
  public void setAfter(Runnable after) {
    this.after = after;
  }

  static class ReduceCrossingRunnable<V, E> implements Runnable {

    Graph<V, E> graph;
    private List<V> vertexOrderedList;

    ReduceCrossingRunnable(Graph<V, E> graph, List<V> vertexOrderList) {
      this.graph = graph;
      this.vertexOrderedList = vertexOrderList;
    }

    @Override
    public void run() {
      // is this a multicomponent graph?
      ConnectivityInspector<V, ?> connectivityInspector = new ConnectivityInspector<>(graph);
      List<Set<V>> componentVertices = connectivityInspector.connectedSets();
      List<V> vertexOrderedList = new ArrayList<>();
      if (componentVertices.size() > 1) {
        for (Set<V> vertexSet : componentVertices) {
          // get back the graph for these vertices
          Graph<V, E> subGraph = GraphTypeBuilder.forGraph(graph).buildGraph();
          vertexSet.forEach(subGraph::addVertex);
          for (V v : vertexSet) {
            // get neighbors
            Graphs.successorListOf(graph, v)
                .forEach(s -> subGraph.addEdge(v, s, graph.getEdge(v, s)));
            Graphs.predecessorListOf(graph, v)
                .forEach(p -> subGraph.addEdge(p, v, graph.getEdge(p, v)));
          }
          vertexOrderedList.addAll(
              new CircleLayoutReduceEdgeCrossing<>(subGraph).getVertexOrderedList());
        }
      } else {
        CircleLayoutReduceEdgeCrossing<V, ?> circleLayouts =
            new CircleLayoutReduceEdgeCrossing<>(graph);
        vertexOrderedList.addAll(circleLayouts.getVertexOrderedList());
      }
      this.vertexOrderedList.clear();
      this.vertexOrderedList.addAll(vertexOrderedList);
    }
  }
}
