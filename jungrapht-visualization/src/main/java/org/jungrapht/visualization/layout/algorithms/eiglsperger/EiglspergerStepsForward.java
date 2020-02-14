package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * initialize for forward sweep (top to bottom)
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
class EiglspergerStepsForward<V, E> extends EiglspergerSteps<V, E> {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerStepsForward.class);

  public EiglspergerStepsForward(Graph<LV<V>, LE<V, E>> svGraph, LV<V>[][] layersArray) {
    super(
        svGraph,
        layersArray,
        PVertex.class::isInstance,
        QVertex.class::isInstance,
        svGraph::getEdgeSource,
        svGraph::getEdgeTarget,
        Graphs::predecessorListOf,
        e -> e);
  }
}
