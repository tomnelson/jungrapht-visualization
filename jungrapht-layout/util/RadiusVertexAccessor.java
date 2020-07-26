/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 *
 * Created on Apr 12, 2005
 */
package org.jungrapht.visualization.layout.util;

import java.util.ConcurrentModificationException;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of PickSupport that returns the vertex or edge that is closest to the
 * specified location. This implementation provides the same picking options that were available in
 * previous versions of
 *
 * <p>No element will be returned that is farther away than the specified maximum distance.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class RadiusVertexAccessor<V> implements VertexAccessor<V> {

  private static final Logger log = LoggerFactory.getLogger(RadiusVertexAccessor.class);
  protected double maxDistance;
  /** Creates an instance with an effectively infinite default maximum distance. */
  public RadiusVertexAccessor() {
    this(Math.sqrt(Double.MAX_VALUE - 1000));
  }

  /**
   * Creates an instance with the specified default maximum distance.
   *
   * @param maxDistance the maximum distance at which any element can be from a specified location
   *     and still be returned
   */
  public RadiusVertexAccessor(double maxDistance) {
    this.maxDistance = maxDistance;
  }

  /**
   * @param layoutModel
   * @param p the pick point
   * @return the vertex associated with location p
   */
  @Override
  public V getVertex(LayoutModel<V> layoutModel, Point p) {
    return getVertex(layoutModel, p.x, p.y);
  }

  /**
   * Gets the vertex nearest to the location of the (x,y) location selected, within a distance of
   * {@code this.maxDistance}. Iterates through all visible vertices and checks their distance from
   * the location. Override this method to provide a more efficient implementation.
   *
   * @param x the x coordinate of the location
   * @param y the y coordinate of the location
   * @return a vertex which is associated with the location {@code (x,y)} as given by {@code layout}
   */
  @Override
  public V getVertex(LayoutModel<V> layoutModel, double x, double y) {

    double minDistance = maxDistance * maxDistance * maxDistance;
    V closest = null;
    while (true) {
      try {
        for (V vertex : layoutModel.getGraph().vertexSet()) {

          Point p = layoutModel.apply(vertex);
          double dx = p.x - x;
          double dy = p.y - y;
          double dist = dx * dx + dy * dy;
          if (dist < minDistance) {
            minDistance = dist;
            closest = vertex;
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return closest;
  }
}
