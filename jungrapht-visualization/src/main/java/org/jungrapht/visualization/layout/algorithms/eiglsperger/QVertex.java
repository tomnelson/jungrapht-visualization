package org.jungrapht.visualization.layout.algorithms.eiglsperger;

/**
 * a vertex that is not in the original graph, but is synthesized in order to position bends in the
 * articulated edges of the SugiyamaLayoutAlgorithm
 *
 * @param <V> vertex type
 */
class QVertex<V> extends SegmentVertex<V> {

  public static <V> QVertex<V> of() {
    return new QVertex();
  }

  protected QVertex() {
    super();
  }

  public QVertex(QVertex<V> other) {
    super(other);
  }

  public void setSegmentVertexPos(int pos) {
    segment.pVertex.setPos(pos);
  }

  @Override
  public String toString() {
    return "QVertex{"
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
