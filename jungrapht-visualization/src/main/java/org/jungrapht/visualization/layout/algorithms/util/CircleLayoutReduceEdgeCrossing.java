package org.jungrapht.visualization.layout.algorithms.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jungrapht.visualization.layout.util.synthetics.SE;
import org.jungrapht.visualization.layout.util.synthetics.SV;
import org.jungrapht.visualization.layout.util.synthetics.SVTransformedGraphSupplier;
import org.jungrapht.visualization.layout.util.synthetics.SyntheticEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircleLayoutReduceEdgeCrossing<V, E> {

  private static final Logger log = LoggerFactory.getLogger(CircleLayoutReduceEdgeCrossing.class);

  private Graph<V, E> originalGraph;
  private Graph<SV<V>, SE<E>> svGraph;
  private Comparator<SV<V>> ascendingDegreeComparator =
      Comparator.comparingInt(v -> svGraph.degreeOf(v));

  private List<SV<V>> tableList = new LinkedList<>();
  private Map<SV<V>, List<SV<V>>> tableMap = new HashMap<>();

  public CircleLayoutReduceEdgeCrossing(Graph<V, E> originalGraph) {
    this.originalGraph = originalGraph;
    SVTransformedGraphSupplier<V, E> transformedGraphSupplier =
        new SVTransformedGraphSupplier(originalGraph);
    this.svGraph = transformedGraphSupplier.get();
  }

  public List<V> getVertexOrderedList() {
    buildTable();
    List<V> vertexList = new LinkedList<>();
    List<SV<V>> waveFrontNodes = new LinkedList<>(); // ordered by degree of vertices.
    List<SV<V>> waveCenterNodes = new LinkedList<>(); // ordered by degree of vertices.
    List<SE<E>> removalList = new ArrayList<>();
    int n = svGraph.vertexSet().size();
    for (int counter = 0; counter < n - 3; counter++) {
      // if there is a wave front node lowest degree
      SV<V> currentNode;
      if (waveFrontNodes.size() > 0) {
        currentNode = waveFrontNodes.remove(0);
      } else if (waveCenterNodes.size() > 0) {
        currentNode = waveCenterNodes.remove(0);
      } else {
        currentNode = this.tableList.get(0);
      }
      // visit adjacent nodes consecutively
      List<SV<V>> adjacentNodes = tableMap.get(currentNode);
      for (int i = 0; i < adjacentNodes.size() - 1; i++) {
        // check i and i+1
        SV<V> v = adjacentNodes.get(i);
        SV<V> w = adjacentNodes.get(i + 1);
        if (svGraph.containsEdge(v, w) || svGraph.containsEdge(w, v)) {
          log.trace("currentNode: {} edge v, w: {},{} exists", currentNode, v, w);
          SE<E> edge = svGraph.getEdge(v, w);
          removalList.add(edge);
        } else {
          log.trace("currentNode: {} edge v, w: {},{} does not exist", currentNode, v, w);
          SyntheticEdge<E> syntheticEdge = new SyntheticEdge();
          svGraph.addEdge(v, w, syntheticEdge);
          removalList.add(syntheticEdge);
        }
        tableMap.get(v).remove(currentNode);
        tableMap.get(w).remove(currentNode);
      }
      /// remove current node and its incident edges from svGraph
      Collection<SE<E>> losers = new HashSet<>(svGraph.incomingEdgesOf(currentNode));
      losers.addAll(svGraph.outgoingEdgesOf(currentNode));
      svGraph.removeAllEdges(losers);
      svGraph.removeVertex(currentNode);
      //      nodes.remove(currentNode);
      waveCenterNodes.clear();
      waveCenterNodes.addAll(waveFrontNodes);
      waveCenterNodes.sort(ascendingDegreeComparator);
      waveFrontNodes.clear();
      waveFrontNodes.addAll(tableMap.get(currentNode));
      waveFrontNodes.sort(ascendingDegreeComparator);
      buildTable();
    }
    svGraph = new SVTransformedGraphSupplier(originalGraph).get();
    svGraph.removeAllEdges(removalList);
    if (log.isTraceEnabled()) {
      log.trace("removed losers to get {}", svGraph);
    }
    DepthFirstIterator<SV<V>, SE<E>> dfi = new DepthFirstIterator<>(svGraph);
    while (dfi.hasNext()) {
      vertexList.add(dfi.next().vertex);
    }
    for (SV<V> v : svGraph.vertexSet()) {
      if (!vertexList.contains(v.vertex)) {
        List<SV<V>> neighbors = Graphs.neighborListOf(svGraph, v);

        List<V> inList =
            neighbors
                .stream()
                .map(nn -> nn.vertex)
                .filter(vertexList::contains)
                .collect(Collectors.toList());
        if (inList.size() == 0) {
          vertexList.add(v.vertex);
        } else if (inList.size() == 1) {
          int idx = vertexList.indexOf(inList.get(0));
          vertexList.add(idx, v.vertex);
        } else {
          int[] idxes = new int[inList.size()];
          for (int i = 0; i < inList.size(); i++) {
            idxes[i] = vertexList.indexOf(inList.get(i));
          }
          // any consecutive?
          boolean consecutive = false;
          for (int j = 0; j < idxes.length - 1; j++) {
            if (Math.abs(idxes[j] - idxes[j + 1]) == 1) {
              vertexList.add(Math.max(idxes[j], idxes[j + 1]), v.vertex);
              consecutive = true;
              break;
            }
          }
          if (!consecutive) {
            vertexList.add(idxes[0], v.vertex);
          }
        }
      }
    }
    return vertexList;
  }

  private void buildTable() {
    tableList.clear();
    tableMap.clear();
    for (SV<V> v : svGraph.vertexSet()) {
      tableList.add(v);
      tableMap.put(
          v,
          Graphs.neighborSetOf(svGraph, v)
              .stream()
              .sorted(ascendingDegreeComparator)
              .collect(Collectors.toList()));
      tableList.sort(ascendingDegreeComparator);
    }
  }
}
