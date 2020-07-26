package org.jungrapht.visualization.layout.algorithms.repulsion;

import java.util.Random;
import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <R> the Repulsion type
 * @param <B> the Repulsion Builder type
 */
public interface StandardRepulsion<
    V, R extends StandardRepulsion<V, R, B>, B extends StandardRepulsion.Builder<V, R, B>> {

  interface Builder<V, R extends StandardRepulsion<V, R, B>, B extends Builder<V, R, B>> {

    B layoutModel(LayoutModel<V> layoutModel);

    B random(Random random);

    R build();
  }

  /**
   * called from the layout algorithm on every step. this version is a noop but the subclass
   * BarnesHut version rebuilds the tree on every step
   */
  void step();

  void calculateRepulsion();
}
