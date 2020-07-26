package org.jungrapht.visualization.layout.util.synthetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jungrapht.visualization.layout.model.Point;

public class ArticulatedEdge<V, E> {

  protected E edge;
  protected V source;
  protected V target;

  public static <V, E> ArticulatedEdge of(E edge, V source, V target) {
    return new ArticulatedEdge<>(edge, source, target);
  }

  protected ArticulatedEdge(E edge, V source, V target) {
    this.edge = edge;
    this.source = source;
    this.target = target;
  }

  public E getEdge() {
    return edge;
  }

  public V getSource() {
    return source;
  }

  public V getTarget() {
    return target;
  }

  protected final List<V> intermediateVertices = new ArrayList<>();
  protected final List<Point> intermediatePoints = new ArrayList<>();

  public List<Point> getIntermediatePoints() {
    return intermediatePoints;
  }

  public void addIntermediateVertex(V v) {
    intermediateVertices.add(v);
  }

  public void addIntermediatePoint(Point p) {
    intermediatePoints.add(p);
  }

  public List<V> getIntermediateVertices() {
    return Collections.unmodifiableList(intermediateVertices);
  }

  /**
   * reverse the direction and endpoints of this edge Done for reverting a feedback edge into its
   * original form
   */
  public ArticulatedEdge<V, E> reversed() {
    ArticulatedEdge<V, E> reversed = ArticulatedEdge.of(this.edge, this.target, this.source);

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
