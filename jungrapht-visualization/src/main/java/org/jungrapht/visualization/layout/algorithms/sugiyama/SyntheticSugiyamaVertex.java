package org.jungrapht.visualization.layout.algorithms.sugiyama;

/**
 * a vertex that is not in the original graph, but is synthesized in order to position bends in the
 * articulated edges of the SugiyamaLayoutAlgorithm
 *
 * @param <V> vertex type
 */
public class SyntheticSugiyamaVertex<V> extends SugiyamaVertex<V> {

  final int hash;

  public static <V> SyntheticSugiyamaVertex<V> of() {
    return new SyntheticSugiyamaVertex();
  }

  protected SyntheticSugiyamaVertex() {
    super();
    this.hash = System.identityHashCode(this);
  }

  public SyntheticSugiyamaVertex(SyntheticSugiyamaVertex<V> other) {
    super(other);
    this.hash = other.hash;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SyntheticSugiyamaVertex) {
      return hash == ((SyntheticSugiyamaVertex) o).hash;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    return "SyntheticSugiyamaVertex{"
        + "vertex="
        + hashCode()
        + ", rank="
        + rank
        + ", index="
        + index
        + ", p="
        + p
        + '}';
  }
}
