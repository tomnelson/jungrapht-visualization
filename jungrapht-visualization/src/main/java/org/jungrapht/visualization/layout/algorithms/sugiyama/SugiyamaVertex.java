package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.Objects;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.synthetics.SV;

/**
 * a vertex type for the SugiyamaLayoutAlgorithm instances of SV<V> replace instances of V during
 * layout The SV<V> holds state information about the position of the vertex in the layered graph
 * for the SugiyamaLayoutAlgorithm
 *
 * @param <V>
 */
public class SugiyamaVertex<V> extends SV<V> {
  protected int rank;
  protected int index;
  protected Point p;

  public static <V> SugiyamaVertex<V> of(V vertex) {
    return new SugiyamaVertex(vertex);
  }

  public static <V> SugiyamaVertex<V> of(V vertex, int rank, int index) {
    return new SugiyamaVertex(vertex, rank, index);
  }

  protected SugiyamaVertex() {
    super();
  }

  protected SugiyamaVertex(V vertex) {
    super(vertex);
  }

  protected SugiyamaVertex(V vertex, int rank, int index) {
    super(vertex);
    this.rank = rank;
    this.index = index;
  }

  protected SugiyamaVertex(V vertex, int rank, int index, Point p) {
    super(vertex);
    this.rank = rank;
    this.index = index;
    this.p = p;
  }

  public SugiyamaVertex(SugiyamaVertex<V> other) {
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
    SugiyamaVertex<?> sugiyamaVertex = (SugiyamaVertex<?>) o;
    if (o instanceof SyntheticVertex) {
      return false;
    }
    return Objects.equals(vertex, sugiyamaVertex.vertex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vertex);
  }

  @Override
  public String toString() {
    return "SugiyamaVertex{"
        + "vertex="
        + vertex
        + ", rank="
        + rank
        + ", index="
        + index
        + ", p="
        + p
        + '}';
  }
}
