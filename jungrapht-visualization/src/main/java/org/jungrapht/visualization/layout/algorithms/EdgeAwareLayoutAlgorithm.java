package org.jungrapht.visualization.layout.algorithms;

import java.util.Comparator;
import java.util.function.Predicate;
import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * LayoutAlgorithm is a visitor to the LayoutModel. When it visits, it runs the algorithm to place
 * the graph vertices at locations.
 *
 * @author Tom Nelson.
 */
public interface EdgeAwareLayoutAlgorithm<V, E> extends LayoutAlgorithm<V> {

  /**
   * visit the passed layoutModel and set its locations
   *
   * @param layoutModel the mediator between the container for vertices (the Graph) and the mapping
   *     from Vertex to Point
   */
  void visit(LayoutModel<V> layoutModel);

  void setVertexPredicate(Predicate<V> vertexPredicate);

  void setEdgePredicate(Predicate<E> edgePredicate);

  void setVertexComparator(Comparator<V> vertexComparator);

  void setEdgeComparator(Comparator<E> edgeComparator);
}
