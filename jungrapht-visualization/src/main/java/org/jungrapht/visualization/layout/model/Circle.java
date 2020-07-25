package org.jungrapht.visualization.layout.model;

/**
 * Simple, immutable Circle class
 *
 * @author Tom Nelson
 */
public class Circle {

  /** the center Point of this circle */
  public final Point center;
  /** the radius of this Circle */
  public final double radius;

  /** a Circle at the origin (0,0) with radius 1 */
  public static final Circle UNIT = new Circle(Point.ORIGIN, 1);

  public static Circle of(Point center, double radius) {
    return new Circle(center, radius);
  }

  /**
   * Create an instance with passed parameters
   *
   * @param center center Point
   * @param radius edge distance from center
   */
  public Circle(Point center, double radius) {
    this.center = center;
    this.radius = radius;
  }

  /**
   * @param p a Point to test
   * @return true if the passed Point is within this Circle
   */
  public boolean contains(Point p) {
    return p.inside(center.x - radius, center.y - radius, center.x + radius, center.y + radius)
        && center.distance(p) <= radius;
  }

  /**
   * @param r a Rectangle to test
   * @return true if there is a non-zero intersection of the Rectangle with this Circle
   */
  public boolean intersects(Rectangle r) {
    // quick fail with bounding box test first
    if (r.x > center.x + radius) return false;
    if (r.y > center.y + radius) return false;
    if (r.maxX < center.x - radius) return false;
    if (r.maxY < center.y - radius) return false;

    return squaredDistance(center, r) < radius * radius;
  }

  /**
   * Compute the squared distance (avoid sqrt operation) between the passed Point and Rectangle
   *
   * @param p a Point to test
   * @param r rectangle to test
   * @return the distance squared
   */
  private static double squaredDistance(Point p, Rectangle r) {
    double distSq = 0;
    double cx = p.x;
    if (cx < r.x) {
      distSq += (r.x - cx) * (r.x - cx);
    }
    if (cx > r.maxX) {
      distSq += (cx - r.maxX) * (cx - r.maxX);
    }
    double cy = p.y;
    if (cy < r.y) {
      distSq += (r.y - cy) * (r.y - cy);
    }
    if (cy > r.maxY) {
      distSq += (cy - r.maxY) * (cy - r.maxY);
    }
    return distSq;
  }
}
