package org.jungrapht.visualization.layout.algorithms.orthogonal;

import org.jungrapht.visualization.layout.algorithms.util.PointSummaryStatistics;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;

import java.util.*;
import java.util.stream.Collectors;

/**
 * a double mapping of vertex to Point and Point to vertex.
 * As each Point can locate only one vertex, any update of a vertex to
 * a new Point must first remove the previous rectangleToVertex mapping
 * @param <V>
 */
public class Mappings<V> {

  private Map<V, Point> vertexToPointMap = new HashMap<>();
  private Map<Point, V> pointToVertexMap = new HashMap<>();


  static <V> Mappings<V> copy(Mappings<V> mappings) {
    Mappings<V> copy = new Mappings<>();
    mappings.entries().forEach(e -> copy.accept(e.getKey(), e.getValue()));
    return copy;
  }
  /**
   *
   * @param v  to map to Point
   * @param point to map to v
   * @return true if there was a change
   */
  public boolean accept(V v, Point point) {
    return update(v, point);
  }

  void confirmIntegrity() {
    if (vertexToPointMap.size() != pointToVertexMap.size()) {
      throw new IllegalArgumentException("sizes differ "+ vertexToPointMap.size()+" != "+
              pointToVertexMap.size());
    }
    for (V v : vertexToPointMap.keySet()) {
      Point r = vertexToPointMap.get(v);
      if (!pointToVertexMap.get(r).equals(v)) {
        throw new  IllegalArgumentException("Mismatch in Mappings "+ vertexToPointMap +" and "+ pointToVertexMap);
      }
    }
  }

  private boolean update(V v, Point r) {
    confirmIntegrity();
    if (!empty(r)) {
      return false; // the passed Rectangle is not empty
    }
    // clear out the old Rectangle for v
    pointToVertexMap.remove(vertexToPointMap.get(v));
    vertexToPointMap.put(v, r);
    pointToVertexMap.put(r, v);
    confirmIntegrity();
    return true;
  }

  private boolean forceUpdates(Set<Map.Entry<V, Point>> entries) {
    vertexToPointMap.clear();
    pointToVertexMap.clear();
    entries.forEach(e -> accept(e.getKey(), e.getValue()));
    confirmIntegrity();
////    if (!empty(r)) {
////      return false; // the passed Rectangle is not empty
////    }
//    // clear out the old Rectangle for v
//    pointToVertexMap.remove(vertexToPointMap.get(v));
//    vertexToPointMap.put(v, r);
//    pointToVertexMap.put(r, v);
////    confirmIntegrity();
    return true;
  }


  public V get(Point r) {
    return pointToVertexMap.get(r);
  }

  public Point get(V v) {
    return vertexToPointMap.get(v);
  }

  public boolean empty(Point r) {
    return !pointToVertexMap.containsKey(r);
  }

//  public boolean empty(Point p) {
//    return !rectangleToVertexMap.containsKey(Rectangle.of(p, 1, 1));
//  }

  public Collection<Point> rectangles() {
    return pointToVertexMap.keySet();
  }

  public Collection<V> vertices() {
    return vertexToPointMap.keySet();
  }

  public Set<Map.Entry<V, Point>> entries() {
    return vertexToPointMap.entrySet();
  }

  public Map<V, Point> getVertexToPointMap() {
    return vertexToPointMap;
  }

  /**
   * move over so there are no negative values
   */
  protected void normalize() {
    Collection<Point> mins = rectangles().stream()//.map(Rectangle::min)
            .collect(Collectors.toSet());
    Rectangle extent = computeExtent(mins);
    double offsetX = extent.x < 0 ? -extent.x : 0;
    double offsetY = extent.y < 0 ? -extent.y : 0;
    Map<V, Point> newMap = new HashMap<>();
    entries().forEach(e -> newMap.put(e.getKey(),
            e.getValue().add(offsetX, offsetY)));
    forceUpdates(newMap.entrySet());
  }
  protected Rectangle computeExtent() {
    Collection<Point> points = new HashSet<>();
    rectangles().forEach(r -> {
      points.add(r);
//      points.add(r.max());
    });
    return computeExtent(points);
  }

  void swap(V v1, V v2) {
    Point p1 = get(v1);
    Point p2 = get(v2);
//    Cell<V> cell1 = Cell.of(v1, this.get(v1));
//    Cell<V> cell2 = Cell.of(v2, this.get(v2));
    vertexToPointMap.put(v1, p2);
    pointToVertexMap.put(p2, v1);
    vertexToPointMap.put(v2, p1);
    pointToVertexMap.put(p1, v2);
  }

//  Mappings<V> createSwappedMappings(V v1, V v2) {
//    Cell<V> cell1 = Cell.of(v1, this.get(v1));
//    Cell<V> cell2 = Cell.of(v2, this.get(v2));
//    Mappings<V> swappedMappings = Mappings.copy(this);
//    swappedMappings.accept(cell1.occupant, cell2.rectangle);
//    swappedMappings.accept(cell2.occupant, cell1.rectangle);
//    return swappedMappings;
//  }

  protected Rectangle computeExtent(Collection<Point> points) {
    // find the dimensions of the layout
    PointSummaryStatistics pss = new PointSummaryStatistics();
    points.forEach(pss::accept);
    return Rectangle.from(pss.getMin(), pss.getMax());
  }


  protected void expand(double factor) {

  }

  @Override
  public String toString() {
    return "Mappings{" +
            "vertexToRectangleMap=" + vertexToPointMap +
            '}';
  }
}
