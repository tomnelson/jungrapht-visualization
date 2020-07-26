package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticLE;
import org.jungrapht.visualization.layout.util.synthetics.Synthetic;

/**
 * A {@code SegmentEdge} is a renaming extention of {@SytheticSugiyamaEdge} It is renamed to match
 * its use in the algorithm
 *
 * @param <V>
 * @param <E>
 */
class SegmentEdge<V, E> extends SyntheticLE<V, E> implements Synthetic {

  public static <V, E> SegmentEdge of(LE<V, E> edge, PVertex<V> pVertex, QVertex<V> qVertex) {

    return new SegmentEdge<>(edge, pVertex, qVertex);
  }

  protected SegmentEdge(LE<V, E> edge, PVertex<V> pVertex, QVertex<V> qVertex) {
    super(edge, pVertex, qVertex);
  }

  @Override
  public String toString() {
    return "SegmentEdge{" + "edge=" + edge + ", pVertex=" + source + ", qVertex" + target + '}';
  }
}
