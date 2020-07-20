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
 * A radial layout for Tree or Forest graphs. Positions vertices in concentric circles with the
 * root(s) at the center
 *
 * @author Tom Nelson
 */
public class RadialTreeLayoutAlgorithm<V> extends TreeLayoutAlgorithm<V>
    implements RadialTreeLayout<V> {

  private static final Logger log = LoggerFactory.getLogger(RadialTreeLayoutAlgorithm.class);

  protected Map<V, PolarPoint> polarLocations = new HashMap<>();

  public static class Builder<V, T extends RadialTreeLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends TreeLayoutAlgorithm.Builder<V, T, B> implements LayoutAlgorithm.Builder<V, T, B> {

    public T build() {
      return (T) new RadialTreeLayoutAlgorithm<>(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return (Builder<V, ?, ?>) new Builder<>().expandLayout(false);
  }

  public RadialTreeLayoutAlgorithm() {
    this(RadialTreeLayoutAlgorithm.builder());
  }

  protected RadialTreeLayoutAlgorithm(Builder<V, ?, ?> builder) {
    super(builder);
  }

  @Override
  protected Set<V> buildTree(LayoutModel<V> layoutModel) {
    Set<V> roots = super.buildTree(layoutModel);
    setRadialLocations(roots, layoutModel);
    putRadialPointsInModel(layoutModel);
    // set size with the max of the layout rings
    int diameter = diameter(layoutModel);
    int offsetDelta = diameter - layoutModel.getWidth();
    offset(layoutModel, offsetDelta / 2);
    layoutModel.setSize(diameter, diameter);
    return roots;
  }

  protected void offset(LayoutModel<V> layoutModel, int delta) {
    layoutModel
        .getGraph()
        .vertexSet()
        .forEach(
            v -> {
              Point p = layoutModel.get(v);
              p = p.add(delta, delta);
              layoutModel.set(v, p);
            });
  }

  @Override
  public int diameter(LayoutModel<V> layoutModel) {
    return layoutModel
            .getGraph()
            .vertexSet()
            .stream()
            .map(vertex -> polarLocations.get(vertex).radius * 2)
            .mapToInt(Double::intValue)
            .max()
            .orElse(layoutModel.getWidth())
        + horizontalVertexSpacing;
  }

  /**
   * override for to always start at zero
   *
   * @param layoutHeight
   * @param treeHeight
   * @return 0 for any supplied values
   */
  @Override
  protected int getInitialPosition(int initialPosition, int layoutHeight, int treeHeight) {
    return 0;
  }

  protected void putRadialPointsInModel(LayoutModel<V> layoutModel) {
    polarLocations.forEach((key, value) -> layoutModel.set(key, getCartesian(layoutModel, key)));
  }

  /** @return a map from vertices to their locations in polar coordinates. */
  public Map<V, PolarPoint> getPolarLocations() {
    return polarLocations;
  }

  protected Point getCartesian(LayoutModel<V> layoutModel, V vertex) {
    PolarPoint pp = polarLocations.get(vertex);
    double centerX = layoutModel.getWidth() / 2;
    double centerY = layoutModel.getHeight() / 2;
    Point cartesian = PolarPoint.polarToCartesian(pp);
    cartesian = cartesian.add(centerX, centerY);
    return cartesian;
  }

  protected Point getMaxXY(LayoutModel<V> layoutModel) {
    double maxx = 0;
    double maxy = 0;
    Collection<V> vertices = layoutModel.getGraph().vertexSet();
    for (V vertex : vertices) {
      Point location = layoutModel.apply(vertex);
      maxx = Math.max(maxx, location.x);
      maxy = Math.max(maxy, location.y);
    }
    return Point.of(maxx, maxy);
  }

  protected void setRadialLocations(Set<V> roots, LayoutModel<V> layoutModel) {
    int width = layoutModel.getWidth();
    Point max = getMaxXY(layoutModel);
    double maxx = max.x + verticalVertexSpacing;
    maxx = Math.max(maxx, width);
    double theta = 2 * Math.PI / maxx;

    double deltaRadius = 1; //width / 2 / maxy;
    double offset = 0;
    if (roots.size() > 1) {
      offset = verticalVertexSpacing;
    }
    for (V vertex : layoutModel.getGraph().vertexSet()) {
      Point p = layoutModel.get(vertex);

      PolarPoint polarPoint =
          PolarPoint.of(p.x * theta, (offset + p.y - this.verticalVertexSpacing) * deltaRadius);
      polarLocations.put(vertex, polarPoint);
    }
  }

  @Override
  public boolean constrained() {
    return true;
  }
}
