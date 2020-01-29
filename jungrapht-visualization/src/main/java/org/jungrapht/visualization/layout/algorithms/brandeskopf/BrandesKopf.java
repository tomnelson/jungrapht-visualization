package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GraphLayers;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GreedyCycleRemoval;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaTransformedGraphSupplier;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Synthetics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrandesKopf<V, E> {

  private static final Logger log = LoggerFactory.getLogger(BrandesKopf.class);
  Graph<V, E> originalGraph;
  LV<V>[][] layersArray;
  Graph<LV<V>, LE<V, E>> svGraph;

  public BrandesKopf(Graph<V, E> originalGraph) {
    this.originalGraph = originalGraph;
    this.svGraph = new SugiyamaTransformedGraphSupplier<>(originalGraph).get();
    GreedyCycleRemoval<LV<V>, LE<V, E>> greedyCycleRemoval = new GreedyCycleRemoval(svGraph);
    Collection<LE<V, E>> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();

    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
    for (LE<V, E> se : feedbackArcs) {
      svGraph.removeEdge(se);
      LE<V, E> newEdge = LE.of(se.getEdge(), se.getTarget(), se.getSource());
      svGraph.addEdge(newEdge.getSource(), newEdge.getTarget(), newEdge);
    }

    List<List<LV<V>>> layers = GraphLayers.assign(svGraph);
    if (log.isTraceEnabled()) {
      GraphLayers.checkLayers(layers);
    }

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<LE<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    this.layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
  }
}
