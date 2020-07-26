package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Synthetics<V, E> {

  private static final Logger log = LoggerFactory.getLogger(Synthetics.class);

  protected final Graph<LV<V>, LE<V, E>> dag;

  public Synthetics(Graph<LV<V>, LE<V, E>> dag) {
    this.dag = dag;
  }

  /**
   * Add new Synthethic vertices to replace edges that 'jump' more than one layer with a chain of
   * edges that connect each layer. The synthetic vertices wil become articulation points of long
   * bendy edges in the final graph rendering.
   *
   * @param edges all edges
   * @param layers all horizontal layers for a layered graph
   * @return updated array of layers to include synthetic vertices
   */
  public LV<V>[][] createVirtualVerticesAndEdges(List<LE<V, E>> edges, List<List<LV<V>>> layers) {
    for (int i = 0; i < layers.size() - 1; i++) {
      List<LV<V>> currentLayer = layers.get(i);

      // find all edges where the source vertex is in the current layer and the target vertex rank is
      // more than one layer away
      for (LV<V> v : currentLayer) {
        List<LE<V, E>> outgoingMulti = new ArrayList<>();
        for (LE<V, E> edge : edges) {
          if (edge.getSource().equals(v)) {
            if (Math.abs(edge.getTarget().getRank() - v.getRank()) > 1) {
              outgoingMulti.add(edge);
            }
          }
        }
        // find all edges where the target vertex is in the current layer and the source vertex rank is
        // more than one layer away
        List<LE<V, E>> incomingMulti = new ArrayList<>();
        for (LE<V, E> edge : edges) {
          if (edge.getTarget().equals(v)) {
            if (Math.abs(edge.getSource().getRank() - v.getRank()) > 1) {
              incomingMulti.add(edge);
            }
          }
        }
        // for edges that 'jump' a row, create a new vertex at the next row's rank
        // and add edges that route the original edge thru the new vertex
        for (LE<V, E> edge : outgoingMulti) {
          //          log.info("will replace outgoingMulti edge {}", edge);
          SyntheticLV<V> virtualVertex = SyntheticLV.of();
          // rank of new vertex is the rank of the source vertex + 1
          int newVertexRank = edge.getSource().getRank() + 1;
          virtualVertex.setRank(newVertexRank);
          virtualVertex.setIndex(layers.get(newVertexRank).size());
          replaceEdgeWithTwo(edges, edge, virtualVertex);
          layers.get(newVertexRank).add(virtualVertex);
          updateIndices(layers.get(newVertexRank));
          edges.removeIf(edge::equals);
        }
        // for edges that 'jump' a row, create a new vertex at the previous row's rank
        // and add edges that route the original edge thru the new vertex
        for (LE<V, E> edge : incomingMulti) {
          //          log.info("will replace incomingMulti edge {}", edge);
          LV<V> virtualVertex = SyntheticLV.of();
          // rank of new vertex is the rank of the target vertex - 1
          int newVertexRank = edge.getTarget().getRank() - 1;
          virtualVertex.setRank(newVertexRank);
          virtualVertex.setIndex(layers.get(newVertexRank).size());
          replaceEdgeWithTwo(edges, edge, virtualVertex);
          layers.get(newVertexRank).add(virtualVertex);
          updateIndices(layers.get(newVertexRank));
          edges.removeIf(edge::equals);
        }
      }
    }
    return convertToArrays(layers);
  }

  /**
   * convert the list layers to array form
   *
   * @param layers
   * @return
   */
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
   * Update the metadata (the index) in each SugiyamaVertex of this layer so that its index matches
   * its actual position in the layer
   *
   * @param layer one horizontal layer of a layered graph
   */
  private void updateIndices(List<LV<V>> layer) {
    for (int i = 0; i < layer.size(); i++) {
      LV<V> LV = layer.get(i);
      LV.setIndex(i);
    }
  }

  /**
   * replace one edge with 2 synthetic edges
   *
   * @param edges all edges (will add 2 and remove 1)
   * @param loser edge to be removed
   * @param virtualVertex incident to both new edges (joins them)
   */
  private void replaceEdgeWithTwo(List<LE<V, E>> edges, LE<V, E> loser, LV<V> virtualVertex) {
    // get the soucr/target of the edge to remove
    LV<V> from = loser.getSource();
    LV<V> to = loser.getTarget();
    // create 2 virtual edges spanning from source -> syntheticvertex -> target
    SyntheticLE<V, E> virtualEdgeOne = SyntheticLE.of(loser, from, virtualVertex);
    SyntheticLE<V, E> virtualEdgeTwo = SyntheticLE.of(loser, virtualVertex, to);

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
    List<SyntheticLV<V>> syntheticVerticesToRemove = new ArrayList<>();
    List<SyntheticLE<V, E>> syntheticEdgesToRemove = new ArrayList<>();
    for (LE<V, E> edge : dag.edgeSet()) {
      if (edge instanceof SyntheticLE) {
        SyntheticLE<V, E> syntheticEdge = (SyntheticLE<V, E>) edge;
        List<SyntheticLV<V>> syntheticVertices = new ArrayList<>();
        LV<V> source = dag.getEdgeSource(edge);
        if (source instanceof SyntheticLV) {
          continue;
        }
        syntheticEdgesToRemove.add(syntheticEdge);
        LV<V> target = dag.getEdgeTarget(syntheticEdge);
        while (target instanceof SyntheticLV) {
          SyntheticLV<V> syntheticTarget = (SyntheticLV<V>) target;
          syntheticVerticesToRemove.add(syntheticTarget);
          syntheticVertices.add(syntheticTarget);
          // a SyntheticVertex will have one and only one outgoing edge
          LE<V, E> outgoingEdge = dag.outgoingEdgesOf(target).stream().findFirst().get();
          syntheticEdgesToRemove.add((SyntheticLE<V, E>) outgoingEdge);
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

  public void alignArticulatedEdges() {
    Set<Point> allInnerPoints = new HashSet<>();
    for (LE<V, E> edge : dag.edgeSet()) {
      if (edge instanceof SyntheticLE) {
        SyntheticLE<V, E> syntheticEdge = (SyntheticLE<V, E>) edge;
        LV<V> source = dag.getEdgeSource(edge);
        if (source instanceof SyntheticLV) {
          continue;
        }
        LV<V> target = dag.getEdgeTarget(syntheticEdge);
        Map<SyntheticLV<V>, Point> innerPoints = new LinkedHashMap<>();
        while (target instanceof SyntheticLV) {
          SyntheticLV<V> syntheticTarget = (SyntheticLV<V>) target;
          innerPoints.put(syntheticTarget, syntheticTarget.getPoint());
          // a SyntheticVertex will have one and only one outgoing edge
          LE<V, E> outgoingEdge = dag.outgoingEdgesOf(target).stream().findFirst().get();
          target = dag.getEdgeTarget(outgoingEdge);
        }

        // look at the x coords of all the points in the innerPoints list
        double avgx = innerPoints.values().stream().mapToDouble(p -> p.x).average().getAsDouble();
        log.trace("points: {}, avgx: {}", innerPoints.values(), avgx);
        boolean overlap = false;
        for (SyntheticLV<V> v : innerPoints.keySet()) {
          Point newPoint = Point.of(avgx, v.p.y);
          overlap |= allInnerPoints.contains(newPoint);
          allInnerPoints.add(newPoint);
          v.setPoint(newPoint);
        }
        if (overlap) {
          if (log.isTraceEnabled()) {
            log.trace("overlap at {}", innerPoints.keySet());
          }
          innerPoints.keySet().forEach(v -> v.setPoint(Point.of(v.p.x + 20, v.p.y)));
        }
      }
    }
    // check for any duplicate points
  }
}
