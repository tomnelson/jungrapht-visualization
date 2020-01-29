package org.jungrapht.visualization.layout.util.synthetics;

/**
 * an edge that is not in the original graph,
 *
 * @param <E> edge type
 */
public class SyntheticSE<E> extends SEI<E> implements Synthetic {

  public SyntheticSE() {
    super(null);
  }

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
    return "SyntheticEdge{" + "edge=" + edge + '}';
  }
}
