package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jungrapht.visualization.layout.util.synthetics.Synthetic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a vertex that is not in the original graph, but is synthesized in order to position bends in the
 * articulated edges of the SugiyamaLayoutAlgorithm
 *
 * @param <V> vertex type
 */
abstract class SegmentVertex<V> extends SyntheticLV<V> implements Synthetic {

  private static final Logger log = LoggerFactory.getLogger(SegmentVertex.class);

  final int hash;

  protected Segment<V> segment;

  protected SegmentVertex() {
    super();
    this.hash = System.identityHashCode(this);
  }

  public SegmentVertex(SegmentVertex<V> other) {
    super(other);
    this.hash = other.hash;
  }

  public Segment<V> getSegment() {
    return segment;
  }

  public void setSegment(Segment<V> segment) {
    this.segment = segment;
  }

  @Override
  public void setPos(int pos) {
    super.setPos(pos);
  }

  public abstract void setSegmentVertexPos(int pos);

  @Override
  public boolean equals(Object o) {
    if (o instanceof SegmentVertex) {
      return hash == ((SegmentVertex) o).hash;
    } else if (o instanceof SyntheticLV) {
      return hash == ((SyntheticLV) o).hash;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    return "SegmentVertex{"
        + "vertex="
        + hashCode()
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
