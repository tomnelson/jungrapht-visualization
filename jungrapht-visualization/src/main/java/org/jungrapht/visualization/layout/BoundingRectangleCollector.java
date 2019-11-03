package org.jungrapht.visualization.layout;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * collects the {@link Rectangle2D}s that bound all of the elements of a {@code Graph}
 *
 * @param <T> the type to collect bounds for, either vertices or edges (see subclasses)
 */
public abstract class BoundingRectangleCollector<T> {

  protected RenderContext rc;
  protected VisualizationModel visualizationModel;
  protected List<Rectangle2D> rectangles = new ArrayList<>();

  public BoundingRectangleCollector(RenderContext rc, VisualizationModel visualizationModel) {
    this.rc = rc;
    this.visualizationModel = visualizationModel;
    compute();
  }

  public static class Points<V> extends BoundingRectangleCollector<V> {
    private static final Logger log =
        LoggerFactory.getLogger(BoundingRectangleCollector.Points.class);

    public Points(RenderContext rc, VisualizationModel visualizationModel) {
      super(rc, visualizationModel);
    }

    public Rectangle2D getForElement(V vertex) {
      Shape shape = new Rectangle2D.Double();
      Point p = (Point) visualizationModel.getLayoutModel().apply(vertex);

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
      Shape shape = (Shape) rc.getVertexShapeFunction().apply(vertex);
      log.trace("vertex is at {}", p);

      float x = (float) p.x;
      float y = (float) p.y;
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      return xform.createTransformedShape(shape).getBounds2D();
    }

    public void compute(Collection vertices) {
      super.compute();

      for (Object v : vertices) {
        Shape shape = (Shape) rc.getVertexShapeFunction().apply(v);
        Point2D p = (Point2D) visualizationModel.getLayoutModel().apply(v);
        //			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        float x = (float) p.getX();
        float y = (float) p.getY();
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
        shape = xform.createTransformedShape(shape);
        rectangles.add(shape.getBounds2D());
      }
    }

    public void compute() {
      super.compute();

      for (Object v : visualizationModel.getGraph().vertexSet()) {
        Shape shape = (Shape) rc.getVertexShapeFunction().apply(v);
        Point2D p = (Point2D) visualizationModel.getLayoutModel().apply(v);
        //			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        float x = (float) p.getX();
        float y = (float) p.getY();
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
  public static class Vertices<V> extends BoundingRectangleCollector<V> {
    private static final Logger log = LoggerFactory.getLogger(Vertices.class);

    public Vertices(RenderContext rc, VisualizationModel visualizationModel) {
      super(rc, visualizationModel);
    }

    public Rectangle2D getForElement(V vertex) {
      Shape shape = (Shape) rc.getVertexShapeFunction().apply(vertex);
      Point p = (Point) visualizationModel.getLayoutModel().apply(vertex);

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
      Shape shape = (Shape) rc.getVertexShapeFunction().apply(vertex);
      //      Point2D p = (Point2D) layoutModel.apply(vertex);
      log.trace("vertex is at {}", p);

      float x = (float) p.x;
      float y = (float) p.y;
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      return xform.createTransformedShape(shape).getBounds2D();
    }

    public void compute(Collection vertices) {
      super.compute();

      for (Object v : vertices) {
        Shape shape = (Shape) rc.getVertexShapeFunction().apply(v);
        Point p = (Point) visualizationModel.getLayoutModel().apply(v);
        //			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        float x = (float) p.x;
        float y = (float) p.y;
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
        shape = xform.createTransformedShape(shape);
        rectangles.add(shape.getBounds2D());
      }
    }

    public void compute() {
      super.compute();

      for (Object v : visualizationModel.getGraph().vertexSet()) {
        Shape shape = (Shape) rc.getVertexShapeFunction().apply(v);
        Point p = (Point) visualizationModel.getLayoutModel().apply(v);
        //			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
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
  public static class Edges<E> extends BoundingRectangleCollector<E> {

    private static final double NON_EMPTY_DELTA = 0.001;

    public Edges(RenderContext rc, VisualizationModel visualizationModel) {
      super(rc, visualizationModel);
    }

    public Rectangle2D getForElement(E edge) {
      Object v1 = visualizationModel.getGraph().getEdgeSource(edge);
      Object v2 = visualizationModel.getGraph().getEdgeTarget(edge);
      Point p1 = (Point) visualizationModel.getLayoutModel().apply(v1);
      Point p2 = (Point) visualizationModel.getLayoutModel().apply(v2);
      float x1 = (float) p1.x;
      float y1 = (float) p1.y;
      float x2 = (float) p2.x;
      float y2 = (float) p2.y;

      boolean isLoop = v1.equals(v2);
      Shape s2 = (Shape) rc.getVertexShapeFunction().apply(v2);
      Shape edgeShape =
          (Shape)
              rc.getEdgeShapeFunction()
                  .apply(Context.getInstance(visualizationModel.getGraph(), edge));

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
      Object v1 = visualizationModel.getGraph().getEdgeSource(edge);
      Object v2 = visualizationModel.getGraph().getEdgeTarget(edge);
      float x1 = (float) p1.x;
      float y1 = (float) p1.y;
      float x2 = (float) p2.x;
      float y2 = (float) p2.y;

      boolean isLoop = v1.equals(v2);
      Shape s2 = (Shape) rc.getVertexShapeFunction().apply(v2);
      Shape edgeShape =
          (Shape)
              rc.getEdgeShapeFunction()
                  .apply(Context.getInstance(visualizationModel.getGraph(), edge));

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

      for (Object e : visualizationModel.getGraph().edgeSet()) {
        Object v1 = visualizationModel.getGraph().getEdgeSource(e);
        Object v2 = visualizationModel.getGraph().getEdgeTarget(e);
        Point p1 = (Point) visualizationModel.getLayoutModel().apply(v1);
        Point p2 = (Point) visualizationModel.getLayoutModel().apply(v2);
        float x1 = (float) p1.x;
        float y1 = (float) p1.y;
        float x2 = (float) p2.x;
        float y2 = (float) p2.y;

        boolean isLoop = v1.equals(v2);
        Shape s2 = (Shape) rc.getVertexShapeFunction().apply(v2);
        Shape edgeShape =
            (Shape)
                rc.getEdgeShapeFunction()
                    .apply(Context.getInstance(visualizationModel.getGraph(), e));

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
