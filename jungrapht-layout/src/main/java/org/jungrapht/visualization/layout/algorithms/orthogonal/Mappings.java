package org.jungrapht.visualization.layout.algorithms.orthogonal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.jungrapht.visualization.layout.model.Rectangle;

public class Mappings<V> implements BiConsumer<V, Rectangle> {

  private Map<V, Rectangle> vertexToRectangleMap = new HashMap<>();
  private Map<Rectangle, V> rectangleToVertexMap = new HashMap<>();

  @Override
  public void accept(V v, Rectangle rectangle) {
    vertexToRectangleMap.put(v, rectangle);
    rectangleToVertexMap.put(rectangle, v);
  }

  public void update(V v, Rectangle r) {
    this.rectangleToVertexMap.remove(vertexToRectangleMap.get(v));
    this.vertexToRectangleMap.put(v, r);
    this.rectangleToVertexMap.put(r, v);
  }

  public V get(Rectangle r) {
    return rectangleToVertexMap.get(r);
  }

  public Rectangle get(V v) {
    return vertexToRectangleMap.get(v);
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
}
