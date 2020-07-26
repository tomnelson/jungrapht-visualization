package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Holds the metadata for LVI vertices as an alternative to copying them */
public class VertexMetadata<V> {

  Logger log = LoggerFactory.getLogger(VertexMetadata.class);

  protected int rank; // the layer number for this vertex
  protected int index; // the index within the layer array for this vertex
  protected int pos = -1;
  protected double measure = -1; // the median of the positions of the neighbors of this LV

  /**
   * store the metadata for the supplied vertex
   *
   * @param vertex
   * @param <V>
   * @return
   */
  public static <V> VertexMetadata<V> of(LV<V> vertex) {
    return new VertexMetadata<>(vertex);
  }

  /**
   * apply the saved metadata to the supplied vertex
   *
   * @param v
   */
  public void applyTo(LV<V> v) {
    v.setRank(this.rank);
    v.setIndex(this.index);
    v.setPos(this.pos);
    v.setMeasure(this.measure);
  }

  VertexMetadata(LV<V> vertex) {
    this.rank = vertex.getRank();
    this.index = vertex.getIndex();
    this.pos = vertex.getPos();
    this.measure = vertex.getMeasure();
  }

  //  @Override
  //  public boolean equals(Object o) {
  //    if (this == o) return true;
  //    if (o == null || getClass() != o.getClass()) return false;
  //    VertexMetadata<?> sugiyamaVertex = (VertexMetadata<?>) o;
  //    if (o instanceof SyntheticLV) {
  //      return false;
  //    }
  //    return Objects.equals(vertex, sugiyamaVertex.vertex);
  //  }
  //
  //  @Override
  //  public int hashCode() {
  //    return Objects.hash(vertex);
  //  }

  @Override
  public String toString() {
    return "VertexMetadata{"
        + "rank="
        + rank
        + ", index="
        + index
        + ", pos="
        + pos
        + ", measure="
        + measure
        + '}';
  }
}
