/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.layout.algorithms;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ConcurrentModificationException;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Fruchterman-Reingold force-directed algorithm for node layout.
 *
 * <p>Behavior is determined by the following settable parameters:
 *
 * <ul>
 *   <li>attraction multiplier: how much edges try to keep their nodes together
 *   <li>repulsion multiplier: how much nodes try to push each other apart
 *   <li>maximum iterations: how many iterations this algorithm will use before stopping
 * </ul>
 *
 * Each of the first two defaults to 0.75; the maximum number of iterations defaults to 700.
 *
 * @see "Fruchterman and Reingold, 'Graph Drawing by Force-directed Placement'"
 * @see
 *     "http://i11www.ilkd.uni-karlsruhe.de/teaching/SS_04/visualisierung/papers/fruchterman91graph.pdf"
 * @author Scott White, Yan-Biao Boey, Danyel Fisher, Tom Nelson
 */
public class FRLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(FRLayoutAlgorithm.class);

  private double forceConstant;

  private double temperature;

  private int currentIteration;

  private int mMaxIterations = 700;

  protected LoadingCache<N, Point> frNodeData =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, Point>() {
                public Point load(N node) {
                  return Point.ORIGIN;
                }
              });

  private double attractionMultiplier = 0.75;

  protected double attractionConstant;

  private double repulsionMultiplier = 0.75;

  protected double repulsionConstant;

  private double max_dimension;

  private boolean initialized = false;

  protected StandardFRRepulsion.Builder repulsionContractBuilder;
  protected StandardFRRepulsion repulsionContract;

  public static class Builder<N>
      extends AbstractIterativeLayoutAlgorithm.Builder<N, FRLayoutAlgorithm<N>, Builder<N>> {
    private StandardFRRepulsion.Builder repulsionContractBuilder =
        new StandardFRRepulsion.Builder();

    public Builder<N> repulsionContractBuilder(
        StandardFRRepulsion.Builder repulsionContractBuilder) {
      this.repulsionContractBuilder = repulsionContractBuilder;
      return this;
    }

    public FRLayoutAlgorithm<N> build() {
      return new FRLayoutAlgorithm(this);
    }
  }

  public static <N> Builder<N> builder() {
    return new Builder<>();
  }

  protected FRLayoutAlgorithm(Builder<N> builder) {
    super(builder);
    this.repulsionContractBuilder = builder.repulsionContractBuilder;
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    if (log.isTraceEnabled()) {
      log.trace("visiting " + layoutModel);
    }
    super.visit(layoutModel);
    max_dimension = Math.max(layoutModel.getWidth(), layoutModel.getHeight());
    initialize();
    repulsionContract =
        repulsionContractBuilder
            .layoutModel(layoutModel)
            .nodeData(frNodeData)
            .repulsionConstant(repulsionConstant)
            .random(random)
            .build();
  }

  public void setAttractionMultiplier(double attraction) {
    this.attractionMultiplier = attraction;
  }

  public void setRepulsionMultiplier(double repulsion) {
    this.repulsionMultiplier = repulsion;
  }

  public void initialize() {
    doInit();
  }

  private void doInit() {
    Graph<N, ?> graph = layoutModel.getGraph();
    if (graph != null && graph.vertexSet().size() > 0) {
      currentIteration = 0;
      temperature = layoutModel.getWidth() / 10;

      forceConstant =
          Math.sqrt(layoutModel.getHeight() * layoutModel.getWidth() / graph.vertexSet().size());

      attractionConstant = attractionMultiplier * forceConstant;
      repulsionConstant = repulsionMultiplier * forceConstant;
      initialized = true;
    }
  }

  protected double EPSILON = 0.000001D;

  /**
   * Moves the iteration forward one notch, calculation attraction and repulsion between nodes and
   * edges and cooling the temperature.
   */
  @Override
  public synchronized void step() {
    repulsionContract.step();
    if (!initialized) {
      doInit();
    }
    Graph<N, ?> graph = layoutModel.getGraph();
    currentIteration++;

    // Calculate repulsion
    while (true) {

      try {
        repulsionContract.calculateRepulsion();
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }

    // Calculate attraction
    while (true) {
      try {
        for (Object edge : graph.edgeSet()) {
          calcAttraction(edge);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }

    while (true) {
      try {
        for (N node : graph.vertexSet()) {
          if (layoutModel.isLocked(node)) {
            continue;
          }
          calcPositions(node);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    cool();
  }

  protected synchronized void calcPositions(N node) {

    Point fvd = getFRData(node);
    if (fvd == null) {
      return;
    }
    Point xyd = layoutModel.apply(node);
    double deltaLength = Math.max(EPSILON, fvd.length());

    double positionX = xyd.x;
    double positionY = xyd.y;
    double newXDisp = fvd.x / deltaLength * Math.min(deltaLength, temperature);
    double newYDisp = fvd.y / deltaLength * Math.min(deltaLength, temperature);

    positionX += newXDisp;
    positionY += newYDisp;

    double borderWidth = layoutModel.getWidth() / 50.0;
    if (positionX < borderWidth) {
      positionX = borderWidth + random.nextDouble() * borderWidth * 2.0;
    } else if (positionX > layoutModel.getWidth() - borderWidth * 2) {
      positionX = layoutModel.getWidth() - borderWidth - random.nextDouble() * borderWidth * 2.0;
    }

    if (positionY < borderWidth) {
      positionY = borderWidth + random.nextDouble() * borderWidth * 2.0;
    } else if (positionY > layoutModel.getWidth() - borderWidth * 2) {
      positionY = layoutModel.getWidth() - borderWidth - random.nextDouble() * borderWidth * 2.0;
    }

    layoutModel.set(node, positionX, positionY);
  }

  protected void calcAttraction(Object edge) {
    Graph<N, Object> graph = (Graph<N, Object>) layoutModel.getGraph();
    N node1 = graph.getEdgeSource(edge);
    N node2 = graph.getEdgeTarget(edge);
    boolean v1_locked = layoutModel.isLocked(node1);
    boolean v2_locked = layoutModel.isLocked(node2);

    if (v1_locked && v2_locked) {
      // both locked, do nothing
      return;
    }
    Point p1 = layoutModel.apply(node1);
    Point p2 = layoutModel.apply(node2);
    if (p1 == null || p2 == null) {
      return;
    }
    double xDelta = p1.x - p2.x;
    double yDelta = p1.y - p2.y;

    double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

    double force = (deltaLength * deltaLength) / attractionConstant;

    Preconditions.checkState(
        !Double.isNaN(force), "Unexpected mathematical result in FRLayout:calcPositions [force]");

    double dx = (xDelta / deltaLength) * force;
    double dy = (yDelta / deltaLength) * force;
    if (!v1_locked) {
      Point fvd1 = getFRData(node1);
      frNodeData.put(node1, fvd1.add(-dx, -dy));
    }
    if (!v2_locked) {
      Point fvd2 = getFRData(node2);
      frNodeData.put(node2, fvd2.add(dx, dy));
    }
  }

  private void cool() {
    temperature *= (1.0 - currentIteration / (double) mMaxIterations);
  }

  public void setMaxIterations(int maxIterations) {
    mMaxIterations = maxIterations;
  }

  protected Point getFRData(N node) {
    return frNodeData.getUnchecked(node);
  }

  @Override
  public String toString() {
    return "FRLayoutAlgorithm{" + "repulsionContract=" + repulsionContract + '}';
  }

  /** @return true once the current iteration has passed the maximum count. */
  @Override
  public boolean done() {
    return currentIteration > mMaxIterations || temperature < 1.0 / max_dimension;
  }
}
