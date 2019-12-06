package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.ArrayList;
import java.util.List;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Synthetics<V, E> {

  private static final Logger log = LoggerFactory.getLogger(Synthetics.class);

  protected final Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> dag;

  public Synthetics(Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> dag) {
    this.dag = dag;
  }

  public List<List<SugiyamaVertex<V>>> createVirtualVerticesAndEdges(
      List<SugiyamaEdge<V, E>> edges, List<List<SugiyamaVertex<V>>> layers) {
    for (int i = 0; i < layers.size() - 1; i++) {
      List<SugiyamaVertex<V>> currentLayer = layers.get(i);

      for (SugiyamaVertex<V> v : currentLayer) {
        List<SugiyamaEdge<V, E>> outgoingMulti = new ArrayList<>();
        for (SugiyamaEdge<V, E> edge : edges) {
          if (edge.source.equals(v)) {
            if (Math.abs(edge.target.rank - v.rank) > 1) {
              outgoingMulti.add(edge);
            }
          }
        }
        // find all edges where the target vertex is in the current layer and the source vertex rank is
        // more than one layer away
        List<SugiyamaEdge<V, E>> incomingMulti = new ArrayList<>();
        for (SugiyamaEdge<V, E> edge : edges) {
          if (edge.target.equals(v)) {
            if (Math.abs(edge.source.rank - v.rank) > 1) {
              incomingMulti.add(edge);
            }
          }
        }
        // for edges that 'jump' a row, create a new vertex at the next row's rank
        // and add edges that route the original edge thru the new vertex
        for (SugiyamaEdge<V, E> edge : outgoingMulti) {
          //          log.info("will replace outgoingMulti edge {}", edge);
          SyntheticVertex<V> virtualVertex = SyntheticVertex.of();
          // rank of new vertex is the rank of the source vertex + 1
          int newVertexRank = edge.source.rank + 1;
          virtualVertex.setRank(newVertexRank);
          virtualVertex.setIndex(layers.get(newVertexRank).size());
          replaceEdgeWithTwo(edges, edge, virtualVertex);
          layers.get(newVertexRank).add(virtualVertex);
          updateIndices(layers.get(newVertexRank));
          edges.removeIf(edge::equals);
        }
        // for edges that 'jump' a row, create a new vertex at the previous row's rank
        // and add edges that route the original edge thru the new vertex
        for (SugiyamaEdge<V, E> edge : incomingMulti) {
          //          log.info("will replace incomingMulti edge {}", edge);
          SugiyamaVertex<V> virtualVertex = SyntheticVertex.of();
          // rank of new vertex is the rank of the target vertex - 1
          int newVertexRank = edge.target.rank - 1;
          virtualVertex.setRank(newVertexRank);
          virtualVertex.setIndex(layers.get(newVertexRank).size());
          replaceEdgeWithTwo(edges, edge, virtualVertex);
          layers.get(newVertexRank).add(virtualVertex);
          updateIndices(layers.get(newVertexRank));
          edges.removeIf(edge::equals);
        }
      }
    }
    return layers;
  }

  private void updateIndices(List<SugiyamaVertex<V>> layer) {

    for (int i = 0; i < layer.size(); i++) {
      SugiyamaVertex<V> sugiyamaVertex = layer.get(i);
      sugiyamaVertex.index = i;
    }
  }

  private void replaceEdgeWithTwo(
      List<SugiyamaEdge<V, E>> edges, SugiyamaEdge<V, E> loser, SugiyamaVertex<V> virtualVertex) {
    SugiyamaVertex<V> from = loser.source;
    SugiyamaVertex<V> to = loser.target;
    SyntheticEdge<V, E> virtualEdgeOne = SyntheticEdge.of(loser, from, virtualVertex);
    SyntheticEdge<V, E> virtualEdgeTwo = SyntheticEdge.of(loser, virtualVertex, to);

    edges.add(virtualEdgeOne);
    edges.add(virtualEdgeTwo);
    dag.addVertex(virtualVertex);
    dag.addEdge(from, virtualVertex, virtualEdgeOne);
    dag.addEdge(virtualVertex, to, virtualEdgeTwo);
    dag.removeEdge(loser);
    edges.remove(loser);
  }

  /**
   * find an edge with a target that is a synthetic vertex follow the edge until the non-synthetic
   * target is reached, saving the intermediate synthetic vertices to a list (to delete later) add
   * each synthetic edge to a list of edges to delete
   */
  public List<ArticulatedEdge<V, E>> makeArticulatedEdges() {
    List<ArticulatedEdge<V, E>> articulatedEdges = new ArrayList<>();
    List<SyntheticVertex<V>> syntheticVerticesToRemove = new ArrayList<>();
    List<SyntheticEdge<V, E>> syntheticEdgesToRemove = new ArrayList<>();
    for (SugiyamaEdge<V, E> edge : dag.edgeSet()) {
      if (edge instanceof SyntheticEdge) {
        SyntheticEdge<V, E> syntheticEdge = (SyntheticEdge<V, E>) edge;
        List<SyntheticVertex<V>> syntheticVertices = new ArrayList<>();
        SugiyamaVertex<V> source = dag.getEdgeSource(edge);
        if (source instanceof SyntheticVertex) {
          continue;
        }
        syntheticEdgesToRemove.add(syntheticEdge);
        SugiyamaVertex<V> target = dag.getEdgeTarget(syntheticEdge);
        while (target instanceof SyntheticVertex) {
          SyntheticVertex<V> syntheticTarget = (SyntheticVertex<V>) target;
          syntheticVerticesToRemove.add(syntheticTarget);
          syntheticVertices.add(syntheticTarget);
          // a SyntheticVertex will have one and only one outgoing edge
          SugiyamaEdge<V, E> outgoingEdge = dag.outgoingEdgesOf(target).stream().findFirst().get();
          syntheticEdgesToRemove.add((SyntheticEdge<V, E>) outgoingEdge);
          target = dag.getEdgeTarget(outgoingEdge);
        }
        // target is not a SyntheticVertex
        // target is now a SV<V>
        ArticulatedEdge<V, E> articulatedEdge = ArticulatedEdge.of(edge, source, target);
        syntheticVertices.forEach(articulatedEdge::addIntermediateVertex);
        syntheticVertices.forEach(v -> articulatedEdge.addIntermediatePoint(v.getPoint()));
        articulatedEdges.add(articulatedEdge);
      }
    }
    syntheticEdgesToRemove.forEach(dag::removeEdge);
    syntheticVerticesToRemove.forEach(dag::removeVertex);

    articulatedEdges.forEach(e -> dag.addEdge(e.source, e.target, e));
    return articulatedEdges;
  }
}
