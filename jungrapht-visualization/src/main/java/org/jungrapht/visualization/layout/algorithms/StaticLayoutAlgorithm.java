package org.jungrapht.visualization.layout.algorithms;

import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * StaticLayout leaves the vertices in the locations specified in the LayoutModel, and has no other
 * behavior.
 *
 * @author Tom Nelson
 */
public class StaticLayoutAlgorithm<V> implements LayoutAlgorithm<V> {

  public StaticLayoutAlgorithm() {}

  /**
   * a no-op, as the Vertex locations are unchanged from where they are in the layoutModel
   *
   * @param layoutModel the mediator between the container for vertices (the Graph) and the mapping
   *     from Vertex to Point
   */
  @Override
  public void visit(LayoutModel<V> layoutModel) {}

  @Override
  public boolean constrained() {
    return false;
  }
}
