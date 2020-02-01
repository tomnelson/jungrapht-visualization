package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 * @param <V>
 * @param <E>
 */
public class BrandesKopf<V, E> {

  private static final Logger log = LoggerFactory.getLogger(BrandesKopf.class);
  Graph<V, E> originalGraph;
  LV<V>[][] layersArray;
  Graph<LV<V>, LE<V, E>> svGraph;

  public BrandesKopf(Graph<V, E> originalGraph) {
    this.originalGraph = originalGraph;
    this.svGraph = new TransformedGraphSupplier<>(originalGraph).get();
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
