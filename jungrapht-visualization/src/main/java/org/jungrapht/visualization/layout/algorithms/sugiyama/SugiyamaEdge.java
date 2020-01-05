package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Objects;
import org.jungrapht.visualization.layout.util.synthetics.SE;

/**
 * An edge type used for the application of the SugiyamaLayoutAlgorithm.<br>
 * Instances of SE&lt;V,E&gt; replace instances of E during layout
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class SugiyamaEdge<V, E> extends SE<E> {

  public final SugiyamaVertex<V> source;
  public final SugiyamaVertex<V> target;

  public static <V, E> SugiyamaEdge<V, E> of(
      E edge, SugiyamaVertex<V> source, SugiyamaVertex<V> target) {
    return new SugiyamaEdge(edge, source, target);
  }

  public SugiyamaEdge(E edge, SugiyamaVertex<V> source, SugiyamaVertex<V> target) {
    super(edge);
    this.source = source;
    this.target = target;
  }

  @Override
  public String toString() {
    return "SugiyamaEdge{" + "edge=" + edge + ", source=" + source + ", target=" + target + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    SugiyamaEdge<?, ?> that = (SugiyamaEdge<?, ?>) o;
    return Objects.equals(source, that.source) && Objects.equals(target, that.target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), source, target);
  }
  //  @Override
  //  public boolean equals(Object o) {
  //    if (this == o) return true;
  //    if (o == null || getClass() != o.getClass()) return false;
  //    SugiyamaEdge<?, ?> se = (SugiyamaEdge<?, ?>) o;
  //    return Objects.equals(edge, se.edge);
  //  }
  //
  //  @Override
  //  public int hashCode() {
  //    return Objects.hash(edge);
  //  }
}
