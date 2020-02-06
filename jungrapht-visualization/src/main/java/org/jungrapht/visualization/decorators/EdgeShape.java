/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on March 10, 2005
 */
package org.jungrapht.visualization.decorators;

import java.awt.Shape;
import java.awt.geom.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.util.Context;
import org.jungrapht.visualization.util.EdgeIndexFunction;

/**
 * An interface for decorators that return a <code>Shape</code> for a specified edge.
 *
 * <p>All edge shapes must be defined so that their endpoints are at (0,0) and (1,0). They will be
 * scaled, rotated and translated into position by the Renderer.
 *
 * @author Tom Nelson
 */
public interface EdgeShape {

  Line2D LINE = new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);
  QuadCurve2D QUAD_CURVE = new QuadCurve2D.Float();
  CubicCurve2D CUBIC_CURVE = new CubicCurve2D.Float();
  Ellipse2D ELLIPSE = new Ellipse2D.Float(-.5f, -.5f, 1, 1);
  Rectangle2D BOX = new Rectangle2D.Float();

  /**
   * A convenience instance for other edge shapes to use for self-loop edges where parallel
   * instances will not overlay each other.
   */
  Loop loop = new Loop();

  static <E> boolean isLoop(Graph<?, E> graph, E edge) {
    return graph.getEdgeSource(edge).equals(graph.getEdgeTarget(edge));
  }

  static <V, E> Line<V, E> line() {
    return new Line<>();
  }

  static <V, E> QuadCurve<V, E> quadCurve() {
    return new QuadCurve<>();
  }

  static <V, E> CubicCurve<V, E> cubicCurve() {
    return new CubicCurve<>();
  }

  static <V, E> Orthogonal<V, E> orthogonal() {
    return new Orthogonal();
  }

  static <V, E> ArticulatedLine<V, E> articulatedLine() {
    return new ArticulatedLine<>();
  }

  /** An edge shape that renders as a straight line between the vertex endpoints. */
  class Line<V, E> implements Function<Context<Graph<V, E>, E>, Shape> {

    public Shape apply(Context<Graph<V, E>, E> context) {
      Graph graph = context.graph;
      E e = context.element;
      return isLoop(graph, e) ? ELLIPSE : LINE;
    }
  }

  class ArticulatedLine<V, E> extends ArticulatedEdgeShapeFunction<V, E>
      implements Function<Context<Graph<V, E>, E>, Shape> {

    /**
     * feedback edges (that would cause cycles) were reversed before the graph layering stage. They
     * need to be modified so that the articulation points are 'reversed' in coordinate space
     */
    Collection<E> reversedEdges;

    public ArticulatedLine(Collection<E> reversedEdges) {
      this.reversedEdges = reversedEdges;
    }

    public ArticulatedLine() {
      this(Collections.emptySet());
    }

    public Shape apply(Context<Graph<V, E>, E> context) {
      E e = context.element;
      List<Point> points = edgeArticulationFunction.apply(e);
      if (points.isEmpty()) {
        return isLoop(context.graph, e) ? ELLIPSE : LINE;
      } else if (reversedEdges.contains(e)) {
        return ArticulatedEdgeShapeFunctions.makeReverseUnitShape(points);
      } else {
        return ArticulatedEdgeShapeFunctions.makeUnitShape(points);
      }
    }
  }

  /** An edge shape that renders as a QuadCurve between vertex endpoints. */
  class QuadCurve<V, E> extends ParallelEdgeShapeFunction<V, E> {
    @Override
    public void setEdgeIndexFunction(EdgeIndexFunction<V, E> parallelEdgeIndexFunction) {
      super.setEdgeIndexFunction(parallelEdgeIndexFunction);
      loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
    }

    /**
     * Get the shape for this edge, returning either the shared instance or, in the case of
     * self-loop edges, the Loop shared instance.
     */
    public Shape apply(Context<Graph<V, E>, E> context) {
      Graph graph = context.graph;
      E e = context.element;
      if (isLoop(graph, e)) {
        return loop.apply(context);
      }

      int index = edgeIndexFunction.apply(context);

      float controlY = controlOffsetIncrement + controlOffsetIncrement * index;
      QUAD_CURVE.setCurve(0.0f, 0.0f, 0.5f, controlY, 1.0f, 0.0f);
      return QUAD_CURVE;
    }
  }

  /**
   * An edge shape that renders as a CubicCurve between vertex endpoints. The two control points are
   * at (1/3*length, 2*controlY) and (2/3*length, controlY) giving a 'spiral' effect.
   */
  class CubicCurve<V, E> extends ParallelEdgeShapeFunction<V, E> {
    public void setEdgeIndexFunction(EdgeIndexFunction<V, E> edgeIndexFunction) {
      super.setEdgeIndexFunction(edgeIndexFunction);
      loop.setEdgeIndexFunction(edgeIndexFunction);
    }

    /**
     * Get the shape for this edge, returning either the shared instance or, in the case of
     * self-loop edges, the Loop shared instance.
     */
    public Shape apply(Context<Graph<V, E>, E> context) {
      Graph graph = context.graph;
      E e = context.element;
      if (isLoop(graph, e)) {
        return loop.apply(context);
      }

      int index = edgeIndexFunction.apply(context);

      float controlY = controlOffsetIncrement + controlOffsetIncrement * index;
      CUBIC_CURVE.setCurve(0.0f, 0.0f, 0.33f, 2 * controlY, .66f, -controlY, 1.0f, 0.0f);
      return CUBIC_CURVE;
    }
  }

  /**
   * An edge shape that renders as a loop with its nadir at the center of the vertex. Parallel
   * instances will overlap.
   *
   * @author Tom Nelson
   */
  class SimpleLoop<E> implements Function<E, Shape> {
    public Shape apply(E e) {
      return ELLIPSE;
    }
  }

  static Shape buildFrame(RectangularShape shape, int index) {
    float x = -.5f;
    float y = -.5f;
    float diam = 1.f;
    diam += diam * index / 2;
    x += x * index / 2;
    y += y * index / 2;

    shape.setFrame(x, y, diam, diam);

    return shape;
  }

  /**
   * An edge shape that renders as a loop with its nadir at the center of the vertex. Parallel
   * instances will not overlap.
   */
  class Loop<V, E> extends ParallelEdgeShapeFunction<V, E> {
    public Shape apply(Context<Graph<V, E>, E> context) {
      Graph graph = context.graph;
      E e = context.element;
      return buildFrame(ELLIPSE, edgeIndexFunction.apply(context));
    }
  }

  /**
   * An edge shape that renders as a diamond with its nadir at the center of the vertex. Parallel
   * instances will not overlap.
   */
  class Box<V, E> extends ParallelEdgeShapeFunction<V, E> {
    public Shape apply(Context<Graph<V, E>, E> context) {
      Graph graph = context.graph;
      E e = context.element;
      return buildFrame(BOX, edgeIndexFunction.apply(context));
    }
  }

  /** An edge shape that renders as a bent-line between the vertex endpoints. */
  class Orthogonal<V, E> extends ParallelEdgeShapeFunction<V, E> {
    Box box;

    public Orthogonal() {
      this.box = new Box();
    }

    public Shape apply(Context<Graph<V, E>, E> context) {
      Graph graph = context.graph;
      E e = context.element;
      return isLoop(graph, e) ? box.apply(context) : LINE;
    }
  }
}
