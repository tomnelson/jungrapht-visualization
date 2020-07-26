package org.jungrapht.visualization.layout.model;

import java.util.Objects;

/**
 * Simple, immutable Rectangle class, included to reduce dependency on awt classes
 *
 * @author Tom Nelson
 */
public class Rectangle {

  /** x location of upper-left corner */
  public final double x;
  /** y location of upper left corner */
  public final double y;
  /** width (x dimension) */
  public final double width;
  /** height (y dimension */
  public final double height;
  /** x location of lower right corner */
  public final double maxX;
  /** y location of lower right corner */
  public final double maxY;

  /** identity rectangle of zero size at origin */
  public static Rectangle IDENTITY = new Rectangle(0, 0, 0, 0);

  /**
   * @param x location of upper left corner
   * @param y location of upper left corner
   * @param width size in x dimension
   * @param height size in y dimension
   * @return a new Rectangle with the passed properties
   */
  public static Rectangle of(int x, int y, int width, int height) {
    return new Rectangle(x, y, width, height);
  }

  public static Rectangle of(int width, int height) {
    return new Rectangle(0, 0, width, height);
  }

  /**
   * @param x location of upper left corner
   * @param y location of upper left corner
   * @param width size in x dimension
   * @param height size in y dimension
   * @return a new Rectangle with the passed properties
   */
  public static Rectangle of(double x, double y, double width, double height) {
    return new Rectangle(x, y, width, height);
  }

  public static Rectangle from(Point min, Point max) {
    return new Rectangle(min.x, min.y, max.x - min.x, max.y - min.y);
  }

  /**
   * @param x left most x location
   * @param y top most y location
   * @param width horizontal size of rectangle when aligned
   * @param height vertical size of rectangle when aligned
   */
  public Rectangle(double x, double y, double width, double height) {
    if (width < 0 || height < 0)
      throw new IllegalArgumentException("width and height must be non-negative");
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.maxX = x + width;
    this.maxY = y + height;
  }

  /** @return the x coordinate of the center of this Rectangle */
  public double getCenterX() {
    return x + width / 2;
  }

  /** @return the y coordinate of the center of this Rectangle */
  public double getCenterY() {
    return y + height / 2;
  }

  /**
   * fail-fast implementation to reduce computation
   *
   * @param other another Rectangle to compare
   * @return whether there is a non-zero intersection of the 2 shapes
   */
  public boolean intersects(Rectangle other) {
    return maxX >= other.x && other.maxX >= x && maxY >= other.y && other.maxY >= y;
  }

  /**
   * @param p point to test
   * @return true if the coordinate is contained, false otherwise
   */
  public boolean contains(Point p) {
    return contains(p.x, p.y);
  }

  public Point min() {
    return Point.of(x, y);
  }

  public Point max() {
    return Point.of(x, y).add(width, height);
  }

  /**
   * Fail fast for the most common case where the point coordinates are not contained.
   * Implementation leaves space for debug breakpoints
   *
   * @param ox coodinate to test
   * @param oy coordinate to test
   * @return true if the coordinate is contained, false otherwise
   */
  public boolean contains(double ox, double oy) {
    if (ox < x) return false;
    if (ox > maxX) return false;
    if (oy < y) return false;
    return !(oy > maxY);
  }

  /**
   * offset the rectangle's location by the passed coordinates
   *
   * @param x horizontal offset
   * @param y vertical offset
   * @return a new Rectangle offset by the passed coordinates
   */
  public Rectangle offset(double x, double y) {
    return new Rectangle(this.x + x, this.y + y, this.width, this.height);
  }

  /**
   * return a new Rectangle that is is the expansion of this Rectangle to contain the passed Point
   *
   * @param newX x coordinate of expansion point
   * @param newY y coordinate of expansion point
   * @return a new Rectangle that was expanded to include the passed coordinates
   */
  public Rectangle union(double newX, double newY) {
    double x1 = Math.min(x, newX);
    double x2 = Math.max(maxX, newX);
    double y1 = Math.min(y, newY);
    double y2 = Math.max(maxY, newY);
    return new Rectangle(x1, y1, x2 - x1, y2 - y1);
  }

  /**
   * return a union of this rectangle and the other rectangle
   *
   * @param other
   * @return
   */
  public Rectangle union(Rectangle other) {
    double minX = Math.min(this.x, other.x);
    double minY = Math.min(this.y, other.y);
    double maxX = Math.max(this.maxX, other.maxX);
    double maxY = Math.max(this.maxY, other.maxY);
    return Rectangle.of(minX, minY, maxX - minX, maxY - minY);
  }

  /**
   * Compare for equality
   *
   * @param o the object to compare
   * @return true if the other Object is a Rectangle with identical properties to this Rectangle
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Rectangle rectangle = (Rectangle) o;
    return Double.compare(rectangle.x, x) == 0
        && Double.compare(rectangle.y, y) == 0
        && Double.compare(rectangle.width, width) == 0
        && Double.compare(rectangle.height, height) == 0
        && Double.compare(rectangle.maxX, maxX) == 0
        && Double.compare(rectangle.maxY, maxY) == 0;
  }

  /** @return a hash of the Rectangle properties */
  @Override
  public int hashCode() {
    return Objects.hash(x, y, width, height, maxX, maxY);
  }

  /** @return a String representation of this Rectangle */
  @Override
  public String toString() {
    return "Rectangle{"
        + "x="
        + x
        + ", y="
        + y
        + ", width="
        + width
        + ", height="
        + height
        + ", maxX="
        + maxX
        + ", maxY="
        + maxY
        + '}';
  }
}
