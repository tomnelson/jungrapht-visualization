/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Dec 4, 2003
 */
package org.jungrapht.visualization.layout.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GreedyCycleRemoval;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@code Layout} suitable for tree-like directed acyclic graphs. Parts of it
 * will probably not terminate if the graph is cyclic! The layout will result in directed edges
 * pointing generally upwards. Any vertices with no successors are considered to be level 0, and
 * tend towards the top of the layout. Any vertex has a level one greater than the maximum level of
 * all its successors.
 *
 * @author John Yesberg
 */
public class DAGLayoutAlgorithm<V, E> extends SpringLayoutAlgorithm<V, E> {

  private static final Logger log = LoggerFactory.getLogger(DAGLayoutAlgorithm.class);
  /**
   * Each vertex has a minimumLevel. Any vertex with no successors has minimumLevel of zero. The
   * minimumLevel of any vertex must be strictly greater than the minimumLevel of its parents.
   * (vertex A is a parent of vertex B iff there is an edge from B to A.) Typically, a vertex will
   * have a minimumLevel which is one greater than the minimumLevel of its parent's. However, if the
   * vertex has two parents, its minimumLevel will be one greater than the maximum of the parents'.
   * We need to calculate the minimumLevel for each vertex. When we layout the graph, vertices
   * cannot be drawn any higher than the minimumLevel. The graphHeight of a graph is the greatest
   * minimumLevel that is used. We will modify the SpringLayout calculations so that vertices cannot
   * move above their assigned minimumLevel.
   */
  private Map<V, Number> minLevels = new HashMap<>();
  // Simpler than the "pair" technique.
  static int graphHeight;
  static int numRoots;
  final double SPACEFACTOR = 1.3;
  // How much space do we allow for additional floating at the bottom.
  final double LEVELATTRACTIONRATE = 0.8;
  protected Collection<E> feedbackArcs;

  /**
   * A bunch of parameters to help work out when to stop quivering.
   *
   * <p>If the MeanSquareVel(ocity) ever gets below the MSV_THRESHOLD, then we will start a final
   * cool-down phase of COOL_DOWN_INCREMENT increments. If the MeanSquareVel ever exceeds the
   * threshold, we will exit the cool down phase, and continue looking for another opportunity.
   */
  final double MSV_THRESHOLD = 10.0;

  double meanSquareVel;
  boolean stoppingIncrements = false;
  int incrementsLeft;
  final int COOL_DOWN_INCREMENTS = 200;

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  public static class Builder<
          V, E, T extends DAGLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>>
      extends SpringLayoutAlgorithm.Builder<V, E, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {

    public T build() {
      return (T) new DAGLayoutAlgorithm<>(this);
    }
  }

  protected DAGLayoutAlgorithm(Builder<V, E, ?, ?> builder) {
    super(builder);
  }

  public DAGLayoutAlgorithm() {
    this(builder());
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    super.visit(layoutModel);
    Graph<V, ?> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    initialize();
  }

  /**
   * Calculates the level of each vertex in the graph. Level 0 is allocated to each vertex with no
   * successors. Level n+1 is allocated to any vertex whose successors' maximum level is n.
   */
  public void setRoot() {
    Graph<V, ?> graph = layoutModel.getGraph();
    numRoots = 0;
    for (V vertex : graph.vertexSet()) {
      if (Graphs.successorListOf(graph, vertex).isEmpty()) {
        setRoot(vertex);
        numRoots++;
      }
    }
  }

  /**
   * Set vertex v to be level 0.
   *
   * @param vertex the vertex to set as root
   */
  public void setRoot(V vertex) {
    minLevels.put(vertex, 0);
    // set all the levels.
    propagateMinimumLevel(vertex);
  }

  /**
   * A recursive method for allocating the level for each vertex. Ensures that all predecessors of v
   * have a level which is at least one greater than the level of v.
   *
   * @param vertex the vertex whose minimum level is to be calculated
   */
  public void propagateMinimumLevel(V vertex) {
    Graph<V, ?> graph = layoutModel.getGraph();
    int level = minLevels.getOrDefault(vertex, 0).intValue();
    for (V child : Graphs.predecessorListOf(graph, vertex)) {
      int oldLevel, newLevel;
      Number o = minLevels.getOrDefault(child, 0);
      if (o != null) {
        oldLevel = o.intValue();
      } else {
        oldLevel = 0;
      }
      newLevel = Math.max(oldLevel, level + 1);
      minLevels.put(child, newLevel);

      if (newLevel > graphHeight) {
        graphHeight = newLevel;
      }
      propagateMinimumLevel(child);
    }
  }

  /**
   * Sets a random location for a vertex within the dimensions of the space.
   *
   * @param vertex the vertex whose position is to be set
   * @param coord the coordinates of the vertex once the position has been set
   */
  private void initializeLocation(V vertex, Point coord, int width, int height) {

    int level = minLevels.getOrDefault(vertex, 0).intValue();
    int minY = (int) (level * height / (graphHeight * SPACEFACTOR));
    double x = Math.random() * width;
    double y = Math.random() * (height - minY) + minY;
    layoutModel.set(vertex, x, y);
  }

  /** Had to override this one as well, to ensure that setRoot() is called. */
  @Override
  public void initialize() {
    super.initialize();
    Graph<V, E> graph = layoutModel.getGraph();
    GreedyCycleRemoval gcr = new GreedyCycleRemoval(graph);
    this.feedbackArcs = gcr.getFeedbackArcs();
    gcr.reverseFeedbackArcs();
    setRoot();
    gcr.reverseFeedbackArcs();
  }

  /**
   * Override the moveVertices() method from SpringLayout. The only change we need to make is to
   * make sure that vertices don't float higher than the minY coordinate, as calculated by their
   * minimumLevel.
   */
  @Override
  protected void moveVertices() {
    int width = layoutModel.getWidth();
    int height = layoutModel.getHeight();
    Graph<V, ?> graph = layoutModel.getGraph();
    double oldMSV = meanSquareVel;
    meanSquareVel = 0;

    synchronized (layoutModel) {
      for (V vertex : graph.vertexSet()) {
        if (layoutModel.isLocked(vertex)) {
          continue;
        }
        SpringVertexData vd = springVertexData.computeIfAbsent(vertex, v -> new SpringVertexData());
        Point xyd = layoutModel.apply(vertex);

        // (JY addition: three lines are new)
        int level = minLevels.getOrDefault(vertex, 0).intValue();
        int minY = (int) (level * height / (graphHeight * SPACEFACTOR));
        int maxY = level == 0 ? (int) (height / (graphHeight * SPACEFACTOR * 2)) : height;

        // JY added 2* - double the sideways repulsion.
        vd.dx += 2 * vd.repulsiondx + vd.edgedx;
        vd.dy += vd.repulsiondy + vd.edgedy;

        // JY Addition: Attract the vertex towards it's minimumLevel
        // height.
        double delta = xyd.y - minY;
        vd.dy -= delta * LEVELATTRACTIONRATE;
        if (level == 0) {
          vd.dy -= delta * LEVELATTRACTIONRATE;
        }
        // twice as much at the top.

        // JY addition:
        meanSquareVel += (vd.dx * vd.dx + vd.dy * vd.dy);

        double posX = xyd.x + Math.max(-5, Math.min(5, vd.dx));
        double posY = xyd.y + Math.max(-5, Math.min(5, vd.dy));

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

        // (JY addition: if there's only one root, anchor it in the
        // middle-top of the screen)
        if (numRoots == 1 && level == 0) {
          posX = width / 2;
        }
        setLocation(vertex, posX, posY);
      }
    }
    //System.out.println("MeanSquareAccel="+meanSquareVel);
    if (!stoppingIncrements && Math.abs(meanSquareVel - oldMSV) < MSV_THRESHOLD) {
      stoppingIncrements = true;
      incrementsLeft = COOL_DOWN_INCREMENTS;
    } else if (stoppingIncrements && Math.abs(meanSquareVel - oldMSV) <= MSV_THRESHOLD) {
      incrementsLeft--;
      if (incrementsLeft <= 0) {
        incrementsLeft = 0;
      }
    }
  }

  /** Override incrementsAreDone so that we can eventually stop. */
  @Override
  public boolean done() {
    boolean done = stoppingIncrements && incrementsLeft == 0;
    if (done) {
      runAfter();
    }
    return done;
  }

  /**
   * Override forceMove so that if someone moves a vertex, we can re-layout everything.
   *
   * @param picked the vertex whose location is to be set
   * @param x the x coordinate of the location to set
   * @param y the y coordinate of the location to set
   */
  public void setLocation(V picked, double x, double y) {
    //    Point coord = layoutModel.apply(picked);
    layoutModel.set(picked, x, y);
    stoppingIncrements = false;
  }

  /**
   * Override forceMove so that if someone moves a vertex, we can re-layout everything.
   *
   * @param picked the vertex whose location is to be set
   * @param p the location to set
   */
  public void setLocation(V picked, Point p) {
    setLocation(picked, p.x, p.y);
  }

  /**
   * Overridden relaxEdges. This one reduces the effect of edges between greatly different levels.
   */
  @Override
  protected void relaxEdges() {
    Graph<V, E> graph = layoutModel.getGraph();
    for (E edge : graph.edgeSet()) {
      V vertex1 = graph.getEdgeSource(edge);
      V vertex2 = graph.getEdgeTarget(edge);

      Point p1 = layoutModel.apply(vertex1);
      Point p2 = layoutModel.apply(vertex2);
      double vx = p1.x - p2.x;
      double vy = p1.y - p2.y;
      double len = Math.sqrt(vx * vx + vy * vy);

      // JY addition.
      int level1 = minLevels.getOrDefault(vertex1, 0).intValue();
      int level2 = minLevels.getOrDefault(vertex2, 0).intValue();

      double desiredLen = lengthFunction.apply(edge);

      // round from zero, if needed [zero would be Bad.].
      len = (len == 0) ? .0001 : len;

      // force factor: optimal length minus actual length,
      // is made smaller as the current actual length gets larger.
      // why?

      double f = force_multiplier * (desiredLen - len) / len;

      f = f * Math.pow(stretch / 100.0, (graph.degreeOf(vertex1) + graph.degreeOf(vertex2) - 2));

      // JY addition. If this is an edge which stretches a long way,
      // don't be so concerned about it.
      if (level1 != level2) {
        f = f / Math.pow(Math.abs(level2 - level1), 1.5);
      }

      // the actual movement distance 'dx' is the force multiplied by the
      // distance to go.
      double dx = f * vx;
      double dy = f * vy;
      SpringVertexData v1D, v2D;
      v1D = springVertexData.computeIfAbsent(vertex1, v -> new SpringVertexData());
      v2D = springVertexData.computeIfAbsent(vertex2, v -> new SpringVertexData());

      v1D.edgedx += dx;
      v1D.edgedy += dy;
      v2D.edgedx += -dx;
      v2D.edgedy += -dy;
    }
  }
}
