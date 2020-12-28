package org.jungrapht.visualization.decorators;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jungrapht.visualization.renderers.AbstractEdgeRenderer;

/**
 * A special case of {@code Shape} that is used by the {@link AbstractEdgeRenderer} to expand the
 * unit edge shape in both the x and y axes instead of only in the x axis. This is used by the
 * Articulated edges of the mincross layouts and the (future) orthogonal edges
 */
public class ExpandXY implements Shape {

  protected Shape delegate;

  public static ExpandXY of(Shape delegate) {
    return new ExpandXY(delegate);
  }

  ExpandXY(Shape delegate) {
    this.delegate = delegate;
  }

  @Override
  public Rectangle getBounds() {
    return delegate.getBounds();
  }

  @Override
  public Rectangle2D getBounds2D() {
    return delegate.getBounds2D();
  }

  @Override
  public boolean contains(double x, double y) {
    return delegate.contains(x, y);
  }

  @Override
  public boolean contains(Point2D p) {
    return delegate.contains(p);
  }

  @Override
  public boolean intersects(double x, double y, double w, double h) {
    return delegate.intersects(x, y, w, h);
  }

  @Override
  public boolean intersects(Rectangle2D r) {
    return delegate.intersects(r);
  }

  @Override
  public boolean contains(double x, double y, double w, double h) {
    return delegate.contains(x, y, w, h);
  }

  @Override
  public boolean contains(Rectangle2D r) {
    return delegate.contains(r);
  }

  @Override
  public PathIterator getPathIterator(AffineTransform at) {
    return delegate.getPathIterator(at);
  }

  @Override
  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return delegate.getPathIterator(at, flatness);
  }
}
