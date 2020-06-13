package org.jungrapht.visualization.util;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * collects the {@link Rectangle2D}s that bound all of the elements of a {@code Graph}
 *
 * @param <T> the type to collect bounds for, either vertices or edges (see subclasses)
 */
public abstract class BoundingRectangleCollector<T> {

  protected LayoutModel layoutModel;
  protected List<Rectangle2D> rectangles = new ArrayList<>();

  public BoundingRectangleCollector(LayoutModel layoutModel) {
    this.layoutModel = layoutModel;
  }

  public static final class Points<V> extends BoundingRectangleCollector<V> {
    private static final Logger log =
        LoggerFactory.getLogger(BoundingRectangleCollector.Points.class);

    protected Function<V, Shape> vertexShapeFunction;

    public Points(Function<V, Shape> vertexShapeFunction, LayoutModel layoutModel) {
      super(layoutModel);
      this.vertexShapeFunction = vertexShapeFunction;
      compute();
    }

    public Rectangle2D getForElement(V vertex) {
      Shape shape = new Rectangle2D.Double();
      Point p = (Point) layoutModel.apply(vertex);

      float x = (float) p.x;
      float y = (float) p.y;
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      Rectangle2D xfs = xform.createTransformedShape(shape).getBounds2D();
      log.trace("vertex {} with shape bounds {} is at {}", vertex, xfs, p);
      return xfs;
    }

    /**
     * @param vertex the vertex to get bounds for
     * @param p1 the location of the vertex
     * @param p2 ignored for Vertices
     * @return bounds for the vertex shape
     */
    public Rectangle2D getForElement(V vertex, Point p1, Point p2) {
      return getForElement(vertex, p1);
    }

    public Rectangle2D getForElement(V vertex, Point p) {
      Shape shape = vertexShapeFunction.apply(vertex);
      log.trace("vertex is at {}", p);

      float x = (float) p.x;
      float y = (float) p.y;
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      return xform.createTransformedShape(shape).getBounds2D();
    }

    public void compute(Collection<V> vertices) {
      super.compute();

      for (V v : vertices) {
        Shape shape = vertexShapeFunction.apply(v);
        Point p = (Point) layoutModel.apply(v);
        float x = (float) p.x;
        float y = (float) p.y;
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
        shape = xform.createTransformedShape(shape);
        rectangles.add(shape.getBounds2D());
      }
    }

    public void compute() {
      super.compute();
      Graph<V, ?> graph = layoutModel.getGraph();
      for (V v : graph.vertexSet()) {
        Shape shape = vertexShapeFunction.apply(v);
        Point p = (Point) layoutModel.apply(v);
        float x = (float) p.x;
        float y = (float) p.y;
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
        shape = xform.createTransformedShape(shape);
        rectangles.add(shape.getBounds2D());
      }
    }
  }

  /**
   * collects the {@link Rectangle2D}s that bound all of the vertices of a {@code Graph}
   *
   * @param <V> the vertex type
   */
  public static final class Vertices<V> extends BoundingRectangleCollector<V> {
    private static final Logger log = LoggerFactory.getLogger(Vertices.class);

    protected Function<V, Shape> vertexShapeFunction;

    public Vertices(Function<V, Shape> vertexShapeFunction, LayoutModel layoutModel) {
      super(layoutModel);
      this.vertexShapeFunction = vertexShapeFunction;
      compute();
    }

    public Rectangle2D getForElement(V vertex) {
      Shape shape = vertexShapeFunction.apply(vertex);
      Point p = (Point) layoutModel.apply(vertex);

      float x = (float) p.x;
      float y = (float) p.y;
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      Rectangle2D xfs = xform.createTransformedShape(shape).getBounds2D();
      log.trace("vertex {} with shape bounds {} is at {}", vertex, xfs, p);
      return xfs;
    }

    /**
     * @param vertex the vertex to get bounds for
     * @param p1 the location of vertex
     * @param p2 ignored for Vertices
     * @return the bounds
     */
    public Rectangle2D getForElement(V vertex, Point p1, Point p2) {
      return getForElement(vertex, p1);
    }

    public Rectangle2D getForElement(V vertex, Point p) {
      Shape shape = vertexShapeFunction.apply(vertex);
      log.trace("vertex is at {}", p);

      float x = (float) p.x;
      float y = (float) p.y;
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      return xform.createTransformedShape(shape).getBounds2D();
    }

    public void compute(Collection<V> vertices) {
      super.compute();

      for (V v : vertices) {
        Shape shape = vertexShapeFunction.apply(v);
        Point p = (Point) layoutModel.apply(v);
        float x = (float) p.x;
        float y = (float) p.y;
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
        shape = xform.createTransformedShape(shape);
        rectangles.add(shape.getBounds2D());
      }
    }

    public void compute() {
      super.compute();

      for (Object v : layoutModel.getGraph().vertexSet()) {
        Shape shape = vertexShapeFunction.apply((V) v);
        Point p = (Point) layoutModel.apply(v);
        float x = (float) p.x;
        float y = (float) p.y;
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
        shape = xform.createTransformedShape(shape);
        rectangles.add(shape.getBounds2D());
      }
    }
  }

  /**
   * collects the {@link Rectangle2D}s that bound all of the edges of a {@code Graph}
   *
   * @param <E> the edge type
   */
  public static final class Edges<V, E> extends BoundingRectangleCollector<E> {

    private static final double NON_EMPTY_DELTA = 0.001;
    protected Function<V, Shape> vertexShapeFunction;
    protected Function<Context<Graph<V, E>, E>, Shape> edgeShapeFunction;
    protected Graph<V, E> graph;

    public Edges(
        Function<V, Shape> vertexShapeFunction,
        Function<Context<Graph<V, E>, E>, Shape> edgeShapeFunction,
        LayoutModel<V> layoutModel) {
      super(layoutModel);
      this.vertexShapeFunction = vertexShapeFunction;
      this.edgeShapeFunction = edgeShapeFunction;
      this.graph = layoutModel.getGraph();
      compute();
    }

    public Rectangle2D getForElement(E edge) {
      V v1 = graph.getEdgeSource(edge);
      V v2 = graph.getEdgeTarget(edge);
      Point p1 = (Point) layoutModel.apply(v1);
      Point p2 = (Point) layoutModel.apply(v2);
      float x1 = (float) p1.x;
      float y1 = (float) p1.y;
      float x2 = (float) p2.x;
      float y2 = (float) p2.y;

      boolean isLoop = v1.equals(v2);
      Shape s2 = vertexShapeFunction.apply(v2);
      Shape edgeShape = edgeShapeFunction.apply(Context.getInstance(layoutModel.getGraph(), edge));

      AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

      if (isLoop) {
        Rectangle2D s2Bounds = s2.getBounds2D();
        xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
        xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
      } else {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float theta = (float) Math.atan2(dy, dx);
        xform.rotate(theta);
        float dist = (float) p1.distance(p2);
        if (edgeShape instanceof Path2D) {
          xform.scale(dist, dist);
        } else {
          xform.scale(dist, 1.0);
        }
      }
      edgeShape = xform.createTransformedShape(edgeShape);
      return nonEmpty(edgeShape.getBounds2D(), NON_EMPTY_DELTA);
    }

    @Override
    public Rectangle2D getForElement(E element, Point p) {
      return getForElement(element, p, p);
    }

    public Rectangle2D getForElement(E edge, Point p1, Point p2) {
      V v1 = graph.getEdgeSource(edge);
      V v2 = graph.getEdgeTarget(edge);
      float x1 = (float) p1.x;
      float y1 = (float) p1.y;
      float x2 = (float) p2.x;
      float y2 = (float) p2.y;

      boolean isLoop = v1.equals(v2);
      Shape s2 = vertexShapeFunction.apply(v2);
      Shape edgeShape = edgeShapeFunction.apply(Context.getInstance(layoutModel.getGraph(), edge));

      AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

      if (isLoop) {
        Rectangle2D s2Bounds = s2.getBounds2D();
        xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
        xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
      } else {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float theta = (float) Math.atan2(dy, dx);
        xform.rotate(theta);
        float dist = (float) p1.distance(p2);
        if (edgeShape instanceof Path2D) {
          xform.scale(dist, dist);
        } else {
          xform.scale(dist, 1.0);
        }
      }
      edgeShape = xform.createTransformedShape(edgeShape);
      return nonEmpty(edgeShape.getBounds2D(), NON_EMPTY_DELTA);
    }

    public void compute() {
      super.compute();

      for (E e : graph.edgeSet()) {
        V v1 = graph.getEdgeSource(e);
        V v2 = graph.getEdgeTarget(e);
        Point p1 = (Point) layoutModel.apply(v1);
        Point p2 = (Point) layoutModel.apply(v2);
        float x1 = (float) p1.x;
        float y1 = (float) p1.y;
        float x2 = (float) p2.x;
        float y2 = (float) p2.y;

        boolean isLoop = v1.equals(v2);
        Shape s2 = vertexShapeFunction.apply(v2);
        Shape edgeShape = edgeShapeFunction.apply(Context.getInstance(layoutModel.getGraph(), e));

        AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

        if (isLoop) {
          Rectangle2D s2Bounds = s2.getBounds2D();
          xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
          xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
        } else {
          float dx = x2 - x1;
          float dy = y2 - y1;
          float theta = (float) Math.atan2(dy, dx);
          xform.rotate(theta);
          float dist = (float) p1.distance(p2);
          if (edgeShape instanceof Path2D) {
            xform.scale(dist, dist);
          } else {
            xform.scale(dist, 1.0);
          }
        }
        edgeShape = xform.createTransformedShape(edgeShape);
        rectangles.add(nonEmpty(edgeShape.getBounds2D(), NON_EMPTY_DELTA));
      }
    }

    /**
     * a zero width or height Rectangle2D will never contain/intersect anything. A vertical or
     * horizontal Line edge will have a bounding rectangle of zero width or height. Give any such
     * bounding rectangles a non-zero volume so that contained edges are not lost
     *
     * @param r Rectangle to check for empty size
     * @param delta how much to increase siz by
     * @return a non-empty Rectangle
     */
    private Rectangle2D nonEmpty(Rectangle2D r, double delta) {
      if (r.getHeight() == 0) {
        r.setFrame(r.getX(), r.getY(), r.getWidth(), delta);
      }
      if (r.getWidth() == 0) {
        r.setFrame(r.getX(), r.getY(), delta, r.getHeight());
      }
      return r;
    }
  }

  public abstract Rectangle2D getForElement(T element);

  public abstract Rectangle2D getForElement(T element, Point p);

  public abstract Rectangle2D getForElement(T element, Point p1, Point p2);

  /** @return the rectangles */
  public List<Rectangle2D> getRectangles() {
    return rectangles;
  }

  public void compute() {
    rectangles.clear();
  }
}
