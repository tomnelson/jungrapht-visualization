package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.Objects;
import org.jungrapht.visualization.layout.model.Point;

/**
 * a vertex type for the SugiyamaLayoutAlgorithm instances of SV<V> replace instances of V during
 * layout The SV<V> holds state information about the position of the vertex in the layered graph
 * for the SugiyamaLayoutAlgorithm
 *
 * @param <V>
 */
public class SV<V> {
  public final V vertex;
  protected int rank;
  protected int index;
  protected Point p;

  public static <V> SV<V> of(V vertex) {
    return new SV(vertex);
  }

  public static <V> SV<V> of(V vertex, int rank, int index) {
    return new SV(vertex, rank, index);
  }

  protected SV() {
    this.vertex = null;
  }

  protected SV(V vertex) {
    this.vertex = vertex;
  }

  protected SV(V vertex, int rank, int index) {
    this.vertex = vertex;
    this.rank = rank;
    this.index = index;
  }

  protected SV(V vertex, int rank, int index, Point p) {
    this.vertex = vertex;
    this.rank = rank;
    this.index = index;
    this.p = p;
  }

  public SV(SV<V> other) {
    this(other.vertex, other.rank, other.index, other.p);
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public int getRank() {
    return rank;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  public Point getPoint() {
    return p;
  }

  public void setPoint(Point p) {
    this.p = p;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SV<?> sv = (SV<?>) o;
    if (o instanceof SyntheticVertex) {
      return false;
    }
    return Objects.equals(vertex, sv.vertex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vertex);
  }

  @Override
  public String toString() {
    return "SV{" + "vertex=" + vertex + ", rank=" + rank + ", index=" + index + ", p=" + p + '}';
  }
}
