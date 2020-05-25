package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a vertex that is not in the original graph, but is synthesized in order to position bends in the
 * articulated edges of the SugiyamaLayoutAlgorithm A QVertex is the bottom vertex of a vertical
 * segment edge
 *
 * @param <V> vertex type
 */
class QVertex<V> extends SegmentVertex<V> {

  private static final Logger log = LoggerFactory.getLogger(QVertex.class);

  public static <V> QVertex<V> of() {
    return new QVertex();
  }

  protected QVertex() {
    super();
  }

  public QVertex(QVertex<V> other) {
    super(other);
  }

  public QVertex<V> copy() {
    return new QVertex<>(this);
  }

  @Override
  public void setPos(int pos) {
    super.setPos(pos);
  }

  public void setSegmentVertexPos(int pos) {
    segment.pVertex.setPos(pos);
  }

  @Override
  public String toString() {
    return "QVertex{"
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
