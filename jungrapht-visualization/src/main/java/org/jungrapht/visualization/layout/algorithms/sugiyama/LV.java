package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.synthetics.SV;

/**
 * a vertex type for the SugiyamaLayoutAlgorithm instances of LV<V> replace instances of V during
 * layout The LV<V> holds metadata information about the position of the vertex in the layered graph
 * for the SugiyamaLayoutAlgorithm
 *
 * @param <V>
 */
public interface LV<V> extends SV<V> {

  static <V> LV<V> of(V vertex) {
    return new LVI(vertex);
  }

  static <V> LV<V> of(V vertex, int rank, int index) {
    return new LVI(vertex, rank, index);
  }

  <T extends LV<V>> T copy();

  void setRank(int rank);

  int getRank();

  void setIndex(int index);

  int getIndex();

  Point getPoint();

  void setPoint(Point p);
}
