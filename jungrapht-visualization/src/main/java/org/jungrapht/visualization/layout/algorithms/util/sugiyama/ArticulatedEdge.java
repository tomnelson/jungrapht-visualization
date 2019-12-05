package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jungrapht.visualization.layout.model.Point;

public class ArticulatedEdge<V, E> extends SugiyamaEdge<V, E> {

  public static <V, E> ArticulatedEdge of(
      SugiyamaEdge<V, E> edge, SugiyamaVertex<V> source, SugiyamaVertex<V> target) {

    return new ArticulatedEdge<>(edge, source, target);
  }

  protected ArticulatedEdge(
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

  protected final List<SugiyamaVertex<V>> intermediateVertices = new ArrayList<>();
  protected final List<Point> intermediatePoints = new ArrayList<>();

  public List<Point> getIntermediatePoints() {
    return intermediatePoints;
  }

  public void addIntermediateVertex(SugiyamaVertex<V> v) {
    intermediateVertices.add(v);
  }

  public void addIntermediatePoint(Point p) {
    intermediatePoints.add(p);
  }

  public List<SugiyamaVertex<V>> getIntermediateVertices() {
    return Collections.unmodifiableList(intermediateVertices);
  }

  /**
   * reverse the direction and endpoints of this edge Done for reverting a feedback edge into its
   * original form
   */
  public ArticulatedEdge<V, E> reversed() {
    ArticulatedEdge<V, E> reversed = ArticulatedEdge.of(this, this.target, this.source);

    this.intermediateVertices.forEach(v -> reversed.intermediateVertices.add(0, v));
    this.intermediatePoints.forEach(v -> reversed.intermediatePoints.add(0, v));
    return reversed;
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
    return "ArticulatedEdge{"
        + "edge="
        + edge
        + ", source="
        + source
        + ", intermediateVertices="
        + intermediateVertices
        + ", target="
        + target
        + '}';
  }
}
