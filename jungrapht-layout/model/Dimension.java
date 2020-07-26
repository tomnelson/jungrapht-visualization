package org.jungrapht.visualization.layout.model;

import java.util.Objects;

/**
 * Holds a width and height pair. Used to limit awt dependencies. Replaced by Rectangle in
 * TreeLayouts
 */
public class Dimension {
  /** horizontal expanse */
  public final int width;
  /** vertical expanse */
  public final int height;

  /**
   * @param width horizontal expanse
   * @param height vertical expanse
   * @return a new Dimension with the passed parameters
   */
  public static Dimension of(int width, int height) {
    return new Dimension(width, height);
  }

  /**
   * @param width horizontal expanse
   * @param height vertical expanse
   */
  private Dimension(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * create a new Dimension with sufficient expanse to also include other
   *
   * @param other another Dimension to augment this with
   * @return a new Dimension with sufficient expanse to also include other
   */
  public Dimension union(Dimension other) {
    return new Dimension(Math.max(width, other.width), Math.min(height, other.height));
  }

  /**
   * @param other another Dimension to compare this with
   * @return a new Dimension of the intersection of this with other
   */
  public Dimension intersection(Dimension other) {
    return new Dimension(Math.min(width, other.width), Math.min(height, other.height));
  }

  /** @return a String representation of this */
  @Override
  public String toString() {
    return "Dimension{" + "width=" + width + ", height=" + height + '}';
  }

  /**
   * @param o another object for comparison
   * @return true if this and the other are both Rectangles with the same width/height
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dimension dimension = (Dimension) o;
    return width == dimension.width && height == dimension.height;
  }

  /** @return a hash of the parameters */
  @Override
  public int hashCode() {
    return Objects.hash(width, height);
  }
}
