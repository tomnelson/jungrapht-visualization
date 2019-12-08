package org.jungrapht.visualization.layout.algorithms.sugiyama;

/**
 * an edge that is not in the original graph, but is synthesized to replace one or more original
 * graph edges.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class SyntheticSugiyamaEdge<V, E> extends SugiyamaEdge<V, E> {

  public static <V, E> SyntheticSugiyamaEdge of(
      SugiyamaEdge<V, E> edge, SugiyamaVertex<V> source, SugiyamaVertex<V> target) {

    return new SyntheticSugiyamaEdge(edge, source, target);
  }

  protected SyntheticSugiyamaEdge(
      SugiyamaEdge<V, E> edge, SugiyamaVertex<V> source, SugiyamaVertex<V> target) {

    super(edge.edge, source, target);
    this.se = edge;
  }

  /**
   * two synthetic edges are created by splitting an existing SE&lt;V,E&gt; edge. This is a
   * reference to that edge The edge what was split will gain an intermediate vertex between the
   * source and target vertices each time it or one of its split-off edges is further split
   */
  protected SugiyamaEdge<V, E> se;

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
    return "SyntheticEdge{" + "edge=" + edge + ", source=" + source + ", target=" + target + '}';
  }
}
