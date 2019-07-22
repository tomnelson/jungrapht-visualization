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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.NetworkNodeAccessor;
import org.jungrapht.visualization.layout.util.RadiusNetworkNodeAccessor;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a self-organizing map layout algorithm, based on Meyer's self-organizing graph
 * methods.
 *
 * @author Yan Biao Boey
 */
public class ISOMLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(ISOMLayoutAlgorithm.class);

  public static class Builder<N> extends AbstractIterativeLayoutAlgorithm.Builder {

    public ISOMLayoutAlgorithm<N> build() {
      return new ISOMLayoutAlgorithm<>(this);
    }
  }

  public static Builder builder() {
    return new Builder<>();
  }

  protected ISOMLayoutAlgorithm(Builder builder) {
    super(builder);
  }

  private LoadingCache<N, ISOMNodeData> isomNodeData =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, ISOMNodeData>() {
                public ISOMNodeData load(N node) {
                  return new ISOMNodeData();
                }
              });

  protected int maxEpoch;
  protected int epoch;

  protected int radiusConstantTime;
  protected int radius;
  protected int minRadius;

  protected double adaption;
  protected double initialAdaption;
  protected double minAdaption;

  private NetworkNodeAccessor<N> elementAccessor;

  protected double coolingFactor;

  protected List<N> queue = new ArrayList<>();
  protected String status = null;

  /** @return the current number of epochs and execution status, as a string. */
  public String getStatus() {
    return status;
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    if (log.isTraceEnabled()) {
      log.trace("visiting {}", layoutModel);
    }

    super.visit(layoutModel);
    this.elementAccessor = new RadiusNetworkNodeAccessor<>();
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

    //Get closest node to random position
    N winner = elementAccessor.getNode(layoutModel, tempXYD.x, tempXYD.y);

    while (true) {
      try {
        for (N node : layoutModel.getGraph().vertexSet()) {
          ISOMNodeData ivd = getISOMNodeData(node);
          ivd.distance = 0;
          ivd.visited = false;
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    adjustNode(winner, tempXYD);
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

  private synchronized void adjustNode(N node, Point tempXYD) {
    Graph<N, ?> graph = layoutModel.getGraph();
    queue.clear();
    ISOMNodeData ivd = getISOMNodeData(node);
    ivd.distance = 0;
    ivd.visited = true;
    queue.add(node);
    N current;

    while (!queue.isEmpty()) {
      current = queue.remove(0);
      ISOMNodeData currData = getISOMNodeData(current);
      Point currXYData = layoutModel.apply(current);

      double dx = tempXYD.x - currXYData.x;
      double dy = tempXYD.y - currXYData.y;
      double factor = adaption / Math.pow(2, currData.distance);

      layoutModel.set(current, currXYData.x + (factor * dx), currXYData.y + (factor * dy));

      if (currData.distance < radius) {
        Collection<N> s = Graphs.neighborListOf(graph, current);
        while (true) {
          try {
            for (N child : s) {
              ISOMNodeData childData = getISOMNodeData(child);
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

  private ISOMNodeData getISOMNodeData(N node) {
    return isomNodeData.getUnchecked(node);
  }

  /**
   * Returns <code>true</code> if the node positions are no longer being updated. Currently <code>
   * ISOMLayout</code> stops updating node positions after a certain number of iterations have taken
   * place.
   *
   * @return <code>true</code> if the node position updates have stopped, <code>false</code>
   *     otherwise
   */
  public boolean done() {
    return epoch >= maxEpoch;
  }

  private static class ISOMNodeData {
    int distance;
    boolean visited;

    protected ISOMNodeData() {
      distance = 0;
      visited = false;
    }
  }

  /**
   * Resets the layout iteration count to 0, which allows the layout algorithm to continue updating
   * node positions.
   */
  public void reset() {
    epoch = 0;
  }
}
