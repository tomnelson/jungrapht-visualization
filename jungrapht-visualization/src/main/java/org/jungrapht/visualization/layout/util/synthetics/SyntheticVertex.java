package org.jungrapht.visualization.layout.util.synthetics;

/**
 * a vertex that is not in the original graph, but is a subtype of SV
 *
 * @param <V> vertex type
 */
public class SyntheticVertex<V> extends SV<V> {

  final int hash;

  public SyntheticVertex() {
    super();
    this.hash = System.identityHashCode(this);
  }

  public SyntheticVertex(SyntheticVertex<V> other) {
    super();
    this.hash = other.hash;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SyntheticVertex) {
      return hash == ((SyntheticVertex) o).hash;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    return "SyntheticVertex{" + "vertex=" + hashCode() + '}';
  }
}
