package org.jungrapht.visualization.layout.algorithms.orthogonal;

import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.util.PointSummaryStatistics;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;

import java.util.*;
import java.util.stream.Collectors;

/**
 * a double mapping of vertex to rectangle and rectangle to vertex.
 * As each Rectangle can contain only one vertex, any update of a vertex to
 * a new Rectangle must first remove the previous rectangleToVertex mapping
 * @param <V>
 */
public class Mappings<V> {

  private Map<V, Rectangle> vertexToRectangleMap = new HashMap<>();
  private Map<Rectangle, V> rectangleToVertexMap = new HashMap<>();


  static <V> Mappings<V> copy(Mappings<V> mappings) {
    Mappings<V> copy = new Mappings<>();
    mappings.entries().forEach(e -> copy.accept(e.getKey(), e.getValue()));
    return copy;
  }
  /**
   *
   * @param v  to map to rectangle
   * @param rectangle to map to v
   * @return true if there was a change
   */
  public boolean accept(V v, Rectangle rectangle) {
    return update(v, rectangle);
  }

  private void confirmIntegrity() {
    if (vertexToRectangleMap.size() != rectangleToVertexMap.size()) {
      throw new IllegalArgumentException("sizes differ "+vertexToRectangleMap.size()+" != "+
              rectangleToVertexMap.size());
    }
    for (V v : vertexToRectangleMap.keySet()) {
      Rectangle r = vertexToRectangleMap.get(v);
      if (!rectangleToVertexMap.get(r).equals(v)) {
        throw new  IllegalArgumentException("Mismatch in Mappings "+vertexToRectangleMap+" and "+rectangleToVertexMap);
      }
    }
  }

  private boolean update(V v, Rectangle r) {
    confirmIntegrity();
    if (!empty(r)) {
      return false; // the passed Rectangle is not empty
    }
    // clear out the old Rectangle for v
    rectangleToVertexMap.remove(vertexToRectangleMap.get(v));
    vertexToRectangleMap.put(v, r);
    rectangleToVertexMap.put(r, v);
    confirmIntegrity();
    return true;
  }

  private boolean forceUpdate(V v, Rectangle r) {
//    confirmIntegrity();
//    if (!empty(r)) {
//      return false; // the passed Rectangle is not empty
//    }
    // clear out the old Rectangle for v
    rectangleToVertexMap.remove(vertexToRectangleMap.get(v));
    vertexToRectangleMap.put(v, r);
    rectangleToVertexMap.put(r, v);
//    confirmIntegrity();
    return true;
  }


  public V get(Rectangle r) {
    return rectangleToVertexMap.get(r);
  }

  public Rectangle get(V v) {
    return vertexToRectangleMap.get(v);
  }

  public boolean empty(Rectangle r) {
    return !rectangleToVertexMap.containsKey(r);
  }

  public boolean empty(Point p) {
    return !rectangleToVertexMap.containsKey(Rectangle.of(p, 1, 1));
  }

  public Collection<Rectangle> rectangles() {
    return rectangleToVertexMap.keySet();
  }

  public Collection<V> vertices() {
    return vertexToRectangleMap.keySet();
  }

  public Set<Map.Entry<V, Rectangle>> entries() {
    return vertexToRectangleMap.entrySet();
  }

  public Map<V, Rectangle> getVertexToRectangleMap() {
    return vertexToRectangleMap;
  }

  /**
   * move over so there are no negative values
   */
  protected void normalize() {
    Collection<Point> mins = rectangles().stream().map(Rectangle::min)
            .collect(Collectors.toSet());
    Rectangle extent = computeExtent(mins);
    double offsetX = extent.x < 0 ? -extent.x : 0;
    double offsetY = extent.y < 0 ? -extent.y : 0;
    entries().forEach(e -> forceUpdate(e.getKey(),
            e.getValue().offset(offsetX, offsetY)));
  }
  protected Rectangle computeExtent() {
    Collection<Point> points = new HashSet<>();
    rectangles().forEach(r -> {
      points.add(r.min());
      points.add(r.max());
    });
    return computeExtent(points);
  }

  void swap(V v1, V v2) {
    Cell<V> cell1 = Cell.of(v1, this.get(v1));
    Cell<V> cell2 = Cell.of(v2, this.get(v2));
    vertexToRectangleMap.put(cell1.occupant, cell2.rectangle);
    rectangleToVertexMap.put(cell2.rectangle, cell1.occupant);
    vertexToRectangleMap.put(cell2.occupant, cell1.rectangle);
    rectangleToVertexMap.put(cell1.rectangle, cell2.occupant);
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
            "vertexToRectangleMap=" + vertexToRectangleMap +
            '}';
  }
}
