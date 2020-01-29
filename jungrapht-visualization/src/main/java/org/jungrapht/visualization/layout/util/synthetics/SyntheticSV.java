package org.jungrapht.visualization.layout.util.synthetics;

/**
 * a vertex that is not in the original graph, but is a subtype of SV
 *
 * @param <V> vertex type
 */
public class SyntheticSV<V> extends SVI<V> implements Synthetic {

  final int hash;

  public SyntheticSV() {
    super();
    this.hash = System.identityHashCode(this);
  }

  public SyntheticSV(SyntheticSV<V> other) {
    super();
    this.hash = other.hash;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SyntheticSV) {
      return hash == ((SyntheticSV) o).hash;
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
