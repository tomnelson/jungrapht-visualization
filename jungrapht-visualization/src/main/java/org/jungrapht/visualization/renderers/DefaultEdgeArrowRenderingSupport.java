/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package org.jungrapht.visualization.renderers;

import java.awt.Shape;
import java.awt.geom.*;
import org.jungrapht.visualization.RenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public class DefaultEdgeArrowRenderingSupport<V, E> implements EdgeArrowRenderingSupport<V, E> {

  private static final Logger log = LoggerFactory.getLogger(DefaultEdgeArrowRenderingSupport.class);

  public AffineTransform getArrowTransform(
      RenderContext<V, E> rc, Shape edgeShape, Shape vertexShape) {
    Path2D path = new Path2D.Double(edgeShape);
    float[] seg = new float[6];
    Point2D p2 = null;
    AffineTransform at = new AffineTransform();
    // when the PathIterator is done, switch to the line-subdivide
    // method to get the arrowhead closer.
    for (PathIterator i = path.getPathIterator(null, 1); !i.isDone(); i.next()) {
      int ret = i.currentSegment(seg);
      if (ret == PathIterator.SEG_MOVETO) {
        p2 = new Point2D.Double(seg[0], seg[1]);
      } else if (ret == PathIterator.SEG_LINETO) {
        Point2D p1 = p2;
        p2 = new Point2D.Double(seg[0], seg[1]);
        if (vertexShape.contains(p2)) {
          at = getArrowTransform(rc, new Line2D.Double(p1, p2), vertexShape);
          break;
        }
      }
    }
    return at;
  }

  public AffineTransform getReverseArrowTransform(
      RenderContext<V, E> rc, Shape edgeShape, Shape vertexShape) {
    return getReverseArrowTransform(rc, edgeShape, vertexShape, true);
  }

  public AffineTransform getReverseArrowTransform(
      RenderContext<V, E> rc, Shape edgeShape, Shape vertexShape, boolean passedGo) {
    Path2D path = new Path2D.Double(edgeShape);
    float[] seg = new float[6];
    Point2D p2 = null;

    AffineTransform at = new AffineTransform();
    for (PathIterator i = path.getPathIterator(null, 1); !i.isDone(); i.next()) {
      int ret = i.currentSegment(seg);
      if (ret == PathIterator.SEG_MOVETO) {
        p2 = new Point2D.Double(seg[0], seg[1]);
      } else if (ret == PathIterator.SEG_LINETO) {
        Point2D p1 = p2;
        p2 = new Point2D.Double(seg[0], seg[1]);
        if (!passedGo && vertexShape.contains(p2)) {
          passedGo = true;
        } else if (passedGo && !vertexShape.contains(p2)) {
          at = getReverseArrowTransform(rc, new Line2D.Double(p1, p2), vertexShape);
          break;
        }
      }
    }
    return at;
  }

  public AffineTransform getArrowTransform(
      RenderContext<V, E> rc, Line2D edgeShape, Shape vertexShape) {
    double dx = edgeShape.getX1() - edgeShape.getX2();
    double dy = edgeShape.getY1() - edgeShape.getY2();
    // iterate over the line until the edge shape will place the
    // arrowhead closer than 'arrowGap' to the vertex shape boundary
    while ((dx * dx + dy * dy) > rc.getArrowPlacementTolerance()) {
      try {
        edgeShape = getLastOutsideSegment(edgeShape, vertexShape);
      } catch (IllegalArgumentException e) {
        log.error("got", e);
        return null;
      }
      dx = edgeShape.getX1() - edgeShape.getX2();
      dy = edgeShape.getY1() - edgeShape.getY2();
    }
    double atheta = Math.atan2(dx, dy) + Math.PI / 2;
    AffineTransform at = AffineTransform.getTranslateInstance(edgeShape.getX1(), edgeShape.getY1());
    at.rotate(-atheta);
    return at;
  }

  protected AffineTransform getReverseArrowTransform(
      RenderContext<V, E> rc, Line2D edgeShape, Shape vertexShape) {
    double dx = (edgeShape.getX1() - edgeShape.getX2());
    double dy = (edgeShape.getY1() - edgeShape.getY2());
    // iterate over the line until the edge shape will place the
    // arrowhead closer than 'arrowGap' to the vertex shape boundary
    while ((dx * dx + dy * dy) > rc.getArrowPlacementTolerance()) {
      try {
        edgeShape = getFirstOutsideSegment(edgeShape, vertexShape);
      } catch (IllegalArgumentException e) {
        log.error("got", e);
        return null;
      }
      dx = (edgeShape.getX1() - edgeShape.getX2());
      dy = (edgeShape.getY1() - edgeShape.getY2());
    }
    // calculate the angle for the arrowhead
    double atheta = Math.atan2(dx, dy) - Math.PI / 2;
    AffineTransform at = AffineTransform.getTranslateInstance(edgeShape.getX1(), edgeShape.getY1());
    at.rotate(-atheta);
    return at;
  }

  /**
   * Returns a line that intersects {@code shape}'s boundary.
   *
   * @param line line to subdivide
   * @param shape shape to compare with line
   * @return a line that intersects the shape boundary
   * @throws IllegalArgumentException if the passed line's point2 is not inside the shape
   */
  protected Line2D getLastOutsideSegment(Line2D line, Shape shape) {
    if (!shape.contains(line.getP2())) {
      throw new RuntimeException(
          "line end point: " + line.getP2() + " is not contained in shape: " + shape.getBounds2D());
    }
    Line2D left = new Line2D.Double();
    Line2D right = new Line2D.Double();
    // subdivide the line until its left segment intersects
    // the shape boundary
    do {
      subdivide(line, left, right);
      line = right;
    } while (!shape.contains(line.getP1()));
    // now that right is completely inside shape,
    // return left, which must be partially outside
    return left;
  }

  /**
   * Returns a line that intersects {@code shape}'s boundary.
   *
   * @param line line to subdivide
   * @param shape shape to compare with line
   * @return a line that intersects the shape boundary
   * @throws IllegalArgumentException if the passed line's point1 is not inside the shape
   */
  protected Line2D getFirstOutsideSegment(Line2D line, Shape shape) {
    if (!shape.contains(line.getP1())) {
      throw new RuntimeException(
          "line start point: "
              + line.getP1()
              + " is not contained in shape: "
              + shape.getBounds2D());
    }
    Line2D left = new Line2D.Double();
    Line2D right = new Line2D.Double();
    // subdivide the line until its right side intersects the
    // shape boundary
    do {
      subdivide(line, left, right);
      line = left;
    } while (!shape.contains(line.getP2()));
    // now that left is completely inside shape,
    // return right, which must be partially outside
    return right;
  }

  /**
   * divide a Line2D into 2 new Line2Ds that are returned in the passed left and right instances, if
   * non-null
   *
   * @param src the line to divide
   * @param left the left side, or null
   * @param right the right side, or null
   */
  protected void subdivide(Line2D src, Line2D left, Line2D right) {
    double x1 = src.getX1();
    double y1 = src.getY1();
    double x2 = src.getX2();
    double y2 = src.getY2();

    double mx = x1 + (x2 - x1) / 2.0;
    double my = y1 + (y2 - y1) / 2.0;
    if (left != null) {
      left.setLine(x1, y1, mx, my);
    }
    if (right != null) {
      right.setLine(mx, my, x2, y2);
    }
  }
}
