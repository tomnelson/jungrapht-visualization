package org.jungrapht.visualization.layout.algorithms;

import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * A marker interface for LayoutAlgorithms that are aware of graph edges and can use the edges as
 * part of the layout vertex positioning
 *
 * @author Tom Nelson.
 */
public interface EdgeAwareLayoutAlgorithm<V, E>
    extends LayoutAlgorithm<V>,
        EdgeSorting<E>,
        EdgePredicated<E>,
        VertexSorting<V>,
        VertexPredicated<V> {

  interface Builder<V, E, T extends EdgeAwareLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>>
      extends LayoutAlgorithm.Builder<V, T, B> {}

  /**
   * visit the passed layoutModel and set its locations
   *
   * @param layoutModel the mediator between the container for vertices (the Graph) and the mapping
   *     from Vertex to Point
   */
  void visit(LayoutModel<V> layoutModel);
}
