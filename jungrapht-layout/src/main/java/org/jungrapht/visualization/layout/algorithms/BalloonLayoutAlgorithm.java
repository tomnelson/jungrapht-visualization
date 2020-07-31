/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package org.jungrapht.visualization.layout.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that assigns positions to {@code Tree} or {@code Graph} vertices
 * using associations with nested circles ("balloons"). A balloon is nested inside another balloon
 * if the first balloon's subtree is a subtree of the second balloon's subtree.
 *
 * @author Tom Nelson
 */
public class BalloonLayoutAlgorithm<V> extends TreeLayoutAlgorithm<V>
    implements TreeLayout<V>, Balloon {

  private static final Logger log = LoggerFactory.getLogger(BalloonLayoutAlgorithm.class);

  public static class Builder<V, T extends BalloonLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends TreeLayoutAlgorithm.Builder<V, T, B> implements LayoutAlgorithm.Builder<V, T, B> {

    public T build() {
      return (T) new BalloonLayoutAlgorithm(this);
    }
  }

  protected Map<V, PolarPoint> polarLocations = new ConcurrentHashMap<>();

  private Function<V, PolarPoint> initializer = v -> PolarPoint.ORIGIN;

  protected Map<V, Double> radii = new HashMap<>();

  public static <V> Builder<V, ?, ?> builder() {
    return (Builder<V, ?, ?>) new Builder<>().expandLayout(false);
  }

  public BalloonLayoutAlgorithm() {
    this(BalloonLayoutAlgorithm.builder());
  }

  protected BalloonLayoutAlgorithm(Builder<V, ?, ?> builder) {
    super(builder);
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    Graph<V, ?> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    super.visit(layoutModel);
    if (log.isTraceEnabled()) {
      log.trace("visit {}", layoutModel);
    }
    setRootPolars(layoutModel);
  }

  protected void setRootPolars(LayoutModel<V> layoutModel) {
    Graph<V, ?> graph = layoutModel.getGraph();
    Set<V> roots =
        graph
            .vertexSet()
            .stream()
            .filter(vertex -> neighborCache.predecessorsOf(vertex).isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    log.trace("roots: {}", roots);
    int width = layoutModel.getWidth();
    if (roots.size() == 1) {
      // its a Tree
      V root = roots.stream().findFirst().get();
      setRootPolar(layoutModel, root);
      setPolars(
          layoutModel,
          neighborCache.successorsOf(root),
          getCenter(layoutModel),
          0,
          width / 2,
          new HashSet<>());
    } else if (roots.size() > 1) {
      // its a Graph
      setPolars(layoutModel, roots, getCenter(layoutModel), 0, width / 2, new HashSet<>());
    }
  }

  protected void setRootPolar(LayoutModel<V> layoutModel, V root) {
    PolarPoint pp = PolarPoint.ORIGIN;
    Point p = getCenter(layoutModel);
    polarLocations.put(root, pp);
    log.trace(
        "putting the root at {} in model of size {},{}",
        p,
        layoutModel.getWidth(),
        layoutModel.getHeight());
    layoutModel.set(root, p);
  }

  protected void setPolars(
      LayoutModel<V> layoutModel,
      Collection<V> kids,
      Point parentLocation,
      double angleToParent,
      double parentRadius,
      Set<V> seen) {

    int childCount = kids.size();
    if (childCount == 0) {
      return;
    }
    // handle the 1-child case with 0 limit on angle.
    double angle = Math.max(0, Math.PI / 2 * (1 - 2.0 / childCount));
    double childRadius = parentRadius * Math.cos(angle) / (1 + Math.cos(angle));
    double radius = parentRadius - childRadius;

    // the angle between the child vertices placed equally on a circle
    double angleBetweenKids = 2 * Math.PI / childCount;
    // how much to offset each angle to bisect the angle between 2 child vertices
    double offset = angleBetweenKids / 2 - angleToParent;

    int i = 0;
    for (V child : kids) {

      // increment for each child. include the offset to space edge to parent
      // in between 2 edges to children
      double theta = i++ * angleBetweenKids + offset;

      radii.put(child, childRadius);

      PolarPoint pp = PolarPoint.of(theta, radius);
      polarLocations.put(child, pp);

      Point p = PolarPoint.polarToCartesian(pp);
      p = p.add(parentLocation.x, parentLocation.y);

      layoutModel.set(child, p);

      // compute the angle from p to the parent and pass to function
      // so that sub tree vertex positions can be bisected by it.
      double newAngleToParent = Math.atan2(p.y - parentLocation.y, parentLocation.x - p.x);
      Set<V> successors = new HashSet<>(neighborCache.successorsOf(child));
      successors.removeIf(seen::contains);
      seen.addAll(successors);
      setPolars(layoutModel, successors, p, newAngleToParent, childRadius, seen);
    }
  }

  /**
   * @param vertex the vertex whose center is to be returned
   * @return the coordinates of {@code vertex}'s parent, or the center of this layout's area if it's
   *     a root.
   */
  public Point getCenter(LayoutModel<V> layoutModel, V vertex) {
    Graph<V, ?> graph = layoutModel.getGraph();
    V parent = neighborCache.predecessorsOf(vertex).stream().findFirst().orElse(null);
    if (parent == null) {
      return getCenter(layoutModel);
    }
    return layoutModel.get(parent);
  }

  private Point getCartesian(LayoutModel<V> layoutModel, V vertex) {
    PolarPoint pp = polarLocations.computeIfAbsent(vertex, initializer);
    double centerX = layoutModel.getWidth() / 2;
    double centerY = layoutModel.getHeight() / 2;
    Point cartesian = PolarPoint.polarToCartesian(pp);
    cartesian = cartesian.add(centerX, centerY);
    return cartesian;
  }

  /** @return the radii */
  public Map<V, Double> getRadii() {
    return radii;
  }

  @Override
  public boolean constrained() {
    return true;
  }
}
