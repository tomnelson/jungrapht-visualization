package org.jungrapht.visualization.layout.algorithms.orthogonal;

import org.jungrapht.visualization.layout.model.Rectangle;

import java.util.Objects;

public class Cell<V> {

  protected V occupant;
  protected Rectangle rectangle;

  protected Cell(V occupant, Rectangle rectangle) {
    this.occupant = occupant;
    this.rectangle = rectangle;
  }

  /**
   * @param x location of upper left corner
   * @param y location of upper left corner
   * @param width size in x dimension
   * @param height size in y dimension
   * @return a new Rectangle with the passed properties
   */
  public static <V> Cell of(V v, double x, double y, double width, double height) {
    return new Cell(v, Rectangle.of(x, y, width, height));
  }

  public static <V> Cell of(V v, Rectangle r) {
    return new Cell(v, r);
  }

  public static Cell of(double width, double height) {
    return new Cell(null, Rectangle.of(0, 0, width, height));
  }

  public V getOccupant() {
    return occupant;
  }

  public void setOccupant(V occupant) {
    this.occupant = occupant;
  }

  public Rectangle getRectangle() {
    return rectangle;
  }

  public void setRectangle(Rectangle rectangle) {
    this.rectangle = rectangle;
  }

  public double getX() {
    return rectangle.x;
  }

  public double getY() {
    return rectangle.y;
  }

  public double getWidth() {
    return rectangle.width;
  }

  public double getHeight() {
    return rectangle.height;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Cell<?> cell = (Cell<?>) o;
    return occupant.equals(cell.occupant);// && rectangle.equals(cell.rectangle);
  }

  @Override
  public int hashCode() {
    return Objects.hash(occupant);
  }

  @Override
  public String toString() {
    return "Cell{" + "occupant=" + occupant + ", rectangle=" + rectangle + '}';
  }
}
