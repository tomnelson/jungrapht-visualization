package org.jungrapht.visualization.layout.algorithms.repulsion;

import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <R> the Repulsion type
 * @param <B> the Repulsion Builder type
 */
public interface BarnesHutRepulsion<
        V, R extends BarnesHutRepulsion<V, R, B>, B extends BarnesHutRepulsion.Builder<V, R, B>>
    extends StandardRepulsion<V, R, B> {

  interface Builder<V, R extends BarnesHutRepulsion<V, R, B>, B extends Builder<V, R, B>>
      extends StandardRepulsion.Builder<V, R, B> {

    B layoutModel(LayoutModel<V> layoutModel);

    B theta(double theta);

    R build();
  }
}
