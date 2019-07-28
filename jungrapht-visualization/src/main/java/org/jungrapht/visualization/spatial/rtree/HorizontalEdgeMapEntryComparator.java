package org.jungrapht.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.Map;

/**
 * A comparator to compare along the x-axis, Map.Entries where the values are Rectangle2D First
 * compare the min x values, then the max x values
 *
 * @author Tom Nelson
 * @param <T>
 */
public class HorizontalEdgeMapEntryComparator<T> implements Comparator<Map.Entry<T, Rectangle2D>> {
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
      if (left.getMaxX() == right.getMaxX()) return 0;
      if (left.getMaxX() < right.getMaxX()) return -1;
      else return 1;
    } else {
      if (left.getMinX() < right.getMinX()) return -1;
      return 1;
    }
  }

  @Override
  public int compare(Map.Entry<T, Rectangle2D> leftVertex, Map.Entry<T, Rectangle2D> rightVertex) {
    return compare(leftVertex.getValue(), rightVertex.getValue());
  }

  public int compare(Node<?> leftVertex, Node<?> rightVertex) {
    return compare(leftVertex.getBounds(), rightVertex.getBounds());
  }
}
