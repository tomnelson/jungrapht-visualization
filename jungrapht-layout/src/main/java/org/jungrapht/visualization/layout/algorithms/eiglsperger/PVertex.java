package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a vertex that is not in the original graph, but is synthesized in order to position bends in the
 * articulated edges of the SugiyamaLayoutAlgorithm a PVertex is the top vertex of a vertical edge
 * segment
 *
 * @param <V> vertex type
 */
class PVertex<V> extends SegmentVertex<V> {

  private static final Logger log = LoggerFactory.getLogger(PVertex.class);

  public static <V> PVertex<V> of() {
    return new PVertex();
  }

  protected PVertex() {
    super();
  }

  public PVertex<V> copy() {
    return new PVertex<>(this);
  }

  public PVertex(PVertex<V> other) {
    super(other);
  }

  public void setSegmentVertexPos(int pos) {
    segment.qVertex.setPos(pos);
  }

  @Override
  public void setPos(int pos) {
    super.setPos(pos);
  }

  @Override
  public String toString() {
    return "PVertex{"
        + "vertex="
        + hashCode()
        + ", segment="
        + segment
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
