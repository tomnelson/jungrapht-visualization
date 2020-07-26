package org.jungrapht.visualization.layout.algorithms.sugiyama;

import org.jungrapht.visualization.layout.util.synthetics.Synthetic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a vertex that is not in the original graph, but is synthesized in order to position bends in the
 * articulated edges of the SugiyamaLayoutAlgorithm
 *
 * @param <V> vertex type
 */
public class SyntheticLV<V> extends LVI<V> implements Synthetic {

  private static final Logger log = LoggerFactory.getLogger(SyntheticLV.class);

  final int hash;

  public static <V> SyntheticLV<V> of() {
    return new SyntheticLV();
  }

  protected SyntheticLV() {
    super();
    this.hash = System.identityHashCode(this);
  }

  public SyntheticLV(SyntheticLV<V> other) {
    super(other);
    this.hash = other.hash;
  }

  public <T extends LV<V>> T copy() {
    return (T) new SyntheticLV<>(this);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SyntheticLV) {
      return hash == ((SyntheticLV) o).hash;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    return "SyntheticLV{"
        + "vertex="
        + hashCode()
        + ", rank="
        + rank
        + ", index="
        + index
        + ", pos="
        + pos
        + ", measure="
        + measure
        + ", p="
        + p
        + '}';
  }
}
