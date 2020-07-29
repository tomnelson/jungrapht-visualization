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

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Kamada-Kawai algorithm for vertex layout. Does not respect filter calls, and
 * sometimes crashes when the view changes to it.
 *
 * @see "Tomihisa Kamada and Satoru Kawai: An algorithm for drawing general indirect graphs.
 *     Information Processing Letters 31(1):7-15, 1989"
 * @see "Tomihisa Kamada: On visualization of abstract objects and relations. Ph.D. dissertation,
 *     Dept. of Information Science, Univ. of Tokyo, Dec. 1988."
 * @author Masanori Harada
 * @author Tom Nelson
 */
public class KKLayoutAlgorithm<V> extends AbstractIterativeLayoutAlgorithm<V>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(KKLayoutAlgorithm.class);

  public static class Builder<V, T extends KKLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends AbstractIterativeLayoutAlgorithm.Builder<V, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected int maxIterations = 2000;
    protected boolean adjustForGravity = true;
    protected boolean exchangeVertices = true;

    public B maxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
      return self();
    }

    public B adjustForGravity(boolean adjustForGravity) {
      this.adjustForGravity = adjustForGravity;
      return self();
    }

    public B exchangeVertices(boolean exchangeVertices) {
      this.exchangeVertices = exchangeVertices;
      return self();
    }

    public T build() {
      return (T) new KKLayoutAlgorithm<>(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  private double EPSILON = 0.1d;

  private int currentIteration;
  private int maxIterations;
  private String status = "KKLayout";

  private double L; // the ideal length of an edge
  private double K = 1; // arbitrary const number
  private double[][] dm; // distance matrix

  private boolean adjustForGravity = true;
  private boolean exchangevertices = true;

  private V[] vertices;
  private Point[] xydata;

  /** Retrieves graph distances between vertices of the visible graph */
  protected Map<Pair<V>, Integer> distance;

  /**
   * The diameter of the visible graph. In other words, the maximum over all pairs of vertices of
   * the length of the shortest path between a and bf the visible graph.
   */
  protected double diameter;

  /** A multiplicative factor which partly specifies the "preferred" length of an edge (L). */
  private double length_factor = 0.9;

  /**
   * A multiplicative factor which specifies the fraction of the graph's diameter to be used as the
   * inter-vertex distance between disconnected vertices.
   */
  private double disconnected_multiplier = 0.5;

  public KKLayoutAlgorithm() {
    this(KKLayoutAlgorithm.builder());
  }

  protected KKLayoutAlgorithm(Builder<V, ?, ?> builder) {
    super(builder);
    this.maxIterations = builder.maxIterations;
    this.adjustForGravity = builder.adjustForGravity;
    this.exchangevertices = builder.exchangeVertices;
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    super.visit(layoutModel);
    Graph<V, ?> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    if (graph != null) {
      this.distance = getDistances(graph);
    }
    initialize();
  }

  private Map<Pair<V>, Integer> getDistances(Graph<V, ?> graph) {

    DijkstraShortestPath<V, ?> dijkstra = new DijkstraShortestPath<>(graph);
    Map<Pair<V>, Integer> distanceMap = new HashMap<>();
    for (V vertex : graph.vertexSet()) {

      ShortestPathAlgorithm.SingleSourcePaths<V, ?> distances = dijkstra.getPaths(vertex);
      for (V n : graph.vertexSet()) {
        GraphPath<V, ?> graphPath = distances.getPath(n);
        if (graphPath != null && graphPath.getWeight() != 0) {
          distanceMap.put(Pair.of(vertex, n), (int) graphPath.getWeight());
        }
      }
    }
    return distanceMap;
  }

  /**
   * @param length_factor a multiplicative factor which partially specifies the preferred length of
   *     an edge
   */
  public void setLengthFactor(double length_factor) {
    this.length_factor = length_factor;
  }

  /**
   * @param disconnected_multiplier a multiplicative factor that specifies the fraction of the
   *     graph's diameter to be used as the inter-vertex distance between disconnected vertices
   */
  public void setDisconnectedDistanceMultiplier(double disconnected_multiplier) {
    this.disconnected_multiplier = disconnected_multiplier;
  }

  /** @return a string with information about the current status of the algorithm. */
  public String getStatus() {
    return status + layoutModel.getWidth() + " " + layoutModel.getHeight();
  }

  public void setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
  }

  /** @return true if the current iteration has passed the maximum count. */
  public boolean done() {
    if (cancelled) return true;
    boolean done = currentIteration > maxIterations;
    if (done) {
      runAfter();
    }
    return done;
  }

  @SuppressWarnings("unchecked")
  public void initialize() {
    currentIteration = 0;
    Graph<V, ?> graph = layoutModel.getGraph();
    // KKLayoutAlgorithm will fail if all vertices start at the same location
    layoutModel.setInitializer(
        new RandomLocationTransformer<>(
            layoutModel.getWidth(), layoutModel.getHeight(), graph.vertexSet().size()));
    if (layoutModel != null) {

      double height = layoutModel.getHeight();
      double width = layoutModel.getWidth();

      int n = graph.vertexSet().size();
      dm = new double[n][n];
      vertices = (V[]) graph.vertexSet().toArray();
      xydata = new Point[n];

      // assign IDs to all visible vertices
      while (true) {
        try {
          int index = 0;
          for (V vertex : graph.vertexSet()) {
            Point xyd = layoutModel.apply(vertex);
            vertices[index] = vertex;
            xydata[index] = xyd;
            index++;
          }
          break;
        } catch (ConcurrentModificationException cme) {
        }
      }

      diameter = diameter(graph, distance, true);
      log.trace("using diameter {}", diameter);
      //      if (diameter == 0) diameter = 2;

      double L0 = Math.min(height, width);
      L = (L0 / diameter) * length_factor; // length_factor used to be hardcoded to 0.9
      //L = 0.75 * Math.sqrt(height * width / n);

      for (int i = 0; i < n - 1; i++) {
        for (int j = i + 1; j < n; j++) {
          Number d_ij = distance.get(Pair.of(vertices[i], vertices[j]));
          log.trace("distance from " + i + " to " + j + " is " + d_ij);

          Number d_ji = distance.get(Pair.of(vertices[j], vertices[i]));
          log.trace("distance from " + j + " to " + i + " is " + d_ji);

          double dist = diameter * disconnected_multiplier;
          log.trace("dist:" + dist);
          if (d_ij != null && d_ij.intValue() < Integer.MAX_VALUE) {
            dist = Math.min(d_ij.doubleValue(), dist);
          }
          if (d_ji != null && d_ji.intValue() < Integer.MAX_VALUE) {
            dist = Math.min(d_ji.doubleValue(), dist);
          }
          dm[i][j] = dm[j][i] = dist;
        }
      }
    }
  }

  @Override
  public void step() {
    if (cancelled) {
      return;
    }
    Graph<V, ?> graph = layoutModel.getGraph();
    currentIteration++;
    double energy;
    int n = graph.vertexSet().size();
    if (n == 0) {
      return;
    }

    double maxDeltaM = 0;
    int pm = -1; // the vertex having max deltaM
    for (int i = 0; i < n; i++) {
      if (layoutModel.isLocked(vertices[i])) {
        continue;
      }
      double deltam = calcDeltaM(i);

      if (maxDeltaM < deltam) {
        maxDeltaM = deltam;
        pm = i;
      }
    }
    if (pm == -1) {
      return;
    }

    for (int i = 0; i < 100; i++) {
      double[] dxy = calcDeltaXY(pm);
      xydata[pm] = Point.of(xydata[pm].x + dxy[0], xydata[pm].y + dxy[1]);
      double deltam = calcDeltaM(pm);
      if (deltam < EPSILON) {
        break;
      }
    }

    if (adjustForGravity) {
      adjustForGravity();
    }

    if (exchangevertices && maxDeltaM < EPSILON) {
      energy = calcEnergy();
      for (int i = 0; i < n - 1; i++) {
        if (layoutModel.isLocked(vertices[i])) {
          continue;
        }
        for (int j = i + 1; j < n; j++) {
          if (layoutModel.isLocked(vertices[j])) {
            continue;
          }
          double xenergy = calcEnergyIfExchanged(i, j);
          if (energy > xenergy) {
            double sx = xydata[i].x;
            double sy = xydata[i].y;
            xydata[i] = Point.of(xydata[j].x, xydata[j].y);
            xydata[j] = Point.of(sx, sy);
            return;
          }
        }
      }
    }
  }

  /** Shift all vertices so that the center of gravity is located at the center of the screen. */
  public void adjustForGravity() {
    double height = layoutModel.getHeight();
    double width = layoutModel.getWidth();
    double gx = 0;
    double gy = 0;
    for (Point aXydata : xydata) {
      gx += aXydata.x;
      gy += aXydata.y;
    }
    gx /= xydata.length;
    gy /= xydata.length;
    double diffx = width / 2 - gx;
    double diffy = height / 2 - gy;
    for (int i = 0; i < xydata.length && !cancelled; i++) {
      xydata[i] = xydata[i].add(diffx, diffy);
      layoutModel.set(vertices[i], xydata[i]);
    }
  }

  public void setAdjustForGravity(boolean on) {
    adjustForGravity = on;
  }

  public boolean getAdjustForGravity() {
    return adjustForGravity;
  }

  /**
   * Enable or disable the local minimum escape technique by exchanging vertices.
   *
   * @param on iff the local minimum escape technique is to be enabled
   */
  public void setExchangevertices(boolean on) {
    exchangevertices = on;
  }

  public boolean getExchangevertices() {
    return exchangevertices;
  }

  /** Determines a step to new position of the vertex m. */
  private double[] calcDeltaXY(int m) {
    double dE_dxm = 0;
    double dE_dym = 0;
    double d2E_d2xm = 0;
    double d2E_dxmdym = 0;
    double d2E_dymdxm = 0;
    double d2E_d2ym = 0;

    for (int i = 0; i < vertices.length; i++) {
      if (i != m) {

        double dist = dm[m][i];
        double l_mi = L * dist;
        double k_mi = K / (dist * dist);
        double dx = xydata[m].x - xydata[i].x;
        double dy = xydata[m].y - xydata[i].y;
        double d = Math.sqrt(dx * dx + dy * dy);
        double ddd = d * d * d;

        dE_dxm += k_mi * (1 - l_mi / d) * dx;
        dE_dym += k_mi * (1 - l_mi / d) * dy;
        d2E_d2xm += k_mi * (1 - l_mi * dy * dy / ddd);
        d2E_dxmdym += k_mi * l_mi * dx * dy / ddd;
        d2E_d2ym += k_mi * (1 - l_mi * dx * dx / ddd);
      }
    }
    // d2E_dymdxm equals to d2E_dxmdym.
    d2E_dymdxm = d2E_dxmdym;

    double denomi = d2E_d2xm * d2E_d2ym - d2E_dxmdym * d2E_dymdxm;
    double deltaX = (d2E_dxmdym * dE_dym - d2E_d2ym * dE_dxm) / denomi;
    double deltaY = (d2E_dymdxm * dE_dxm - d2E_d2xm * dE_dym) / denomi;
    return new double[] {deltaX, deltaY};
  }

  /** Calculates the gradient of energy function at the vertex m. */
  private double calcDeltaM(int m) {
    double dEdxm = 0;
    double dEdym = 0;
    for (int i = 0; i < vertices.length; i++) {
      if (i != m) {
        double dist = dm[m][i];
        double l_mi = L * dist;
        double k_mi = K / (dist * dist);

        double dx = xydata[m].x - xydata[i].x;
        double dy = xydata[m].y - xydata[i].y;
        double d = Math.sqrt(dx * dx + dy * dy);

        double common = k_mi * (1 - l_mi / d);
        dEdxm += common * dx;
        dEdym += common * dy;
      }
    }
    return Math.sqrt(dEdxm * dEdxm + dEdym * dEdym);
  }

  /** Calculates the energy function E. */
  private double calcEnergy() {
    double energy = 0;
    for (int i = 0; i < vertices.length - 1; i++) {
      for (int j = i + 1; j < vertices.length; j++) {
        double dist = dm[i][j];
        double l_ij = L * dist;
        double k_ij = K / (dist * dist);
        double dx = xydata[i].x - xydata[j].x;
        double dy = xydata[i].y - xydata[j].y;
        double d = Math.sqrt(dx * dx + dy * dy);

        energy += k_ij / 2 * (dx * dx + dy * dy + l_ij * l_ij - 2 * l_ij * d);
      }
    }
    return energy;
  }

  /** Calculates the energy function E as if positions of the specified vertices are exchanged. */
  private double calcEnergyIfExchanged(int p, int q) {
    if (p >= q) {
      throw new RuntimeException("p should be < q");
    }
    double energy = 0; // < 0
    for (int i = 0; i < vertices.length - 1; i++) {
      for (int j = i + 1; j < vertices.length; j++) {
        int ii = i;
        int jj = j;
        if (i == p) {
          ii = q;
        }
        if (j == q) {
          jj = p;
        }

        double dist = dm[i][j];
        double l_ij = L * dist;
        double k_ij = K / (dist * dist);
        double dx = xydata[ii].x - xydata[jj].x;
        double dy = xydata[ii].y - xydata[jj].y;
        double d = Math.sqrt(dx * dx + dy * dy);

        energy += k_ij / 2 * (dx * dx + dy * dy + l_ij * l_ij - 2 * l_ij * d);
      }
    }
    return energy;
  }

  private static <V> double diameter(Graph<V, ?> g, Map<Pair<V>, Integer> d, boolean use_max) {
    double diameter = 0;
    // TODO: provide an undirected version
    for (V v : g.vertexSet()) {
      for (V w : g.vertexSet()) {
        if (v.equals(w)) {
          continue; // don't include self-distances
        }
        Pair<V> pair = Pair.of(v, w);
        if (d.containsKey(pair)) {
          int dist = d.get(pair);
          diameter = Math.max(diameter, dist);

        } else {
          if (!use_max) {
            return Double.POSITIVE_INFINITY;
          }
        }
      }
    }
    return diameter;
  }

  public static class Pair<V> {
    final V first;
    final V second;

    public static <V> Pair<V> of(V first, V second) {
      return new Pair(first, second);
    }

    private Pair(V first, V second) {
      this.first = first;
      this.second = second;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Pair<?> pair = (Pair<?>) o;
      return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
      return Objects.hash(first, second);
    }

    @Override
    public String toString() {
      return "Pair{" + first + "," + second + '}';
    }
  }
}
