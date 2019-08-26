package org.jungrapht.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.Map;

/**
 * A comparator to compare along the x-axis, Vertices where the values are Rectangle2D are compared
 * with the center x values
 *
 * @author Tom Nelson
 * @param <T>
 */
public class HorizontalCenterNodeComparator<T> implements Comparator<Map.Entry<T, Rectangle2D>> {

  /**
   * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer
   * as the first argument is less than, equal to, or greater than the second.
   *
   * @param left the first object to be compared.
   * @param right the second object to be compared.
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *     equal to, or greater than the second.
   * @throws NullPointerException if an argument is null and this comparator does not permit null
   *     arguments
   * @throws ClassCastException if the arguments' types prevent them from being compared by this
   *     comparator.
   */
  public int compare(Rectangle2D left, Rectangle2D right) {
    return Double.compare(left.getCenterX(), right.getCenterX());
  }

  @Override
  public int compare(Map.Entry<T, Rectangle2D> leftVertex, Map.Entry<T, Rectangle2D> rightVertex) {
    return compare(leftVertex.getValue(), rightVertex.getValue());
  }

  //  @Override
  //  public int compare(Node<T> leftVertex, Node<T> rightVertex) {
  //    return compare(leftVertex.getBounds(), rightVertex.getBounds());
  //  }
}
