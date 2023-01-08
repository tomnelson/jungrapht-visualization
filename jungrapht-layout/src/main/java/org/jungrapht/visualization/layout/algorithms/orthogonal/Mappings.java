package org.jungrapht.visualization.layout.algorithms.orthogonal;

import org.jungrapht.visualization.layout.model.Rectangle;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * a double mapping of vertex to rectangle and rectangle to vertex.
 * As each Rectangle can contain only one vertex, any update of a vertex to
 * a new Rectangle must first remove the previous rectangleToVertex mapping
 * @param <V>
 */
public class Mappings<V> implements BiConsumer<V, Rectangle> {

  private Map<V, Rectangle> vertexToRectangleMap = new HashMap<>();
  private Map<Rectangle, V> rectangleToVertexMap = new HashMap<>();

  @Override
  public void accept(V v, Rectangle rectangle) {
    update(v, rectangle);
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

  private void update(V v, Rectangle r) {
    confirmIntegrity();
    // remove existing values
    Rectangle existingRectangle = vertexToRectangleMap.remove(v);
    if (existingRectangle != null) {
      rectangleToVertexMap.remove(existingRectangle);
    }
    V existingVertex = rectangleToVertexMap.remove(r);
    if (existingVertex != null) {
      vertexToRectangleMap.remove(existingVertex);
    }
    confirmIntegrity();
    // set up new mapping
    vertexToRectangleMap.put(v, r);
    rectangleToVertexMap.put(r, v);
    confirmIntegrity();
  }

  public V get(Rectangle r) {
    return rectangleToVertexMap.get(r);
  }

  public Rectangle get(V v) {
//    if(vertexToRectangleMap.get(v) == null) {
//      throw new IllegalArgumentException("not mapped");
//    }
    return vertexToRectangleMap.get(v);
  }

  public boolean empty(Rectangle r) {
    return rectangleToVertexMap.get(r) == null;
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
