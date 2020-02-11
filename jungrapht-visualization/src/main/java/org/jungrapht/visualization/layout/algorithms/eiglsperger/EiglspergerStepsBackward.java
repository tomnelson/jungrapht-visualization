package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EiglspergerStepsBackward<V, E> extends EiglspergerSteps<V, E> {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerStepsBackward.class);

  public EiglspergerStepsBackward(Graph<LV<V>, LE<V, E>> svGraph, LV<V>[][] layersArray) {
    super(
        svGraph,
        layersArray,
        QVertex.class::isInstance,
        PVertex.class::isInstance,
        Graphs::successorListOf);
  }
}
