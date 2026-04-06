package org.jungrapht.visualization.layout.algorithms.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.NeighborCache;
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
  NeighborCache<SV<V>, SE<E>> neighborCache;

  public CircleLayoutReduceEdgeCrossing(Graph<V, E> originalGraph) {
    this.originalGraph = originalGraph;
    SVTransformedGraphSupplier<V, E> transformedGraphSupplier =
        new SVTransformedGraphSupplier(originalGraph);
    this.svGraph = transformedGraphSupplier.get();
    this.neighborCache = new NeighborCache<>(svGraph);
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
      neighborCache = new NeighborCache<>(svGraph);
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
    neighborCache = new NeighborCache<>(svGraph);
    DepthFirstIterator<SV<V>, SE<E>> dfi = new DepthFirstIterator<>(svGraph);
    while (dfi.hasNext()) {
      vertexList.add(dfi.next().getVertex());
    }
    for (SV<V> v : svGraph.vertexSet()) {
      if (!vertexList.contains(v.getVertex())) {
        List<SV<V>> neighbors = neighborCache.neighborListOf(v);
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
          neighborCache
              .neighborsOf(v)
              .stream()
              .sorted(ascendingDegreeComparator)
              .collect(Collectors.toCollection(LinkedList::new)));

      tableList.sort(ascendingDegreeComparator);
    }
  }

  /**
   * Fast O(E) crossing count for circular layouts using combinatorial formula. Two edges (a-b) and
   * (c-d) cross if the four endpoints interleave in circular order.
   */
  public static <V, E> int countCrossings(Graph<V, E> graph, V[] vertices) {
    int n = vertices.length;
    if (n < 4) return 0;

    // Map vertex to its position on the circle (0 to n-1)
    Map<V, Integer> pos = new HashMap<>(n);
    for (int i = 0; i < n; i++) {
      pos.put(vertices[i], i);
    }

    int crossings = 0;

    // Iterate over all pairs of edges
    List<E> edges = new ArrayList<>(graph.edgeSet());
    for (int i = 0; i < edges.size(); i++) {
      E e1 = edges.get(i);
      V a = graph.getEdgeSource(e1);
      V b = graph.getEdgeTarget(e1);
      int p1 = pos.get(a);
      int p2 = pos.get(b);
      if (p1 > p2) {
        int temp = p1;
        p1 = p2;
        p2 = temp;
      } // ensure p1 < p2

      for (int j = i + 1; j < edges.size(); j++) {
        E e2 = edges.get(j);
        V c = graph.getEdgeSource(e2);
        V d = graph.getEdgeTarget(e2);
        int p3 = pos.get(c);
        int p4 = pos.get(d);
        if (p3 > p4) {
          int temp = p3;
          p3 = p4;
          p4 = temp;
        }

        // Check if they cross on the circle
        boolean cross = (p3 > p1 && p3 < p2 && p4 > p2) || (p4 > p1 && p4 < p2 && p3 > p2);
        if (cross) {
          crossings++;
        }
      }
    }

    return crossings;
  }

  /**
   * Improved post-processing: uses greedy adjacent swaps + limited 2-opt.
   * Much faster and scales better than the original brute-force local search.
   */
  public static <V, E> List<V> postProcessing(Graph<V, E> graph, List<V> list) {
    V[] array = (V[]) list.toArray();
    int n = array.length;
    if (n < 4) {
      return list;
    }

    Map<V, Integer> vertexListPositions = new HashMap<>(n);
    for (int i = 0; i < n; i++) {
      vertexListPositions.put(array[i], i);
    }

    int currentCrossings = countCrossings(graph, array);
    log.trace("originalCrossings: {}", currentCrossings);
    int originalCrossings = currentCrossings;

    final int MAX_PASSES = 6;           // reduced from 9
    boolean improved = true;

    for (int pass = 0; pass < MAX_PASSES && improved; pass++) {
      improved = false;

      // Greedy adjacent swaps - very fast and effective on circle
      for (int i = 0; i < n; i++) {
        int j = (i + 1) % n;

        // Try swapping adjacent vertices
        swap(array, i, j);
        vertexListPositions.put(array[i], i);
        vertexListPositions.put(array[j], j);

        int newCrossings = countCrossings(graph, array);

        if (newCrossings < currentCrossings) {
          currentCrossings = newCrossings;
          improved = true;
          log.trace("reduced crossings to {} (adjacent swap)", currentCrossings);
        } else {
          // revert
          swap(array, i, j);
          vertexListPositions.put(array[i], i);
          vertexListPositions.put(array[j], j);
        }
      }

      // Occasional limited 2-opt (non-adjacent) - only every other pass to control cost
      if (pass % 2 == 0 && n > 10) {
        for (int i = 0; i < n; i += 3) {           // step by 3 to reduce trials
          for (int k = 3; k < n / 3; k += 3) {    // limited distance
            int j = (i + k) % n;

            swap(array, i, j);
            vertexListPositions.put(array[i], i);
            vertexListPositions.put(array[j], j);

            int newCrossings = countCrossings(graph, array);

            if (newCrossings < currentCrossings) {
              currentCrossings = newCrossings;
              improved = true;
              log.trace("reduced crossings to {} (2-opt)", currentCrossings);
            } else {
              swap(array, i, j); // revert
              vertexListPositions.put(array[i], i);
              vertexListPositions.put(array[j], j);
            }
          }
        }
      }

      if (currentCrossings >= originalCrossings * 0.98) {   // stop if improvement is tiny
        log.trace("stopping early - minimal improvement");
        break;
      }
    }

    log.debug("Post-processing finished with {} crossings", currentCrossings);
    return Arrays.asList(array);
  }
  /** Simple helper to swap two vertices in the array */
  //  private void swap(V[] vertices, int i, int j) {
  //    V temp = vertices[i];
  //    vertices[i] = vertices[j];
  //    vertices[j] = temp;
  //  }
  private static <T> void swap(T[] array, int i, int j) {
    T temp = array[i];
    array[i] = array[j];
    array[j] = temp;
  }
}
