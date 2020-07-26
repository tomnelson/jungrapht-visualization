package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.*;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jungrapht.visualization.layout.algorithms.util.ComponentGrouping;
import org.jungrapht.visualization.layout.algorithms.util.NetworkSimplex;
import org.jungrapht.visualization.layout.algorithms.util.NetworkSimplexDevelopment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphLayers {

  private static final Logger log = LoggerFactory.getLogger(GraphLayers.class);

  private GraphLayers() {}

  public static <V, E> List<List<LV<V>>> assign(Graph<LV<V>, LE<V, E>> dag) {
    int rank = 0;
    List<List<LV<V>>> sorted = new ArrayList<>();
    List<LE<V, E>> edges = dag.edgeSet().stream().collect(Collectors.toCollection(LinkedList::new));
    List<LV<V>> vertices =
        dag.vertexSet().stream().collect(Collectors.toCollection(LinkedList::new));
    List<LV<V>> start =
        getVerticesWithoutIncomingEdges(dag, edges, vertices); // should be the roots

    // if there are multiple components, arrange the first row order to group their roots
    start = ComponentGrouping.groupByComponents(dag, start);

    while (start.size() > 0) {
      for (int i = 0; i < start.size(); i++) {
        LV<V> v = start.get(i);
        v.setRank(rank);
        v.setIndex(i);
      }
      sorted.add(start); // add a row
      Set<LV<V>> fstart = new HashSet<>(start);
      // remove any edges that start in the new row
      edges.removeIf(e -> fstart.contains(dag.getEdgeSource(e)));
      // remove any vertices that have been added to the row
      vertices.removeIf(fstart::contains);
      start = getVerticesWithoutIncomingEdges(dag, edges, vertices);
      //      if (componentVertices.size() > 1) {
      //        start = groupByComponentMembership(componentVertices, start);
      //      }
      rank++;
    }
    return sorted;
  }

  public static <V, E> void minimizeEdgeLength(
      Graph<LV<V>, LE<V, E>> dag, List<List<LV<V>>> layers) {
    for (int i = layers.size() - 1; i >= 0; i--) {
      List<LV<V>> layer = layers.get(i);
      Map<LV<V>, Integer> verticesToMove = new HashMap<>();

      for (LV<V> v : layer) {
        if (dag.outDegreeOf(v) == 0) {
          continue;
        }
        int minRank =
            dag.outgoingEdgesOf(v)
                .stream()
                .mapToInt(e -> dag.getEdgeTarget(e).getRank() - 1)
                .min()
                .getAsInt();

        if (minRank != v.getRank()) {
          // change the rank of v and move it (below)
          verticesToMove.put(v, minRank);
          if (log.isTraceEnabled()) {
            log.trace("moving {} to rank {}", v, minRank);
          }
        }
      }
      for (Map.Entry<LV<V>, Integer> entry : verticesToMove.entrySet()) {
        LV<V> v = entry.getKey();
        int oldRank = entry.getKey().getRank();
        int newRank = entry.getValue();
        layers.get(oldRank).remove(v);
        layers.get(newRank).add(v);
        v.setRank(newRank);
      }
    }
  }

  private static <V> List<LV<V>> groupByComponentMembership(
      List<Set<LV<V>>> componentVertices, List<LV<V>> list) {
    List<LV<V>> groupedRow = new ArrayList<>();
    for (Set<LV<V>> set : componentVertices) {
      groupedRow.addAll(list.stream().filter(set::contains).collect(Collectors.toList()));
    }
    return groupedRow;
  }

  public static <V, E> List<List<LV<V>>> longestPath(Graph<LV<V>, LE<V, E>> dag) {
    List<List<LV<V>>> list = new ArrayList<>();

    Set<LV<V>> U = new HashSet<>();
    Set<LV<V>> Z = new HashSet<>();
    Set<LV<V>> V = new HashSet<>(dag.vertexSet());
    int currentLayer = 0;
    list.add(new ArrayList<>());
    while (U.size() != dag.vertexSet().size()) {
      Optional<LV<V>> optional =
          V.stream()
              .filter(v -> !U.contains(v))
              .filter(v -> Z.containsAll(Graphs.successorListOf(dag, v)))
              .findFirst();
      if (optional.isPresent()) {
        LV<V> got = optional.get();
        //        got.setRank(currentLayer);
        list.get(currentLayer).add(got);
        U.add(got);
      } else {
        currentLayer++;
        list.add(new ArrayList<>());
        Z.addAll(U);
      }
    }

    Collections.reverse(list);
    for (int i = 0; i < list.size(); i++) {
      List<LV<V>> layer = list.get(i);
      for (int j = 0; j < layer.size(); j++) {
        LV<V> v = layer.get(j);
        v.setRank(i);
        v.setIndex(j);
      }
    }

    return list;
  }

  public static <V, E> List<List<LV<V>>> longestPathReverse(Graph<LV<V>, LE<V, E>> dag) {
    List<List<LV<V>>> list = new ArrayList<>();

    Set<LV<V>> U = new HashSet<>();
    Set<LV<V>> Z = new HashSet<>();
    Set<LV<V>> V = new HashSet<>(dag.vertexSet());
    int currentLayer = 0;
    list.add(new ArrayList<>());
    while (U.size() != dag.vertexSet().size()) {
      Optional<LV<V>> optional =
          V.stream()
              .filter(v -> !U.contains(v))
              .filter(v -> Z.containsAll(Graphs.successorListOf(dag, v)))
              .findFirst();
      if (optional.isPresent()) {
        LV<V> got = optional.get();
        //        got.setRank(currentLayer);
        list.get(currentLayer).add(got);
        U.add(got);
      } else {
        currentLayer++;
        list.add(new ArrayList<>());
        Z.addAll(U);
      }
    }

    //    Collections.reverse(list);
    for (int i = 0; i < list.size(); i++) {
      List<LV<V>> layer = list.get(i);
      for (int j = 0; j < layer.size(); j++) {
        LV<V> v = layer.get(j);
        v.setRank(i);
        v.setIndex(j);
      }
    }

    return list;
  }

  public static <V, E> List<List<LV<V>>> networkSimplex(Graph<LV<V>, LE<V, E>> dag) {
    List<List<LV<V>>> layerList = new ArrayList<>();
    List<Graph<LV<V>, LE<V, E>>> componentList = ComponentGrouping.getComponentGraphs(dag);
    componentList.sort(Comparator.comparingInt(l -> -componentList.size()));
    for (Graph<LV<V>, LE<V, E>> sub : componentList) {
      NetworkSimplex<V, E> ns = NetworkSimplex.builder(sub).build();
      ns.run();
      List<List<LV<V>>> layers = ns.getLayerList();
      for (int i = 0; i < layers.size(); i++) {
        List<LV<V>> layer = layers.get(i);
        if (layerList.size() <= i) {
          layerList.add(new ArrayList<>());
        }
        layerList.get(i).addAll(layer);
      }
    }

    Collections.reverse(layerList);

    for (int i = 0; i < layerList.size(); i++) {
      List<LV<V>> layer = layerList.get(i);
      for (int j = 0; j < layer.size(); j++) {
        LV<V> v = layer.get(j);
        v.setRank(i);
        v.setIndex(j);
      }
    }

    return layerList;
  }

  public static <V, E> List<List<LV<V>>> coffmanGraham(Graph<LV<V>, LE<V, E>> dag, int width) {
    if (width == 0) {
      width = dag.vertexSet().size() / 10;
    }
    List<List<LV<V>>> list = new ArrayList<>();
    Set<LV<V>> Z = new HashSet<>(); // seen
    Map<LV<V>, Integer> lambda = new HashMap<>(); //
    Set<LV<V>> V = new HashSet<>(dag.vertexSet());
    V.stream().forEach(v -> lambda.put(v, Integer.MAX_VALUE));

    for (int i = 0; i < V.size(); i++) {
      LV<V> mv =
          V.stream()
              .filter(v -> lambda.get(v) == Integer.MAX_VALUE)
              .min(Comparator.comparingInt(dag::inDegreeOf))
              .get();
      lambda.put(mv, i);
    }
    int k = 0;
    list.add(new ArrayList<>());
    Set<LV<V>> U = new HashSet<>();
    while (U.size() != dag.vertexSet().size()) {
      LV<V> got =
          V.stream()
              .filter(v -> !U.contains(v))
              .filter(v -> U.containsAll(Graphs.successorListOf(dag, v)))
              .max(Comparator.comparingInt(lambda::get))
              .get();
      if (list.get(k).size() < width && Z.containsAll(Graphs.successorListOf(dag, got))) {
        list.get(k).add(got);
      } else {
        Z.addAll(list.get(k));
        k++;
        list.add(new ArrayList<>());
        list.get(k).add(got);
      }
      U.add(got);
    }
    // remove any empty lists from list
    list = list.stream().filter(l -> !l.isEmpty()).collect(Collectors.toList());

    Collections.reverse(list);
    for (int i = 0; i < list.size(); i++) {
      List<LV<V>> layer = list.get(i);
      for (int j = 0; j < layer.size(); j++) {
        LV<V> v = layer.get(j);
        v.setRank(i);
        v.setIndex(j);
      }
    }

    return list;
  }

  private static <V, E> int tightTree(Graph<V, E> dag) {
    NetworkSimplexDevelopment<V, E> networkSimplexDevelopment =
        new NetworkSimplexDevelopment<>(dag);
    return networkSimplexDevelopment.getTheBestSpanningTree().vertexSet().size();
  }

  //  private static <V, E> Graph<LV<V>, LE<V,E>> feasibleTree(Graph<LV<V>, LE<V,E>> dag) {
  //    List<List<LV<V>>> list = GraphLayers.longestPath(dag);
  //    NetworkSimplex<LV<V>,LE<V,E>> networkSimplex = new NetworkSimplex<>(dag);
  //    Graph<LV<V>,LE<V,E>> tree = networkSimplex.getTheBestSpanningTree();
  //    Map<LE<V,E>, Integer> cutValueMap = networkSimplex.getEdgeCutValues(tree);
  //    while (tree.vertexSet().size() < dag.vertexSet().size()) {
  //      LE<V,E> le = dag.edgeSet().stream()
  //              .filter(v -> !tree.containsEdge(v)).min(Comparator.comparingInt(cutValueMap::get)).get();
  //
  //
  //
  //      int delta = cutValueMap.get(le);
  //
  //
  //      Pair<Set<V>> headAndTail = networkSimplex.getHeadAndTailComponents(tree, le);
  //      spanningTree.removeEdge(edgeToCut);
  //      Set<E> crossComponentEdges = getCrossComponentEdges(spanningTree, headAndTail);
  //
  //    }
  //    return null;
  //  }

  /**
   * Find all vertices that have no incoming edges by
   *
   * <p>
   *
   * <ul>
   *   <li>collect all vertices that are edge targets
   *   <li>collect all vertices that are not part of that set of targets
   * </ul>
   *
   * Note that loop edges have already been removed from the graph, so any vertices that have only
   * loop edges will appear in the set of vertices without incoming edges.
   *
   * @param dag the {@code Graph} to examine
   * @param edges all edges from the {@code Graph}. Note that loop edges have already been removed
   *     from this set
   * @param vertices all vertices in the {@code Graph} (including vertices that have only loop edges
   * @param <V> vertex type
   * @param <E> edge type
   * @return vertices in the graph that have no incoming edges (or only loop edges)
   */
  private static <V, E> List<LV<V>> getVerticesWithoutIncomingEdges(
      Graph<LV<V>, LE<V, E>> dag, Collection<LE<V, E>> edges, Collection<LV<V>> vertices) {
    // get targets of all edges
    Set<LV<V>> targets = edges.stream().map(e -> dag.getEdgeTarget(e)).collect(Collectors.toSet());
    // from vertices, filter out any that are an edge target
    return vertices.stream().filter(v -> !targets.contains(v)).collect(Collectors.toList());
  }

  public static <V> void checkLayers(List<List<LV<V>>> layers) {
    for (int i = 0; i < layers.size(); i++) {
      List<LV<V>> layer = layers.get(i);
      for (int j = 0; j < layer.size(); j++) {
        LV<V> LV = layer.get(j);
        if (i != LV.getRank()) {
          log.error("{} is not the rank of {}", i, LV);
          throw new RuntimeException("rank is wrong");
        }
        if (j != LV.getIndex()) {
          log.error("{} is not the index of {}", j, LV);
          throw new RuntimeException("index is wrong");
        }
      }
    }
  }

  public static <V> void checkLayers(LV<V>[][] layers) {
    if (log.isTraceEnabled()) {
      for (int i = 0; i < layers.length; i++) {
        for (int j = 0; j < layers[i].length; j++) {
          if (i != layers[i][j].getRank()) {
            log.error("{} is not the rank of {}", i, layers[i][j]);
            throw new RuntimeException(i + " is not the rank of " + layers[i][j]);
          }
          if (j != layers[i][j].getIndex()) {
            log.error("{} is not the index of {}", j, layers[i][j]);
            throw new RuntimeException(j + " is not the index of " + layers[i][j]);
          }
        }
      }
    }
  }

  public static <V, E> boolean isLoopVertex(Graph<V, E> graph, V v) {
    return graph.outgoingEdgesOf(v).equals(graph.incomingEdgesOf(v));
  }

  public static <V, E> boolean isZeroDegreeVertex(Graph<V, E> graph, V v) {
    return graph.degreeOf(v) == 0;
  }

  public static <V, E> boolean isIsolatedVertex(Graph<V, E> graph, V v) {
    return isLoopVertex(graph, v) || isZeroDegreeVertex(graph, v);
  }

  /**
   * to set vertex order to normal -> loop -> zeroDegree
   *
   * @param graph
   * @param v
   * @param <V>
   * @param <E>
   * @return
   */
  public static <V, E> int vertexIsolationScore(Graph<V, E> graph, V v) {
    if (isZeroDegreeVertex(graph, v)) return 2;
    if (isLoopVertex(graph, v)) return 1;
    return 0;
  }
}
