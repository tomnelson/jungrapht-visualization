package org.jungrapht.visualization.layout.model;

import java.util.Objects;

public class Dimension {
  public final int width;
  public final int height;

  public static Dimension of(int width, int height) {
    return new Dimension(width, height);
  }

  private Dimension(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public Dimension union(Dimension other) {
    return new Dimension(Math.max(width, other.width), Math.min(height, other.height));
  }

  public Dimension intersection(Dimension other) {
    return new Dimension(Math.min(width, other.width), Math.min(height, other.height));
  }

  @Override
  public String toString() {
    return "Dimension{" + "width=" + width + ", height=" + height + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dimension dimension = (Dimension) o;
    return width == dimension.width && height == dimension.height;
  }

  @Override
  public int hashCode() {
    return Objects.hash(width, height);
  }
}
