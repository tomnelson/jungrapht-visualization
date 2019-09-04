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
package org.jungrapht.visualization.layout;

import java.awt.*;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RadiusVertexAccessor;

/**
 * Simple implementation of GraphElementAccessor that returns the vertex or edge that is closest to
 * the specified location.
 *
 * <p>No element will be returned that is farther away than the specified maximum distance.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class RadiusGraphElementAccessor<V, E> extends RadiusVertexAccessor<V>
    implements GraphElementAccessor<V, E> {
  private final Graph<V, E> graph;

  /** Creates an instance with an effectively infinite default maximum distance. */
  public RadiusGraphElementAccessor(Graph<V, E> graph) {
    this(graph, Math.sqrt(Double.MAX_VALUE - 1000));
  }

  /**
   * Creates an instance with the specified default maximum distance.
   *
   * @param maxDistance the maximum distance at which any element can be from a specified location
   *     and still be returned
   */
  public RadiusGraphElementAccessor(Graph<V, E> graph, double maxDistance) {
    super(maxDistance);
    this.graph = graph;
  }

  /**
   * Gets the edge nearest to the point location selected, whose endpoints are &lt; {@code
   * maxDistance}. Iterates through all visible edges and checks their distance from the location.
   * Override this method to provide a more efficient implementation.
   *
   * @param layoutModel
   * @param p the pick location
   * @return
   */
  @Override
  public E getEdge(LayoutModel<V> layoutModel, Point p) {
    return getEdge(layoutModel, p.x, p.y);
  }

  /**
   * Gets the edge nearest to the location of the (x,y) location selected, whose endpoints are &lt;
   * {@code maxDistance}. Iterates through all visible vertices and checks their distance from the
   * location. Override this method to provide a more efficient implementation.
   *
   * <p>// * @param layout the context in which the location is defined
   *
   * @param x the x coordinate of the location
   * @param y the y coordinate of the location // * @param maxDistance the maximum distance at which
   *     any element can be from a specified location and still be returned
   * @return an edge which is associated with the location {@code (x,y)} as given by {@code layout}
   */
  @Override
  public E getEdge(LayoutModel<V> layoutModel, double x, double y) {
    double minDistance = maxDistance * maxDistance;
    E closest = null;
    while (true) {
      try {
        for (E edge : graph.edgeSet()) {
          V vertex1 = graph.getEdgeSource(edge);
          V vertex2 = graph.getEdgeTarget(edge);
          // Get coords
          org.jungrapht.visualization.layout.model.Point p1 = layoutModel.apply(vertex1);
          org.jungrapht.visualization.layout.model.Point p2 = layoutModel.apply(vertex2);
          double x1 = p1.x;
          double y1 = p1.y;
          double x2 = p2.x;
          double y2 = p2.y;
          // Calculate location on line closest to (x,y)
          // First, check that v1 and v2 are not coincident.
          if (x1 == x2 && y1 == y2) {
            continue;
          }
          double b =
              ((y - y1) * (y2 - y1) + (x - x1) * (x2 - x1))
                  / ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
          //
          double distance2; // square of the distance
          if (b <= 0) {
            distance2 = (x - x1) * (x - x1) + (y - y1) * (y - y1);
          } else if (b >= 1) {
            distance2 = (x - x2) * (x - x2) + (y - y2) * (y - y2);
          } else {
            double x3 = x1 + b * (x2 - x1);
            double y3 = y1 + b * (y2 - y1);
            distance2 = (x - x3) * (x - x3) + (y - y3) * (y - y3);
          }

          if (distance2 < minDistance) {
            minDistance = distance2;
            closest = edge;
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return closest;
  }

  public Set<V> getVertices(LayoutModel<V> layoutModel, Shape rectangle) {
    Set<V> pickedvertices = new HashSet<>();
    while (true) {
      try {
        for (V vertex : layoutModel.getGraph().vertexSet()) {
          Point p = layoutModel.apply(vertex);
          if (rectangle.contains(p.x, p.y)) {
            pickedvertices.add(vertex);
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return pickedvertices;
  }
}
