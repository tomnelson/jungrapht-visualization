package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Objects;
import org.jungrapht.visualization.layout.util.synthetics.SEI;

/**
 * Implementation of an edge type used for the application of a layered layout algorithm.<br>
 * Instances of LEI&lt;V,E&gt; replace instances of E during layout
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class LEI<V, E> extends SEI<E> implements LE<V, E> {

  public final LV<V> source;
  public final LV<V> target;

  public LEI(E edge, LV<V> source, LV<V> target) {
    super(edge);
    this.source = source;
    this.target = target;
  }

  @Override
  public LEI<V, E> swapped() {
    return new LEI<>(edge, target, source);
  }

  @Override
  public LV<V> getSource() {
    return source;
  }

  @Override
  public LV<V> getTarget() {
    return target;
  }

  @Override
  public String toString() {
    return "LEI{" + "edge=" + edge + ", source=" + source + ", target=" + target + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    LEI<?, ?> that = (LEI<?, ?>) o;
    return Objects.equals(source, that.source) && Objects.equals(target, that.target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), source, target);
  }
}
