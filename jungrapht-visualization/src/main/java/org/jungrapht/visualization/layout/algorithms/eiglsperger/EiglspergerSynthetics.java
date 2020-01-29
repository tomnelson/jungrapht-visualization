package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.List;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.ArticulatedEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticLE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticLV;
import org.jungrapht.visualization.layout.util.synthetics.Synthetic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EiglspergerSynthetics<V, E> {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerSynthetics.class);

  protected final Graph<LV<V>, LE<V, E>> dag;

  public EiglspergerSynthetics(Graph<LV<V>, LE<V, E>> dag) {
    this.dag = dag;
  }

  /**
   * Add new Synthetic vertices to replace edges that 'jump' more than one layer with a chain of
   * edges that connect each layer. The synthetic vertices wil become articulation points of long
   * bendy edges in the final graph rendering.
   *
   * @param edges all edges
   * @param layers all horizontal layers for a layered graph
   * @return updated layers to include synthetic vertices
   */
  public LV<V>[][] createVirtualVerticesAndEdges(List<LE<V, E>> edges, List<List<LV<V>>> layers) {
    for (int i = 0; i < layers.size() - 1; i++) {
      List<LV<V>> currentLayer = layers.get(i);

      // find all edges where the source vertex is in the current layer and the target vertex rank is
      // more than one layer away
      for (LV<V> v : currentLayer) {
        if (v instanceof PVertex) {
          continue;
        }
        List<LE<V, E>> outgoingMulti = new ArrayList<>();
        edges
            .stream()
            .filter(e -> !(e instanceof SegmentEdge))
            .filter(e -> e.getSource().equals(v))
            .filter(e -> Math.abs(e.getTarget().getRank() - v.getRank()) > 1)
            .forEach(outgoingMulti::add);
        //        for (LE<V, E> edge : edges) {
        //          if (edge.getSource().equals(v)) {
        //            if (Math.abs(edge.getTarget().rank - v.rank) > 1) {
        //              outgoingMulti.add(edge);
        //            }
        //          }
        //        }
        // find all edges where the target vertex is in the current layer and the source vertex rank is
        // more than one layer away
        //        List<LE<V, E>> incomingMulti = new ArrayList<>();
        //        for (LE<V, E> edge : edges) {
        //          if (edge.getTarget().equals(v)) {
        //            if (Math.abs(edge.getSource().rank - v.rank) > 1) {
        //              incomingMulti.add(edge);
        //            }
        //          }
        //        }

        // for edges that 'jump' a row, create a new vertex at the next row's rank
        // and add edges that route the original edge thru the new vertex
        for (LE<V, E> edge : outgoingMulti) {
          //          log.info("will replace outgoingMulti edge {}", edge);
          // if the edge has a source and target rank that are only 2 levels apart,
          // make just one sytheticVertex between them. Otherwise, make a segment
          if (edge.getTarget().getRank() - edge.getSource().getRank() == 2) {
            int syntheticVertexRank = edge.getSource().getRank() + 1;

            SyntheticLV<V> syntheticLV = SyntheticLV.of();
            syntheticLV.setRank(syntheticVertexRank);
            replaceEdgeWithSyntheticVertex(edges, edge, syntheticLV);
            layers.get(syntheticVertexRank).add(syntheticLV);
            updateIndices(layers.get(syntheticVertexRank));

          } else {
            PVertex<V> pVertex = PVertex.of();
            QVertex<V> qVertex = QVertex.of();

            // rank of new vertex is the rank of the source vertex + 1
            int pVertexRank = edge.getSource().getRank() + 1;
            int qVertexRank = edge.getTarget().getRank() - 1;
            pVertex.setRank(pVertexRank);
            qVertex.setRank(qVertexRank);

            pVertex.setIndex(layers.get(pVertexRank).size());
            qVertex.setIndex(layers.get(qVertexRank).size());

            replaceEdgeWithSegment(edges, edge, pVertex, qVertex);
            layers.get(pVertexRank).add(pVertex);
            layers.get(qVertexRank).add(qVertex);

            updateIndices(layers.get(pVertexRank));
            updateIndices(layers.get(qVertexRank));
          }

          edges.removeIf(edge::equals);
        }
        // for edges that 'jump' a row, create a new vertex at the previous row's rank
        // and add edges that route the original edge thru the new vertex
        //        for (LE<V, E> edge : incomingMulti) {
        //          //          log.info("will replace incomingMulti edge {}", edge);
        //          LV<V> virtualVertex = SyntheticLV.of();
        //          // rank of new vertex is the rank of the target vertex - 1
        //          int newVertexRank = edge.getTarget().rank - 1;
        //          virtualVertex.setRank(newVertexRank);
        //          virtualVertex.setIndex(layers.get(newVertexRank).size());
        //          replaceEdgeWithTwo(edges, edge, virtualVertex);
        //          layers.get(newVertexRank).add(virtualVertex);
        //          updateIndices(layers.get(newVertexRank));
        //          edges.removeIf(edge::equals);
        //        }
      }
    }
    return convertToArrays(layers);
  }

  private LV<V>[][] convertToArrays(List<List<LV<V>>> layers) {
    LV<V>[][] ranks = new LV[layers.size()][];
    for (int i = 0; i < layers.size(); i++) {
      List<LV<V>> list = layers.get(i);
      ranks[i] = new LV[list.size()];
      for (int j = 0; j < list.size(); j++) {
        ranks[i][j] = list.get(j);
      }
    }
    return ranks;
  }

  /**
   * Update the metadata (the index) in each LV of this layer so that its index matches its actual
   * position in the layer
   *
   * @param layer one horizontal layer of a layered graph
   */
  private void updateIndices(List<LV<V>> layer) {
    for (int i = 0; i < layer.size(); i++) {
      LV<V> sugiyamaVertex = layer.get(i);
      sugiyamaVertex.setIndex(i);
    }
  }

  private void replaceEdgeWithSyntheticVertex(
      List<LE<V, E>> edges, LE<V, E> loser, SyntheticLV<V> syntheticLV) {
    SyntheticLE<V, E> edgeToSyntheticVertex = SyntheticLE.of(loser, loser.getSource(), syntheticLV);
    SyntheticLE<V, E> edgeFromSyntheticVertex =
        SyntheticLE.of(loser, syntheticLV, loser.getTarget());

    // add the 3 new edges and the 2 new synthetic vertices, remove the loser edge
    edges.add(edgeToSyntheticVertex);
    edges.add(edgeFromSyntheticVertex);
    dag.addVertex(syntheticLV);

    dag.addEdge(loser.getSource(), syntheticLV, edgeToSyntheticVertex);
    dag.addEdge(syntheticLV, loser.getTarget(), edgeFromSyntheticVertex);
    dag.removeEdge(loser);
    edges.remove(loser);
  }
  /**
   * replace one edge with 2 synthetic edges
   *
   * @param edges all edges (will add 1 and remove 1)
   * @param loser edge to be removed
   * @param pVertex incident to both new edges (joins them)
   * @param qVertex incident to both new edges (joins them)
   */
  private Segment<V> replaceEdgeWithSegment(
      List<LE<V, E>> edges, LE<V, E> loser, PVertex<V> pVertex, QVertex<V> qVertex) {
    SyntheticLE<V, E> edgeToPVertex = SyntheticLE.of(loser, loser.getSource(), pVertex);
    SegmentEdge<V, E> segmentEdge = SegmentEdge.of(loser, pVertex, qVertex);
    Segment<V> segment = Segment.of(pVertex, qVertex);
    pVertex.setSegment(segment);
    qVertex.setSegment(segment);
    SyntheticLE<V, E> edgeFromQVertex = SyntheticLE.of(loser, qVertex, loser.getTarget());

    // add the 3 new edges and the 2 new synthetic vertices, remove the loser edge
    edges.add(edgeToPVertex);
    edges.add(segmentEdge);
    edges.add(edgeFromQVertex);
    dag.addVertex(pVertex);
    dag.addVertex(qVertex);
    //    dag.addVertex(segment);
    dag.addEdge(loser.getSource(), pVertex, edgeToPVertex);
    dag.addEdge(pVertex, qVertex, segmentEdge);
    dag.addEdge(qVertex, loser.getTarget(), edgeFromQVertex);
    //    dag.addEdge(p)
    dag.removeEdge(loser);
    edges.remove(loser);
    return segment;
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
    List<SyntheticLV<V>> syntheticVerticesToRemove = new ArrayList<>();
    List<SyntheticLE<V, E>> syntheticEdgesToRemove = new ArrayList<>();
    for (LE<V, E> edge : dag.edgeSet()) {
      if (edge instanceof Synthetic) {
        SyntheticLE<V, E> syntheticEdge = (SyntheticLE<V, E>) edge;
        List<SyntheticLV<V>> syntheticVertices = new ArrayList<>();
        LV<V> source = dag.getEdgeSource(edge);
        if (source instanceof Synthetic) {
          continue;
        }
        syntheticEdgesToRemove.add(syntheticEdge);
        LV<V> target = dag.getEdgeTarget(syntheticEdge);
        while (target instanceof Synthetic) {
          SyntheticLV<V> syntheticTarget = (SyntheticLV<V>) target;
          syntheticVerticesToRemove.add(syntheticTarget);
          syntheticVertices.add(syntheticTarget);
          // a SyntheticVertex will have one and only one outgoing edge
          LE<V, E> outgoingEdge = dag.outgoingEdgesOf(target).stream().findFirst().get();
          syntheticEdgesToRemove.add((SyntheticLE<V, E>) outgoingEdge);
          target = dag.getEdgeTarget(outgoingEdge);
        }
        // target is not a SyntheticLV,
        // target is now a LV<V>
        ArticulatedEdge<V, E> articulatedEdge = ArticulatedEdge.of(edge, source, target);
        syntheticVertices.forEach(articulatedEdge::addIntermediateVertex);
        syntheticVertices.forEach(v -> articulatedEdge.addIntermediatePoint(v.getPoint()));
        articulatedEdges.add(articulatedEdge);
      }
    }
    syntheticEdgesToRemove.forEach(dag::removeEdge);
    syntheticVerticesToRemove.forEach(dag::removeVertex);

    articulatedEdges.forEach(e -> dag.addEdge(e.getSource(), e.getTarget(), e));
    return articulatedEdges;
  }
}
