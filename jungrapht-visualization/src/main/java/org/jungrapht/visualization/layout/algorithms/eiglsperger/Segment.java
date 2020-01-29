package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticLV;
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
public class Segment<V> extends SyntheticLV<V> implements Synthetic {

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

  public void initialize() {
    //    this.left = null;
    //    this.right = null;
    //    this.parent = null;
    //    this.size = -1;
  }

  //  public int size() {
  //    if (this.size == -1) {
  //      count();
  //    }
  //    return this.size;
  //  }

  //  int count() {
  //    if (this == left) {
  //      System.err.println("Error");
  //    }
  //    if (left != null && left == right) {
  //      System.err.println("Error");
  //    }
  //    int leftCount = left != null ? left.count() : 0;
  //    int rightCount = right != null ? right.count() : 0;
  //    this.size = 1 + leftCount + rightCount;
  //    return this.size;
  //  }
  //
  //  public int height() {
  //    int leftHeight = left != null ? left.height() : 0;
  //    int rightHeight = right != null ? right.height() : 0;
  //    return 1 + Math.max(leftHeight, rightHeight);
  //  }

  //  private boolean isRightChild(Segment<V> segment) {
  //    return segment.parent.right == segment;
  //  }
  //
  //  private boolean isLeftChild(Segment<V> segment) {
  //    return segment.parent.left == segment;
  //  }
  //
  //  private boolean isRoot(Segment<V> segment) {
  //    return segment.parent == null;
  //  }
  //
  //  private int size(Segment<V> segment) {
  //    return segment == null ? 0 : segment.size();
  //  }
  //
  //  public int pos(Segment<V> segment) {
  //    if (isRoot(segment)) {
  //      return size(segment.left) + 1;
  //    } else if (isRightChild(segment)) {
  //      return pos(segment.parent) + size(segment.left) + 1;
  //    } else { //if (isLeftChild(segment)) {
  //      return pos(segment.parent) - size(segment.right) - 1;
  //    }
  //  }

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
    //            + "pVertex=" + pVertex + ", qVertex" + qVertex + '}';
  }
}
