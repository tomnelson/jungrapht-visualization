package org.jungrapht.visualization.layout.algorithms.repulsion;

import java.util.Random;
import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * @author Tom Nelson
 * @param <N> the node type
 * @param <R> the Repulsion type
 * @param <B> the Repulsion Builder type
 */
public interface StandardRepulsion<
    N, R extends StandardRepulsion<N, R, B>, B extends StandardRepulsion.Builder<N, R, B>> {

  interface Builder<N, R extends StandardRepulsion<N, R, B>, B extends Builder<N, R, B>> {

    B layoutModel(LayoutModel<N> layoutModel);

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
