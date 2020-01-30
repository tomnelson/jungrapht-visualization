package org.jungrapht.visualization.layout.algorithms.eiglsperger;

/**
 * a vertex that is not in the original graph, but is synthesized in order to position bends in the
 * articulated edges of the SugiyamaLayoutAlgorithm
 *
 * @param <V> vertex type
 */
class PVertex<V> extends SegmentVertex<V> {

  public static <V> PVertex<V> of() {
    return new PVertex();
  }

  protected PVertex() {
    super();
  }

  public PVertex(PVertex<V> other) {
    super(other);
  }

  @Override
  public String toString() {
    return "PVertex{"
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
