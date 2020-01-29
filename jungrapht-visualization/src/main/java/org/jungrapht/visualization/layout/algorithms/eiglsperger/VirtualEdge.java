package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jungrapht.visualization.layout.algorithms.sugiyama.LEI;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;

/**
 * an edge that is not in the original graph, but is synthesized to replace one or more original
 * graph edges.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class VirtualEdge<V, E> extends LEI<V, E> {

  protected int weight;

  public static <V, E> VirtualEdge of(LV<V> source, LV<V> target) {
    return new VirtualEdge(source, target);
  }

  protected VirtualEdge(LV<V> source, LV<V> target) {
    super(null, source, target);
    if (target instanceof SubContainer) {
      this.weight = ((SubContainer<V, Segment<V>>) target).size();
    }
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  /**
   * two synthetic edges are created by splitting an existing SE&lt;V,E&gt; edge. This is a
   * reference to that edge The edge what was split will gain an intermediate vertex between the
   * source and target vertices each time it or one of its split-off edges is further split
   */
  //  protected SugiyamaEdge<V, E> se;

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
    return "VirtualEdge{" + ", source=" + source + ", target=" + target + '}';
  }
}
