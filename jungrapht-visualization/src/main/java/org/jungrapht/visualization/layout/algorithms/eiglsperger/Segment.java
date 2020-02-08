package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jungrapht.visualization.layout.util.synthetics.Synthetic;

/**
 * A Segment&lt;V&gt; is a vertex in the sparse compaction graph and contains a {@code PVertex}
 * (top) and {@code QVertex} (bottom).<br>
 * The {@code PVertex} is a {@code SyntheticSugiyamaVertex} that is the target of a synthetic edge
 * from a real vertex source.<br>
 * The {@code QVertex} is a {@code SyntheticSugiyamaVertex} that is the source of a synthetic edge
 * to a real vertex target.<br>
 * There is also {@code SegmentEdge} connecting the {@code PVertex} and {@code QVertex}.<br>
 * The SegmentEdge is added to the Graph&lt;SugiyamaVertex&lt;V&gt;, SugiyamaEdge&lt;V, E&gt;&gt;
 * while the Segment&lt;V&gt; is added to the sparse compaction graph as a Vertex.
 *
 * @param <V>
 */
class Segment<V> extends SyntheticLV<V> implements Synthetic {

  public static <V> Segment of(PVertex<V> pVertex, QVertex<V> qVertex) {
    return new Segment<>(pVertex, qVertex);
  }

  /** */
  protected final PVertex pVertex;

  protected final QVertex qVertex;

  int index;

  int pVertexRank;
  int qVertexRank;
  String rangeString;

  protected Segment(PVertex<V> pVertex, QVertex<V> qVertex) {
    super();
    this.pVertex = pVertex;
    this.pVertexRank = pVertex.getRank();
    this.qVertex = qVertex;
    this.qVertexRank = qVertex.getRank();
    rangeString = pVertexRank + "-to-" + qVertexRank;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public void setIndex(int index) {
    this.index = index;
  }

  public void initialize() {}

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
    return "Segment{" + rangeString + "}";
  }
}
