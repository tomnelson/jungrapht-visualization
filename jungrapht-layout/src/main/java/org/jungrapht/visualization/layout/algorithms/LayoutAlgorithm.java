package org.jungrapht.visualization.layout.algorithms;

import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * LayoutAlgorithm is a visitor to the LayoutModel. When it visits, it runs the algorithm to place
 * the graph vertices at locations.
 *
 * @author Tom Nelson.
 */
public interface LayoutAlgorithm<V> {

  interface Builder<V, T extends LayoutAlgorithm<V>, B extends Builder<V, T, B>> {
    T build();
  }
  /**
   * visit the passed layoutModel and set its locations
   *
   * @param layoutModel the mediator between the container for vertices (the Graph) and the mapping
   *     from Vertex to Point
   */
  void visit(LayoutModel<V> layoutModel);

  default void cancel() {
    // no op
  }

  default boolean constrained() {
    return true;
  }

  class NoOp<V> implements LayoutAlgorithm<V> {

    /**
     * visit the passed layoutModel and set its locations
     *
     * @param layoutModel the mediator between the container for vertices (the Graph) and the
     *     mapping from Vertex to Point
     */
    @Override
    public void visit(LayoutModel<V> layoutModel) {
      // noop
    }
  }
}
