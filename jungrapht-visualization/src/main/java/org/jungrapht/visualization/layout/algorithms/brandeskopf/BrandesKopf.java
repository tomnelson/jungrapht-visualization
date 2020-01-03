package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GraphLayers;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GreedyCycleRemoval;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaTransformedGraphSupplier;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaVertex;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Synthetics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrandesKopf<V, E> {

  private static final Logger log = LoggerFactory.getLogger(BrandesKopf.class);
  Graph<V, E> originalGraph;
  SugiyamaVertex<V>[][] layersArray;
  Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph;

  public BrandesKopf(Graph<V, E> originalGraph) {
    this.originalGraph = originalGraph;
    this.svGraph = new SugiyamaTransformedGraphSupplier<>(originalGraph).get();
    GreedyCycleRemoval<SugiyamaVertex<V>, SugiyamaEdge<V, E>> greedyCycleRemoval =
        new GreedyCycleRemoval(svGraph);
    Collection<SugiyamaEdge<V, E>> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();

    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
    for (SugiyamaEdge<V, E> se : feedbackArcs) {
      svGraph.removeEdge(se);
      SugiyamaEdge<V, E> newEdge = SugiyamaEdge.of(se.edge, se.target, se.source);
      svGraph.addEdge(newEdge.source, newEdge.target, newEdge);
    }

    List<List<SugiyamaVertex<V>>> layers = GraphLayers.assign(svGraph);
    if (log.isTraceEnabled()) {
      GraphLayers.checkLayers(layers);
    }

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<SugiyamaEdge<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    this.layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
  }
}
