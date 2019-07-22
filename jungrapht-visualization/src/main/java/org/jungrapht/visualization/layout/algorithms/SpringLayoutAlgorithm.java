/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.layout.algorithms;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ConcurrentModificationException;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardSpringRepulsion;
import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SpringLayout package represents a visualization of a set of nodes. The SpringLayout, which is
 * initialized with a Graph, assigns X/Y locations to each node. When called <code>step()</code>,
 * the SpringLayout moves the visualization forward one step.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 */
public class SpringLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(SpringLayoutAlgorithm.class);
  protected double stretch = 0.70;
  protected Function<Object, Integer> lengthFunction;
  protected int repulsion_range_sq = 100 * 100;
  protected double force_multiplier = 1.0 / 3.0;
  boolean done = false;

  protected LoadingCache<N, SpringNodeData> springNodeData =
      CacheBuilder.newBuilder().build(CacheLoader.from(SpringNodeData::new));

  protected StandardSpringRepulsion.Builder repulsionContractBuilder;
  protected StandardSpringRepulsion repulsionContract;

  public static class Builder<N>
      extends AbstractIterativeLayoutAlgorithm.Builder<N, SpringLayoutAlgorithm<N>, Builder<N>> {
    private StandardSpringRepulsion.Builder repulsionContractBuilder =
        StandardSpringRepulsion.standardBuilder();
    private Function<Object, Integer> lengthFunction = n -> 30;

    public Builder<N> repulsionContractBuilder(
        StandardSpringRepulsion.Builder repulsionContractBuilder) {
      this.repulsionContractBuilder = repulsionContractBuilder;
      return this;
    }

    public Builder<N> withLengthFunction(Function<Object, Integer> lengthFunction) {
      this.lengthFunction = lengthFunction;
      return this;
    }

    public SpringLayoutAlgorithm<N> build() {
      return new SpringLayoutAlgorithm<>(this);
    }
  }

  public static <N> Builder<N> builder() {
    return new Builder<>();
  }

  protected SpringLayoutAlgorithm(Builder<N> builder) {
    super(builder);
    this.lengthFunction = builder.lengthFunction;
    this.repulsionContractBuilder = builder.repulsionContractBuilder;
  }

  /**
   * Override to create the BarnesHutOctTree
   *
   * @param layoutModel
   */
  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);

    // setting the layout model will build the BHQT if the builder is the
    // Optimized one
    repulsionContract =
        repulsionContractBuilder
            .nodeData(springNodeData)
            .layoutModel(layoutModel)
            .random(random)
            .build();
  }

  /** @return the current value for the stretch parameter */
  public double getStretch() {
    return stretch;
  }

  public void setStretch(double stretch) {
    this.stretch = stretch;
  }

  public int getRepulsionRange() {
    return (int) (Math.sqrt(repulsion_range_sq));
  }

  public void setRepulsionRange(int range) {
    this.repulsion_range_sq = range * range;
  }

  public double getForceMultiplier() {
    return force_multiplier;
  }

  public void setForceMultiplier(double force) {
    this.force_multiplier = force;
  }

  public void initialize() {}

  public void step() {
    this.repulsionContract.step();
    Graph<N, ?> graph = layoutModel.getGraph();
    try {
      for (N node : graph.vertexSet()) {
        SpringNodeData svd = springNodeData.getUnchecked(node);
        if (svd == null) {
          continue;
        }
        svd.dx /= 4;
        svd.dy /= 4;
        svd.edgedx = svd.edgedy = 0;
        svd.repulsiondx = svd.repulsiondy = 0;
      }
    } catch (ConcurrentModificationException cme) {
      step();
    }

    relaxEdges();
    repulsionContract.calculateRepulsion();
    moveNodes();
  }

  protected void relaxEdges() {
    Graph<N, Object> graph = (Graph<N, Object>) layoutModel.getGraph();
    try {
      for (Object edge : layoutModel.getGraph().edgeSet()) {
        N node1 = graph.getEdgeSource(edge);
        N node2 = graph.getEdgeTarget(edge);

        Point p1 = this.layoutModel.get(node1);
        Point p2 = this.layoutModel.get(node2);
        if (p1 == null || p2 == null) {
          continue;
        }
        double vx = p1.x - p2.x;
        double vy = p1.y - p2.y;
        double len = Math.sqrt(vx * vx + vy * vy);

        double desiredLen = lengthFunction.apply(edge);

        // round from zero, if needed [zero would be Bad.].
        len = (len == 0) ? .0001 : len;

        double f = force_multiplier * (desiredLen - len) / len;
        f = f * Math.pow(stretch, (graph.degreeOf(node1) + graph.degreeOf(node2) - 2));

        // the actual movement distance 'dx' is the force multiplied by the
        // distance to go.
        double dx = f * vx;
        double dy = f * vy;
        SpringNodeData v1D, v2D;
        v1D = springNodeData.getUnchecked(node1);
        v2D = springNodeData.getUnchecked(node2);
        v1D.edgedx += dx;
        v1D.edgedy += dy;
        v2D.edgedx += -dx;
        v2D.edgedy += -dy;
      }
    } catch (ConcurrentModificationException cme) {
      relaxEdges();
    }
  }

  protected void moveNodes() {
    Graph<N, ?> graph = layoutModel.getGraph();

    synchronized (layoutModel) {
      try {
        for (N node : graph.vertexSet()) {
          if (layoutModel.isLocked(node)) {
            continue;
          }
          SpringNodeData vd = springNodeData.getUnchecked(node);
          if (vd == null) {
            continue;
          }
          Point xyd = layoutModel.apply(node);
          double posX = xyd.x;
          double posY = xyd.y;

          vd.dx += vd.repulsiondx + vd.edgedx;
          vd.dy += vd.repulsiondy + vd.edgedy;
          // keeps nodes from moving any faster than 5 per time unit
          posX = posX + Math.max(-5, Math.min(5, vd.dx));
          posY = posY + Math.max(-5, Math.min(5, vd.dy));

          int width = layoutModel.getWidth();
          int height = layoutModel.getHeight();

          if (posX < 0) {
            posX = 0;
          } else if (posX > width) {
            posX = width;
          }
          if (posY < 0) {
            posY = 0;
          } else if (posY > height) {
            posY = height;
          }
          // after the bounds have been honored above, really set the location
          // in the layout model
          layoutModel.set(node, posX, posY);
        }
      } catch (ConcurrentModificationException cme) {
        moveNodes();
      }
    }
  }

  public static class SpringNodeData {
    protected double edgedx;
    protected double edgedy;
    public double repulsiondx;
    public double repulsiondy;

    /** movement speed, x */
    protected double dx;

    /** movement speed, y */
    protected double dy;

    @Override
    public String toString() {
      return "{"
          + "edge="
          + Point.of(edgedx, edgedy)
          + ", rep="
          + Point.of(repulsiondx, repulsiondy)
          + ", dx="
          + dx
          + ", dy="
          + dy
          + '}';
    }
  }

  /** @return false */
  public boolean done() {
    return this.done;
  }
}
