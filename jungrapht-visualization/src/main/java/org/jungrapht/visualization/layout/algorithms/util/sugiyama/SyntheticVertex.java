package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

public class SyntheticVertex<V> extends SV<V> {

  final int hash;

  public static <V> SyntheticVertex<V> of() {
    return new SyntheticVertex();
  }

  protected SyntheticVertex() {
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
    return "SyntheticVertex{"
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
