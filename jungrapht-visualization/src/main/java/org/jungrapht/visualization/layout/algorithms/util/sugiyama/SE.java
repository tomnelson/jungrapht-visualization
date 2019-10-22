package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.Objects;

public class SE<V, E> {

  public final E edge;
  //  protected final List<SV<V>> intermediateVertices = new ArrayList<>();
  public final SV<V> source;
  public final SV<V> target;

  public static <V, E> SE<V, E> of(E edge, SV<V> source, SV<V> target) {
    return new SE(edge, source, target);
  }

  public SE(E edge, SV<V> source, SV<V> target) {
    this.edge = edge;
    this.source = source;
    this.target = target;
  }

  //  public void addIntermediateVertex(SV<V> v) {
  //    intermediateVertices.add(v);
  //  }
  //
  //  public List<SV<V>> getIntermediateVertices() {
  //    return Collections.unmodifiableList(intermediateVertices);
  //  }

  @Override
  public String toString() {
    return "SE{"
        + "edge="
        + edge
        + ", source="
        + source
        //        + ", intermediateVertices="
        //        + intermediateVertices
        + ", target="
        + target
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SE<?, ?> se = (SE<?, ?>) o;
    return Objects.equals(edge, se.edge);
  }

  @Override
  public int hashCode() {
    return Objects.hash(edge);
  }
}
