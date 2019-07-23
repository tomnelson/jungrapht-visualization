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
import java.util.Map;
import java.util.Set;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A radial layout for Tree or Forest graphs. Positions vertices in concentric circles with the root
 * at the center
 *
 * @author Tom Nelson
 */
public class RadialTreeLayoutAlgorithm<N> extends TreeLayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(RadialTreeLayoutAlgorithm.class);

  protected Map<N, PolarPoint> polarLocations = new HashMap<>();

  public static class Builder<N, T extends RadialTreeLayoutAlgorithm<N>, B extends Builder<N, T, B>>
      extends TreeLayoutAlgorithm.Builder<N, T, B> {

    public T build() {
      return (T) new RadialTreeLayoutAlgorithm<>(this);
    }
  }

  public static <N> Builder<N, ?, ?> builder() {
    return new Builder<>();
  }

  protected RadialTreeLayoutAlgorithm(Builder<N, ?, ?> builder) {
    super(builder);
  }

  @Override
  protected Set<N> buildTree(LayoutModel<N> layoutModel) {
    Set<N> roots = super.buildTree(layoutModel);
    setRadialLocations(roots, layoutModel);
    putRadialPointsInModel(layoutModel);
    return roots;
  }

  /**
   * override for to always start at zero
   *
   * @param initialY
   * @return
   */
  @Override
  protected int getInitialY(int initialY) {
    return 0;
  }

  private void putRadialPointsInModel(LayoutModel<N> layoutModel) {
    polarLocations.forEach((key, value) -> layoutModel.set(key, getCartesian(layoutModel, key)));
  }

  /** @return a map from nodes to their locations in polar coordinates. */
  public Map<N, PolarPoint> getPolarLocations() {
    return polarLocations;
  }

  private Point getCartesian(LayoutModel<N> layoutModel, N node) {
    PolarPoint pp = polarLocations.get(node);
    double centerX = layoutModel.getWidth() / 2;
    double centerY = layoutModel.getHeight() / 2;
    Point cartesian = PolarPoint.polarToCartesian(pp);
    cartesian = cartesian.add(centerX, centerY);
    return cartesian;
  }

  private Point getMaxXY(LayoutModel<N> layoutModel) {
    double maxx = 0;
    double maxy = 0;
    Collection<N> nodes = layoutModel.getGraph().vertexSet();
    for (N node : nodes) {
      Point location = layoutModel.apply(node);
      maxx = Math.max(maxx, location.x);
      maxy = Math.max(maxy, location.y);
    }
    return Point.of(maxx, maxy);
  }

  private void setRadialLocations(Set<N> roots, LayoutModel<N> layoutModel) {
    int width = layoutModel.getWidth();
    Point max = getMaxXY(layoutModel);
    double maxx = max.x;
    double maxy = max.y;
    maxx = Math.max(maxx, width);
    double theta = 2 * Math.PI / maxx;

    double deltaRadius = width / 2 / maxy;
    double offset = 0;
    if (roots.size() > 1) {
      offset = verticalNodeSpacing;
    }
    for (N node : layoutModel.getGraph().vertexSet()) {
      Point p = layoutModel.get(node);

      PolarPoint polarPoint =
          PolarPoint.of(p.x * theta, (offset + p.y - this.verticalNodeSpacing) * deltaRadius);
      polarLocations.put(node, polarPoint);
    }
  }
}
