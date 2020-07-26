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

import static org.jungrapht.visualization.layout.model.LayoutModel.PREFIX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.layout.algorithms.util.AfterRunnable;
import org.jungrapht.visualization.layout.algorithms.util.CircleLayoutReduceEdgeCrossing;
import org.jungrapht.visualization.layout.algorithms.util.ExecutorConsumer;
import org.jungrapht.visualization.layout.algorithms.util.Threaded;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that positions vertices equally spaced on a regular circle.
 *
 * @author Masanori Harada
 * @author Tom Nelson - adapted to an algorithm
 */
public class CircleLayoutAlgorithm<V>
    implements LayoutAlgorithm<V>, AfterRunnable, Threaded, ExecutorConsumer, Future {

  private static final Logger log = LoggerFactory.getLogger(CircleLayoutAlgorithm.class);

  protected static final String CIRCLE_REDUCE_EDGE_CROSSING = PREFIX + "circle.reduceEdgeCrossing";
  protected static final String CIRCLE_REDUCE_EDGE_CROSSING_MAX_EDGES =
      PREFIX + "circle.reduceEdgeCrossingMaxEdges";
  protected static final String CIRCLE_THREADED = PREFIX + "circle.threaded";

  protected LayoutModel<V> layoutModel;
  protected Executor executor;
  protected double radius;
  protected boolean reduceEdgeCrossing;
  protected List<V> vertexOrderedList;
  protected Runnable after;
  private boolean threaded;
  CompletableFuture theFuture;
  protected int reduceEdgeCrossingMaxEdges;
  int crossingCount = -1;

  public static class Builder<V, T extends CircleLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected int radius;
    protected Executor executor;
    protected boolean reduceEdgeCrossing =
        Boolean.parseBoolean(System.getProperty(CIRCLE_REDUCE_EDGE_CROSSING, "true"));
    protected int reduceEdgeCrossingMaxEdges =
        Integer.getInteger(CIRCLE_REDUCE_EDGE_CROSSING_MAX_EDGES, 200);
    protected Runnable after = () -> {};
    protected boolean threaded = Boolean.parseBoolean(System.getProperty(CIRCLE_THREADED, "true"));

    B self() {
      return (B) this;
    }

    public B executor(Executor executor) {
      this.executor = executor;
      return self();
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
        builder.executor,
        builder.radius,
        builder.reduceEdgeCrossing,
        builder.reduceEdgeCrossingMaxEdges,
        builder.threaded,
        builder.after);
  }

  private CircleLayoutAlgorithm(
      Executor executor,
      int radius,
      boolean reduceEdgeCrossing,
      int reduceEdgeCrossingMaxEdges,
      boolean threaded,
      Runnable after) {
    this.executor = executor;
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

  @Override
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }

  @Override
  public Executor getExecutor() {
    return this.executor;
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
        if (executor != null) {
          theFuture =
              CompletableFuture.runAsync(reduceCrossingRunnable, executor)
                  .thenRun(
                      () -> {
                        log.trace("ReduceEdgeCrossing done");
                        layoutVertices(layoutModel);
                        runAfter(); // run the after function
                        layoutModel.getViewChangeSupport().fireViewChanged();
                        // fire an event to say that the layout is done
                        layoutModel
                            .getLayoutStateChangeSupport()
                            .fireLayoutStateChanged(layoutModel, false);
                      });
        } else {
          theFuture =
              CompletableFuture.runAsync(reduceCrossingRunnable)
                  .thenRun(
                      () -> {
                        log.trace("ReduceEdgeCrossing done");
                        layoutVertices(layoutModel);
                        runAfter(); // run the after function
                        layoutModel.getViewChangeSupport().fireViewChanged();
                        // fire an event to say that the layout is done
                        layoutModel
                            .getLayoutStateChangeSupport()
                            .fireLayoutStateChanged(layoutModel, false);
                      });
        }
      } else {
        reduceCrossingRunnable.run();
        layoutVertices(layoutModel);
        runAfter();
        layoutModel.getViewChangeSupport().fireViewChanged();
        // fire an event to say that the layout is done
        layoutModel.getLayoutStateChangeSupport().fireLayoutStateChanged(layoutModel, false);
      }
    } else {
      this.vertexOrderedList = new ArrayList<>(graph.vertexSet());
      layoutVertices(layoutModel);
      layoutModel.getLayoutStateChangeSupport().fireLayoutStateChanged(layoutModel, false);
    }
    if (log.isTraceEnabled()) {
      log.trace(
          "crossing count {}",
          CircleLayoutReduceEdgeCrossing.countCrossings(graph, (V[]) vertexOrderedList.toArray()));
    }
  }

  public int getCrossingCount() {
    if (this.crossingCount < 0) {
      this.crossingCount = countCrossings();
    }
    return this.crossingCount;
  }

  @Override
  public boolean isThreaded() {
    return this.threaded;
  }

  @Override
  public void setThreaded(boolean threaded) {
    this.threaded = threaded;
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
    Graph<V, ?> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    this.layoutModel = layoutModel;
    if (layoutModel != null) {
      computeVertexOrder(layoutModel);
    }
  }

  private void layoutVertices(LayoutModel<V> layoutModel) {
    double height = layoutModel.getHeight();
    double width = layoutModel.getWidth();

    if (radius <= 0) {
      radius = 0.35 * Math.max(width, height);
      //              0.45 * (Math.min(height, width));
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
    crossingCount = countCrossings();
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
    if (theFuture != null) {
      return theFuture.get(l, timeUnit);
    }
    return null;
  }

  @Override
  public void runAfter() {
    if (after != null) {
      after.run();
    }
  }

  @Override
  public void setAfter(Runnable after) {
    this.after = after;
  }

  static class ReduceCrossingRunnable<V, E> implements Runnable {

    Graph<V, E> graph;
    private List<V> vertexOrderedList;
    int edgeCrossCount = 0;

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
          CircleLayoutReduceEdgeCrossing<V, E> rec = new CircleLayoutReduceEdgeCrossing<>(subGraph);
          vertexOrderedList.addAll(rec.getVertexOrderedList());
        }
      } else {
        CircleLayoutReduceEdgeCrossing<V, ?> rec = new CircleLayoutReduceEdgeCrossing<>(graph);
        vertexOrderedList.addAll(rec.getVertexOrderedList());
      }
      this.vertexOrderedList.clear();
      this.vertexOrderedList.addAll(vertexOrderedList);
    }
  }

  public <E> int countCrossings() {
    if (vertexOrderedList.size() == 0) {
      return -1;
    }
    V[] vertices = (V[]) vertexOrderedList.toArray(new Object[0]);
    Map<V, Integer> vertexListPositions = new HashMap<>();
    Graph<V, E> graph = this.layoutModel.getGraph();
    IntStream.range(0, vertices.length).forEach(i -> vertexListPositions.put(vertices[i], i));
    int numberOfCrossings = 0;
    Set<E> openEdgeList = new LinkedHashSet<>();
    List<V> verticesSeen = new ArrayList<>();
    for (V v : vertices) {
      log.trace("for vertex {}", v);
      verticesSeen.add(v);
      // sort the incident edges....
      List<E> incidentEdges = new ArrayList<>(graph.edgesOf(v));
      incidentEdges.sort(
          (e, f) -> {
            V oppe = Graphs.getOppositeVertex(graph, e, v);
            V oppf = Graphs.getOppositeVertex(graph, f, v);
            int idxv = vertexListPositions.get(v);
            int idxe = vertexListPositions.get(oppe);
            int idxf = vertexListPositions.get(oppf);
            int deltae = idxv - idxe;
            if (deltae < 0) {
              deltae += vertices.length;
            }
            int deltaf = idxv - idxf;
            if (deltaf < 0) {
              deltaf += vertices.length;
            }
            return Integer.compare(deltae, deltaf);
          });

      for (E e : incidentEdges) {
        V opposite = Graphs.getOppositeVertex(graph, e, v);
        if (!verticesSeen.contains(opposite)) {
          // e is an open edge
          openEdgeList.add(e);
        } else {
          openEdgeList.remove(e);
          for (int i = verticesSeen.indexOf(opposite) + 1; i < verticesSeen.indexOf(v); i++) {
            V tween = verticesSeen.get(i);
            numberOfCrossings +=
                graph.edgesOf(tween).stream().filter(openEdgeList::contains).count();
            log.trace("numberOfCrossings now {}", numberOfCrossings);
          }
        }
        log.trace("added edge {}", e);
      }
    }
    return numberOfCrossings;
  }

  @Override
  public boolean constrained() {
    return false;
  }
}
