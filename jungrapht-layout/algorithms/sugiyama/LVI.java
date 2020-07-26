package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Objects;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.synthetics.SVI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a vertex type for a layered layout algorithm.<br>
 * Instances of LVI&lt;V&gt; replace instances of V during layout The LVI&lt;V&gt; holds metadata
 * information about the position of the vertex in the layered graph.
 *
 * @param <V>
 */
public class LVI<V> extends SVI<V> implements LV<V> {

  Logger log = LoggerFactory.getLogger(LVI.class);

  protected int rank; // the layer number for this vertex
  protected int index; // the index within the layer array for this vertex
  protected int pos = -1;
  protected double measure = -1; // the median of the positions of the neighbors of this LV

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

  protected LVI(V vertex, int rank, int index, int pos, double measure, Point p) {
    super(vertex);
    this.rank = rank;
    this.index = index;
    this.pos = pos;
    this.measure = measure;
    this.p = p;
  }

  public LVI(LVI<V> other) {
    this(other.vertex, other.rank, other.index, other.pos, other.measure, other.p);
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

  public int getPos() {
    return pos;
  }

  public void setPos(int pos) {
    this.pos = pos;
  }

  public double getMeasure() {
    return measure;
  }

  public void setMeasure(double measure) {
    this.measure = measure;
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
    return "LVI{"
        + "vertex="
        + vertex
        + ", rank="
        + rank
        + ", index="
        + index
        + ", pos="
        + pos
        + ", measure="
        + measure
        + ", p="
        + p
        + '}';
  }
}
