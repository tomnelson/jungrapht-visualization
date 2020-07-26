package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LEI;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;

/**
 * an edge that is not in the original graph, but is synthesized to replace one or more original
 * graph edges.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class VirtualEdge<V, E> extends LEI<V, E> implements LE<V, E> {

  protected int weight;

  public static <V, E> VirtualEdge<V, E> of(LV<V> source, LV<V> target) {
    return new VirtualEdge(source, target);
  }

  @Override
  public VirtualEdge<V, E> swapped() {
    return VirtualEdge.of(source, target); // we're not swapping endpoints on virtual edges
    // because they were created in the correct order
  }

  protected VirtualEdge(LV<V> source, LV<V> target) {
    super(null, source, target);
    if (target instanceof Container) {
      this.weight = ((Container<V>) target).size();
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
