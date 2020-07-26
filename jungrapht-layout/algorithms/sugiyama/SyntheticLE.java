package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.jungrapht.visualization.layout.util.synthetics.Synthetic;

/**
 * an edge that is not in the original graph, but is synthesized to replace one or more original
 * graph edges.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class SyntheticLE<V, E> extends LEI<V, E> implements Synthetic {

  public static <V, E> SyntheticLE of(LE<V, E> edge, LV<V> source, LV<V> target) {

    return new SyntheticLE(edge, source, target);
  }

  protected SyntheticLE(LE<V, E> edge, LV<V> source, LV<V> target) {

    super(edge.getEdge(), source, target);
    this.se = edge;
  }

  /**
   * two synthetic edges are created by splitting an existing SE&lt;V,E&gt; edge. This is a
   * reference to that edge The edge what was split will gain an intermediate vertex between the
   * source and target vertices each time it or one of its split-off edges is further split
   */
  protected LE<V, E> se;

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return "SyntheticLE{" + "edge=" + edge + ", source=" + source + ", target=" + target + '}';
  }
}
