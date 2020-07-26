package org.jungrapht.visualization.layout.algorithms.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jungrapht.visualization.layout.util.synthetics.SE;
import org.jungrapht.visualization.layout.util.synthetics.SV;
import org.jungrapht.visualization.layout.util.synthetics.SVTransformedGraphSupplier;
import org.jungrapht.visualization.layout.util.synthetics.SyntheticSE;
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
  private V[] vertices;

  public CircleLayoutReduceEdgeCrossing(Graph<V, E> originalGraph) {
    this.originalGraph = originalGraph;
    SVTransformedGraphSupplier<V, E> transformedGraphSupplier =
        new SVTransformedGraphSupplier(originalGraph);
    this.svGraph = transformedGraphSupplier.get();
  }

  public List<V> getVertexOrderedList() {
    buildTable();
    List<V> vertexList = new ArrayList<>();
    List<SV<V>> waveFrontNodes = new ArrayList<>(); // ordered by degree of vertices.
    List<SV<V>> waveCenterNodes = new ArrayList<>(); // ordered by degree of vertices.
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
          SyntheticSE<E> syntheticSE = new SyntheticSE();
          svGraph.addEdge(v, w, syntheticSE);
          removalList.add(syntheticSE);
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
      vertexList.add(dfi.next().getVertex());
    }
    for (SV<V> v : svGraph.vertexSet()) {
      if (!vertexList.contains(v.getVertex())) {
        List<SV<V>> neighbors = Graphs.neighborListOf(svGraph, v);

        List<V> inList =
            neighbors
                .stream()
                .map(nn -> nn.getVertex())
                .filter(vertexList::contains)
                .collect(Collectors.toList());
        if (inList.size() == 0) {
          vertexList.add(v.getVertex());
        } else if (inList.size() == 1) {
          int idx = vertexList.indexOf(inList.get(0));
          vertexList.add(idx, v.getVertex());
        } else {
          int[] idxes = new int[inList.size()];
          for (int i = 0; i < inList.size(); i++) {
            idxes[i] = vertexList.indexOf(inList.get(i));
          }
          // any consecutive?
          boolean consecutive = false;
          for (int j = 0; j < idxes.length - 1; j++) {
            if (Math.abs(idxes[j] - idxes[j + 1]) == 1) {
              vertexList.add(Math.max(idxes[j], idxes[j + 1]), v.getVertex());
              consecutive = true;
              break;
            }
          }
          if (!consecutive) {
            vertexList.add(idxes[0], v.getVertex());
          }
        }
      }
      this.vertices = (V[]) vertexList.toArray(new Object[0]);
    }

    return postProcessing(originalGraph, vertexList);
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
              .collect(Collectors.toCollection(LinkedList::new)));
      tableList.sort(ascendingDegreeComparator);
    }
  }

  public static <V, E> int countCrossings(Graph<V, E> graph, V[] vertices) {
    Map<V, Integer> vertexListPositions = new HashMap<>();
    IntStream.range(0, vertices.length).forEach(i -> vertexListPositions.put(vertices[i], i));
    int numberOfCrossings = 0;
    Set<E> openEdgeList = new LinkedHashSet<>();
    List<V> verticesSeen = new LinkedList<>();
    for (V v : vertices) {
      log.trace("for vertex {}", v);
      verticesSeen.add(v);
      // sort the incident edges....
      List<E> incidentEdges = new ArrayList<>(graph.edgesOf(v));
      incidentEdges.sort(
          (e, f) -> {
            V oppe = Graphs.getOppositeVertex(graph, e, v);
            V oppf = Graphs.getOppositeVertex(graph, f, v);
            int idxv = vertexListPositions.get(v);
            int idxe = vertexListPositions.get(oppe);
            int idxf = vertexListPositions.get(oppf);
            int deltae = idxv - idxe;
            if (deltae < 0) {
              deltae += vertices.length;
            }
            int deltaf = idxv - idxf;
            if (deltaf < 0) {
              deltaf += vertices.length;
            }
            return Integer.compare(deltae, deltaf);
          });

      for (E e : incidentEdges) {
        V opposite = Graphs.getOppositeVertex(graph, e, v);
        if (!verticesSeen.contains(opposite)) {
          // e is an open edge
          openEdgeList.add(e);
        } else {
          openEdgeList.remove(e);
          for (int i = verticesSeen.indexOf(opposite) + 1; i < verticesSeen.indexOf(v); i++) {
            V tween = verticesSeen.get(i);
            numberOfCrossings +=
                graph.edgesOf(tween).stream().filter(openEdgeList::contains).count();
            log.trace("numberOfCrossings now {}", numberOfCrossings);
          }
        }
        log.trace("added edge {}", e);
      }
    }
    return numberOfCrossings;
  }

  public static <V, E> List<V> postProcessing(Graph<V, E> graph, List<V> list) {
    V[] array = (V[]) list.toArray();
    Map<V, Integer> vertexListPositions = new HashMap<>();
    IntStream.range(0, array.length).forEach(i -> vertexListPositions.put(array[i], i));
    int currentCrossings = countCrossings(graph, array);
    log.trace("originalCrossings: {}", currentCrossings);
    int originalCrossings = currentCrossings;
    List<Integer> positions = new LinkedList<>();
    for (int i = 0; i < 9; i++) {
      for (V v : graph.vertexSet()) {
        positions.clear();
        if (graph.degreeOf(v) > 1) {
          // there are at least 2
          List<E> incidentEdges = new ArrayList<>(graph.edgesOf(v));
          // get two nodes
          V one = Graphs.getOppositeVertex(graph, incidentEdges.get(0), v);
          V two = Graphs.getOppositeVertex(graph, incidentEdges.get(1), v);
          int idxOne = vertexListPositions.get(one);
          int idxTwo = vertexListPositions.get(two);
          IntStream.range(idxOne + 1, idxTwo).forEach(positions::add);
          if (positions.isEmpty()) {
            positions.add(idxOne - 1);
            positions.add(idxOne + 1);
          }
          for (int pos = 0; pos < positions.size(); pos++) {
            // put u at pos and whatever was at pos at u's position
            int vpos = vertexListPositions.get(v);
            swap(array, vpos, pos);
            vertexListPositions.put(array[pos], pos);
            vertexListPositions.put(array[vpos], vpos);
            int newCrossings = countCrossings(graph, array);
            if (newCrossings < currentCrossings) {
              currentCrossings = newCrossings;
              log.trace("reduced crossings to {}", currentCrossings);
            } else {
              swap(array, vpos, pos);
              vertexListPositions.put(array[pos], pos);
              vertexListPositions.put(array[vpos], vpos);
            }
          }
        }
      }
      if (currentCrossings >= originalCrossings) {
        log.trace("break {} >= {}", currentCrossings, originalCrossings);
        break;
      }
    }
    return Arrays.asList(array);
  }

  private static <T> void swap(T[] array, int i, int j) {
    T temp = array[i];
    array[i] = array[j];
    array[j] = temp;
  }
}
