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

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that assigns positions to {@code Tree} or {@code Graph} nodes
 * using associations with nested circles ("balloons"). A balloon is nested inside another balloon
 * if the first balloon's subtree is a subtree of the second balloon's subtree.
 *
 * @author Tom Nelson
 */
public class BalloonLayoutAlgorithm<N> extends TreeLayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(BalloonLayoutAlgorithm.class);

  public static class Builder<N, T extends BalloonLayoutAlgorithm<N>, B extends Builder<N, T, B>>
      extends TreeLayoutAlgorithm.Builder<N, T, B> {

    public T build() {
      return (T) new BalloonLayoutAlgorithm(this);
    }
  }

  protected LoadingCache<N, PolarPoint> polarLocations =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, PolarPoint>() {
                public PolarPoint load(N node) {
                  return PolarPoint.ORIGIN;
                }
              });

  protected Map<N, Double> radii = new HashMap<>();

  public static <N> Builder<N, ?, ?> builder() {
    return new Builder<>();
  }

  protected BalloonLayoutAlgorithm(Builder<N, ?, ?> builder) {
    super(builder);
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);
    if (log.isTraceEnabled()) {
      log.trace("visit {}", layoutModel);
    }
    super.visit(layoutModel);
    setRootPolars(layoutModel);
  }

  protected void setRootPolars(LayoutModel<N> layoutModel) {
    Graph<N, ?> graph = layoutModel.getGraph();
    Set<N> roots =
        graph
            .vertexSet()
            .stream()
            .filter(node -> Graphs.predecessorListOf(graph, node).isEmpty())
            .collect(toImmutableSet());
    log.trace("roots: {}", roots);
    int width = layoutModel.getWidth();
    if (roots.size() == 1) {
      // its a Tree
      N root = Iterables.getOnlyElement(roots);
      setRootPolar(layoutModel, root);
      setPolars(
          layoutModel,
          Graphs.successorListOf(graph, root),
          getCenter(layoutModel),
          0,
          width / 2,
          new HashSet<>());
    } else if (roots.size() > 1) {
      // its a Network
      setPolars(layoutModel, roots, getCenter(layoutModel), 0, width / 2, new HashSet<>());
    }
  }

  protected void setRootPolar(LayoutModel<N> layoutModel, N root) {
    PolarPoint pp = PolarPoint.ORIGIN;
    Point p = getCenter(layoutModel);
    polarLocations.put(root, pp);
    layoutModel.set(root, p);
  }

  protected void setPolars(
      LayoutModel<N> layoutModel,
      Collection<N> kids,
      Point parentLocation,
      double angleToParent,
      double parentRadius,
      Set<N> seen) {

    int childCount = kids.size();
    if (childCount == 0) {
      return;
    }
    // handle the 1-child case with 0 limit on angle.
    double angle = Math.max(0, Math.PI / 2 * (1 - 2.0 / childCount));
    double childRadius = parentRadius * Math.cos(angle) / (1 + Math.cos(angle));
    double radius = parentRadius - childRadius;

    // the angle between the child nodes placed equally on a circle
    double angleBetweenKids = 2 * Math.PI / childCount;
    // how much to offset each angle to bisect the angle between 2 child nodes
    double offset = angleBetweenKids / 2 - angleToParent;

    int i = 0;
    for (N child : kids) {

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
      // so that sub tree node positions can be bisected by it.
      double newAngleToParent = Math.atan2(p.y - parentLocation.y, parentLocation.x - p.x);
      List<N> successors = Graphs.successorListOf(layoutModel.getGraph(), child);
      successors.removeIf(seen::contains);
      seen.addAll(successors);
      setPolars(layoutModel, successors, p, newAngleToParent, childRadius, seen);
    }
  }

  /**
   * @param node the node whose center is to be returned
   * @return the coordinates of {@code node}'s parent, or the center of this layout's area if it's a
   *     root.
   */
  public Point getCenter(LayoutModel<N> layoutModel, N node) {
    Graph<N, ?> graph = layoutModel.getGraph();
    N parent = Iterables.getOnlyElement(Graphs.predecessorListOf(graph, node), null);
    if (parent == null) {
      return getCenter(layoutModel);
    }
    return layoutModel.get(parent);
  }

  private Point getCartesian(LayoutModel<N> layoutModel, N node) {
    PolarPoint pp = polarLocations.getUnchecked(node);
    double centerX = layoutModel.getWidth() / 2;
    double centerY = layoutModel.getHeight() / 2;
    Point cartesian = PolarPoint.polarToCartesian(pp);
    cartesian = cartesian.add(centerX, centerY);
    return cartesian;
  }

  /** @return the radii */
  public Map<N, Double> getRadii() {
    return radii;
  }
}
