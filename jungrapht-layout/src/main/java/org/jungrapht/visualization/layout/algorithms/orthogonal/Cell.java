package org.jungrapht.visualization.layout.algorithms.orthogonal;

import org.jungrapht.visualization.layout.model.Rectangle;

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
  public String toString() {
    return "Cell{" + "occupant=" + occupant + ", rectangle=" + rectangle + '}';
  }
}
