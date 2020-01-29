package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Objects;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.synthetics.SVI;

/**
 * a vertex type for the SugiyamaLayoutAlgorithm instances of LVI<V> replace instances of V during
 * layout The LVI<V> holds metadata information about the position of the vertex in the layered
 * graph for the SugiyamaLayoutAlgorithm
 *
 * @param <V>
 */
public class LVI<V> extends SVI<V> implements LV<V> {
  protected int rank; // the layer number for this vertex
  protected int index; // the position within the layer for this vertex
  //  protected int pos = -1;
  //  protected int measure = -1;

  protected Point p; // the cartesian coordinates for this articulation point

  protected LVI() {
    super();
  }

  protected LVI(V vertex) {
    super(vertex);
  }

  protected LVI(V vertex, int rank, int index) {
    super(vertex);
    this.rank = rank;
    this.index = index;
  }

  protected LVI(V vertex, int rank, int index, Point p) {
    super(vertex);
    this.rank = rank;
    this.index = index;
    this.p = p;
  }

  public LVI(LVI<V> other) {
    this(other.vertex, other.rank, other.index, other.p);
  }

  public <T extends LV<V>> T copy() {
    return (T) new LVI<>(this);
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
    LVI<?> sugiyamaVertex = (LVI<?>) o;
    if (o instanceof SyntheticLV) {
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
        //        + ", pos="
        //        + pos
        //        + ", measure="
        //        + measure
        + ", p="
        + p
        + '}';
  }
}
