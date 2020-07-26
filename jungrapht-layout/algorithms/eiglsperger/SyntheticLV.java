package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LVI;
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
  LV<V> source;
  LV<V> target;
  String srcDest;

  public static <V> SyntheticLV<V> of() {
    return new SyntheticLV();
  }

  public static <V> SyntheticLV<V> of(LV<V> source, LV<V> target) {

    return new SyntheticLV(source, target);
  }

  protected SyntheticLV() {
    super();
    this.hash = System.identityHashCode(this);
  }

  protected SyntheticLV(LV<V> source, LV<V> target) {
    super();
    this.source = source;
    this.target = target;
    this.srcDest = "{" + source.getRank() + "-" + target.getRank() + "}";
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
  public void setPos(int pos) {
    super.setPos(pos);
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
