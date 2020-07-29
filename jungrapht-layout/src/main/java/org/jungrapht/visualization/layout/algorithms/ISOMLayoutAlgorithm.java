/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.layout.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RadiusVertexAccessor;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.jungrapht.visualization.layout.util.VertexAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a self-organizing map layout algorithm, based on Meyer's self-organizing graph
 * methods.
 *
 * @author Yan Biao Boey
 */
public class ISOMLayoutAlgorithm<V> extends AbstractIterativeLayoutAlgorithm<V>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(ISOMLayoutAlgorithm.class);

  public static class Builder<V, T extends ISOMLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends AbstractIterativeLayoutAlgorithm.Builder<V, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {

    public T build() {
      return (T) new ISOMLayoutAlgorithm<>(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  public ISOMLayoutAlgorithm() {
    this(ISOMLayoutAlgorithm.builder());
  }

  protected ISOMLayoutAlgorithm(Builder<V, ?, ?> builder) {
    super(builder);
  }

  private Map<V, ISOMVertexData> isomVertexData = new ConcurrentHashMap<>();

  private Function<V, ISOMVertexData> initializer = v -> new ISOMVertexData();

  protected int maxEpoch;
  protected int epoch;

  protected int radiusConstantTime;
  protected int radius;
  protected int minRadius;

  protected double adaption;
  protected double initialAdaption;
  protected double minAdaption;

  private VertexAccessor<V> elementAccessor;

  protected double coolingFactor;

  protected List<V> queue = new ArrayList<>();
  protected String status = null;

  /** @return the current number of epochs and execution status, as a string. */
  public String getStatus() {
    return status;
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    super.visit(layoutModel);
    if (log.isTraceEnabled()) {
      log.trace("visiting {}", layoutModel);
    }
    Graph<V, ?> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    this.elementAccessor = new RadiusVertexAccessor<>();
    initialize();
  }

  public void initialize() {
    layoutModel.setInitializer(
        new RandomLocationTransformer<>(layoutModel.getWidth(), layoutModel.getHeight()));

    maxEpoch = 2000;
    epoch = 1;

    radiusConstantTime = 100;
    radius = 5;
    minRadius = 1;

    initialAdaption = 90.0D / 100.0D;
    adaption = initialAdaption;
    minAdaption = 0;

    //factor = 0; //Will be set later on
    coolingFactor = 2;

    //temperature = 0.03;
    //initialJumpRadius = 100;
    //jumpRadius = initialJumpRadius;

    //delay = 100;
  }

  /** Advances the current positions of the graph elements. */
  @Override
  public void step() {
    status = "epoch: " + epoch + "; ";
    if (epoch < maxEpoch) {
      adjust();
      updateParameters();
      status += " status: running";
    } else {
      status += "adaption: " + adaption + "; ";
      status += "status: done";
      //			done = true;
    }
  }

  private synchronized void adjust() {
    double width = layoutModel.getWidth();
    double height = layoutModel.getHeight();
    //Generate random position in graph space
    // creates a new XY data location
    Point tempXYD = Point.of(10 + Math.random() * width, 10 + Math.random() * height);

    //Get closest vertex to random position
    V winner = elementAccessor.getVertex(layoutModel, tempXYD.x, tempXYD.y);

    while (true) {
      try {
        for (V vertex : layoutModel.getGraph().vertexSet()) {
          ISOMVertexData ivd = getISOMVertexData(vertex);
          ivd.distance = 0;
          ivd.visited = false;
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    adjustVertex(winner, tempXYD);
  }

  private synchronized void updateParameters() {
    epoch++;
    double factor = Math.exp(-1 * coolingFactor * (1.0 * epoch / maxEpoch));
    adaption = Math.max(minAdaption, factor * initialAdaption);
    //jumpRadius = (int) factor * jumpRadius;
    //temperature = factor * temperature;
    if ((radius > minRadius) && (epoch % radiusConstantTime == 0)) {
      radius--;
    }
  }

  private synchronized void adjustVertex(V vertex, Point tempXYD) {
    Graph<V, ?> graph = layoutModel.getGraph();
    queue.clear();
    ISOMVertexData ivd = getISOMVertexData(vertex);
    ivd.distance = 0;
    ivd.visited = true;
    queue.add(vertex);
    V current;

    while (!queue.isEmpty() && !cancelled) {
      current = queue.remove(0);
      ISOMVertexData currData = getISOMVertexData(current);
      Point currXYData = layoutModel.apply(current);

      double dx = tempXYD.x - currXYData.x;
      double dy = tempXYD.y - currXYData.y;
      double factor = adaption / Math.pow(2, currData.distance);

      layoutModel.set(current, currXYData.x + (factor * dx), currXYData.y + (factor * dy));

      if (currData.distance < radius) {
        Collection<V> s = Graphs.neighborListOf(graph, current);
        while (true) {
          try {
            for (V child : s) {
              ISOMVertexData childData = getISOMVertexData(child);
              if (childData != null && !childData.visited) {
                childData.visited = true;
                childData.distance = currData.distance + 1;
                queue.add(child);
              }
            }
            break;
          } catch (ConcurrentModificationException cme) {
          }
        }
      }
    }
  }

  private ISOMVertexData getISOMVertexData(V vertex) {
    return isomVertexData.computeIfAbsent(vertex, initializer);
  }

  /**
   * Returns <code>true</code> if the vertex positions are no longer being updated. Currently <code>
   * ISOMLayout</code> stops updating vertex positions after a certain number of iterations have
   * taken place.
   *
   * @return <code>true</code> if the vertex position updates have stopped, <code>false</code>
   *     otherwise
   */
  public boolean done() {
    if (cancelled) return true;
    boolean done = epoch >= maxEpoch;
    if (done) {
      runAfter();
    }
    return done;
  }

  private static class ISOMVertexData {
    int distance;
    boolean visited;

    protected ISOMVertexData() {
      distance = 0;
      visited = false;
    }
  }

  /**
   * Resets the layout iteration count to 0, which allows the layout algorithm to continue updating
   * vertex positions.
   */
  public void reset() {
    epoch = 0;
  }
}
