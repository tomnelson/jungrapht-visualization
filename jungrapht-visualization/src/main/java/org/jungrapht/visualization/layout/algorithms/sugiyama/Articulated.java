package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jungrapht.visualization.layout.model.Point;

public class Articulated<E> {

  public final E edge;

  public Articulated(E edge) {
    this.edge = edge;
  }

  protected List<Point> articulationPoints;

  public void setArticulationPoints(List<Point> points) {
    this.articulationPoints = points;
  }

  public List<Point> getArticulationPoints() {
    return Collections.unmodifiableList(articulationPoints);
  }

  @Override
  public String toString() {
    return "Articulated{" + "edge=" + edge + ", articulationPoints=" + articulationPoints + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Articulated<?> that = (Articulated<?>) o;
    return Objects.equals(edge, that.edge);
  }

  @Override
  public int hashCode() {
    return Objects.hash(edge);
  }
}
