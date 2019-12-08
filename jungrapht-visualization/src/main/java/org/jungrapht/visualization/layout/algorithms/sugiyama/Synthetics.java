package org.jungrapht.visualization.layout.algorithms.sugiyama;

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

  /**
   * Add new Synthethic vertices to replace edges that 'jump' more than one layer with a chain of
   * edges that connect each layer. The synthetic vertices wil become articulation points of long
   * bendy edges in the final graph rendering.
   *
   * @param edges all edges
   * @param layers all horizontal layers for a layered graph
   * @return updated layers to include synthetic vertices
   */
  public List<List<SugiyamaVertex<V>>> createVirtualVerticesAndEdges(
      List<SugiyamaEdge<V, E>> edges, List<List<SugiyamaVertex<V>>> layers) {
    for (int i = 0; i < layers.size() - 1; i++) {
      List<SugiyamaVertex<V>> currentLayer = layers.get(i);

      // find all edges where the source vertex is in the current layer and the target vertex rank is
      // more than one layer away
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
          SyntheticSugiyamaVertex<V> virtualVertex = SyntheticSugiyamaVertex.of();
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
          SugiyamaVertex<V> virtualVertex = SyntheticSugiyamaVertex.of();
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

  /**
   * Update the metadata (the index) in each SugiyamaVertex of this layer so that its index matches
   * its actual position in the layer
   *
   * @param layer one horizontal layer of a layered graph
   */
  private void updateIndices(List<SugiyamaVertex<V>> layer) {
    for (int i = 0; i < layer.size(); i++) {
      SugiyamaVertex<V> sugiyamaVertex = layer.get(i);
      sugiyamaVertex.index = i;
    }
  }

  /**
   * replace one edge with 2 synthetic edges
   *
   * @param edges all edges (will add 2 and remove 1)
   * @param loser edge to be removed
   * @param virtualVertex incident to both new edges (joins them)
   */
  private void replaceEdgeWithTwo(
      List<SugiyamaEdge<V, E>> edges, SugiyamaEdge<V, E> loser, SugiyamaVertex<V> virtualVertex) {
    // get the soucr/target of the edge to remove
    SugiyamaVertex<V> from = loser.source;
    SugiyamaVertex<V> to = loser.target;
    // create 2 virtual edges spanning from source -> syntheticvertex -> target
    SyntheticSugiyamaEdge<V, E> virtualEdgeOne =
        SyntheticSugiyamaEdge.of(loser, from, virtualVertex);
    SyntheticSugiyamaEdge<V, E> virtualEdgeTwo = SyntheticSugiyamaEdge.of(loser, virtualVertex, to);

    // add the 2 new edges and the new synthetic vertex, remove the loser edge
    edges.add(virtualEdgeOne);
    edges.add(virtualEdgeTwo);
    dag.addVertex(virtualVertex);
    dag.addEdge(from, virtualVertex, virtualEdgeOne);
    dag.addEdge(virtualVertex, to, virtualEdgeTwo);
    dag.removeEdge(loser);
    edges.remove(loser);
  }

  /**
   * Post processing function to turn a chain of synthetic edges into one articulated edge where the
   * old synthetic vertices become the articulation points in the new articulated edge. Find an edge
   * with a target that is a synthetic vertex. Follow the edge until the non-synthetic target is
   * reached, saving the intermediate synthetic vertices to a list (to delete outside of loop) add
   * each synthetic edge to a list of edges to delete (outside of loop)
   */
  public List<ArticulatedEdge<V, E>> makeArticulatedEdges() {
    List<ArticulatedEdge<V, E>> articulatedEdges = new ArrayList<>();
    List<SyntheticSugiyamaVertex<V>> syntheticVerticesToRemove = new ArrayList<>();
    List<SyntheticSugiyamaEdge<V, E>> syntheticEdgesToRemove = new ArrayList<>();
    for (SugiyamaEdge<V, E> edge : dag.edgeSet()) {
      if (edge instanceof SyntheticSugiyamaEdge) {
        SyntheticSugiyamaEdge<V, E> syntheticEdge = (SyntheticSugiyamaEdge<V, E>) edge;
        List<SyntheticSugiyamaVertex<V>> syntheticVertices = new ArrayList<>();
        SugiyamaVertex<V> source = dag.getEdgeSource(edge);
        if (source instanceof SyntheticSugiyamaVertex) {
          continue;
        }
        syntheticEdgesToRemove.add(syntheticEdge);
        SugiyamaVertex<V> target = dag.getEdgeTarget(syntheticEdge);
        while (target instanceof SyntheticSugiyamaVertex) {
          SyntheticSugiyamaVertex<V> syntheticTarget = (SyntheticSugiyamaVertex<V>) target;
          syntheticVerticesToRemove.add(syntheticTarget);
          syntheticVertices.add(syntheticTarget);
          // a SyntheticVertex will have one and only one outgoing edge
          SugiyamaEdge<V, E> outgoingEdge = dag.outgoingEdgesOf(target).stream().findFirst().get();
          syntheticEdgesToRemove.add((SyntheticSugiyamaEdge<V, E>) outgoingEdge);
          target = dag.getEdgeTarget(outgoingEdge);
        }
        // target is not a SyntheticSugiyamaVertex,
        // target is now a SugiyamaVertex<V>
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
