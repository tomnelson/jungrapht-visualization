package org.jungrapht.visualization.layout.algorithms.util.sugiyama;

import java.util.ArrayList;
import java.util.List;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Synthetics<V, E> {

  private static final Logger log = LoggerFactory.getLogger(Synthetics.class);

  protected final Graph<SV<V>, SE<V, E>> dag;

  public Synthetics(Graph<SV<V>, SE<V, E>> dag) {
    this.dag = dag;
  }

  public List<List<SV<V>>> createVirtualVerticesAndEdges(
      List<SE<V, E>> edges, List<List<SV<V>>> layers) {
    for (int i = 0; i < layers.size() - 1; i++) {
      //      log.info("i = {}", i);
      List<SV<V>> currentLayer = layers.get(i);

      for (SV<V> v : currentLayer) {
        //        log.info(
        //            "outgoing of {} are {} targets are {}",
        //            v,
        //            edges.stream().map(e -> e.source).collect(Collectors.toList()));
        //        log.info("targets are {}", edges.stream().map(e -> e.target).collect(Collectors.toList()));
        // find all edges where the source vertex is in the current layer and the target vertex rank is
        // more than one layer away
        //        List<SE<V, E>> outgoingMulti =
        //            edges
        //                .stream()
        //                .filter(e -> e.source.equals(v))
        //                .filter(e -> Math.abs(e.target.rank - v.rank) > 1)
        //                .collect(Collectors.toList());

        List<SE<V, E>> outgoingMulti = new ArrayList<>();
        for (SE<V, E> edge : edges) {
          if (edge.source.equals(v)) {
            if (Math.abs(edge.target.rank - v.rank) > 1) {
              outgoingMulti.add(edge);
            }
          }
        }
        // find all edges where the target vertex is in the current layer and the source vertex rank is
        // more than one layer away
        //        List<SE<V, E>> incomingMulti =
        //            edges
        //                .stream()
        //                .filter(e -> e.target.equals(v))
        //                .filter(e -> Math.abs(e.source.rank - v.rank) > 1)
        //                .collect(Collectors.toList());
        List<SE<V, E>> incomingMulti = new ArrayList<>();
        for (SE<V, E> edge : edges) {
          if (edge.target.equals(v)) {
            if (Math.abs(edge.source.rank - v.rank) > 1) {
              incomingMulti.add(edge);
            }
          }
        }
        // for edges that 'jump' a row, create a new vertex at the next row's rank
        // and add edges that route the original edge thru the new vertex
        for (SE<V, E> edge : outgoingMulti) {
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
        for (SE<V, E> edge : incomingMulti) {
          //          log.info("will replace incomingMulti edge {}", edge);
          SV<V> virtualVertex = SyntheticVertex.of();
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

  private void updateIndices(List<SV<V>> layer) {

    //    log.info("layer had {}", layer);
    for (int i = 0; i < layer.size(); i++) {
      SV<V> sv = layer.get(i);
      sv.index = i;
    }
    //    log.info("layer has {}", layer);
  }

  private void replaceEdgeWithTwo(List<SE<V, E>> edges, SE<V, E> loser, SV<V> virtualVertex) {
    SV<V> from = loser.source;
    SV<V> to = loser.target;
    SyntheticEdge<V, E> virtualEdgeOne = SyntheticEdge.of(loser, from, virtualVertex);
    SyntheticEdge<V, E> virtualEdgeTwo = SyntheticEdge.of(loser, virtualVertex, to);

    //    log.info("replaced edge {} with {} and {}", loser, virtualEdgeOne, virtualEdgeTwo);

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
    for (SE<V, E> edge : dag.edgeSet()) {
      if (edge instanceof SyntheticEdge) {
        SyntheticEdge<V, E> syntheticEdge = (SyntheticEdge<V, E>) edge;
        List<SyntheticVertex<V>> syntheticVertices = new ArrayList<>();
        SV<V> source = dag.getEdgeSource(edge);
        if (source instanceof SyntheticVertex) {
          continue;
        }
        syntheticEdgesToRemove.add(syntheticEdge);
        SV<V> target = dag.getEdgeTarget(syntheticEdge);
        while (target instanceof SyntheticVertex) {
          SyntheticVertex<V> syntheticTarget = (SyntheticVertex<V>) target;
          syntheticVerticesToRemove.add(syntheticTarget);
          syntheticVertices.add(syntheticTarget);
          // a SyntheticVertex will have one and only one outgoing edge
          SE<V, E> outgoingEdge = dag.outgoingEdgesOf(target).stream().findFirst().get();
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
