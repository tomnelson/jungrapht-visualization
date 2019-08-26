package org.jungrapht.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.Map;

/**
 * A comparator to compare along the x-axis, Vertices where the values are Rectangle2D First compare
 * the min x values, then the max x values
 *
 * @author Tom Nelson
 * @param <T>
 */
public class HorizontalEdgeNodeComparator<T> implements Comparator<Node<T>> {

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
    if (left.getMinX() == right.getMinX()) {
      return Double.compare(left.getMaxX(), right.getMaxX());
    } else {
      if (left.getMinX() < right.getMinX()) return -1;
      return 1;
    }
  }

  public int compare(Map.Entry<?, Rectangle2D> leftVertex, Map.Entry<?, Rectangle2D> rightVertex) {
    return compare(leftVertex.getValue(), rightVertex.getValue());
  }

  @Override
  public int compare(Node<T> leftVertex, Node<T> rightVertex) {
    return compare(leftVertex.getBounds(), rightVertex.getBounds());
  }
}
