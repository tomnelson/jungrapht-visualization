package org.jungrapht.visualization.layout.algorithms.repulsion;

import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * @author Tom Nelson
 * @param <N> the node type
 * @param <R> the Repulsion type
 * @param <B> the Repulsion Builder type
 */
public interface BarnesHutRepulsion<
        N, R extends BarnesHutRepulsion<N, R, B>, B extends BarnesHutRepulsion.Builder<N, R, B>>
    extends StandardRepulsion<N, R, B> {

  interface Builder<N, R extends BarnesHutRepulsion<N, R, B>, B extends Builder<N, R, B>>
      extends StandardRepulsion.Builder<N, R, B> {

    B layoutModel(LayoutModel<N> layoutModel);

    B theta(double theta);

    R build();
  }
}
