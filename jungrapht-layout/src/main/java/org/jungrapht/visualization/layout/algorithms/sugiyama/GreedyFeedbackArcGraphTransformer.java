package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.Collection;
import java.util.function.Function;
import org.jgrapht.Graph;

public class GreedyFeedbackArcGraphTransformer<V, E> implements Function<Graph<V, E>, Graph<V, E>> {

  Function<E, E> edgeFactory;

  public GreedyFeedbackArcGraphTransformer(Function<E, E> edgeFactory) {
    this.edgeFactory = edgeFactory;
  }

  @Override
  public Graph<V, E> apply(Graph<V, E> graph) {
    GreedyCycleRemoval<V, E> greedyCycleRemoval = new GreedyCycleRemoval(graph);
    Collection<E> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();
    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
    for (E se : feedbackArcs) {
      V source = graph.getEdgeSource(se);
      V target = graph.getEdgeTarget(se);
      graph.removeEdge(se);
      E newEdge = edgeFactory.apply(se);
      graph.addEdge(target, source, newEdge);
    }
    return graph;
  }
}
