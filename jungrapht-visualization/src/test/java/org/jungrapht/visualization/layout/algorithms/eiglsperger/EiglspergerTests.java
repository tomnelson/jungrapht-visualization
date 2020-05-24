package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GraphLayers;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GreedyCycleRemoval;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.TransformedGraphSupplier;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EiglspergerTests {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerTests.class);

  Graph<String, Integer> graph;
  Graph<LV<String>, LE<String, Integer>> svGraph;
  NeighborCache<LV<String>, LE<String, Integer>> neighborCache;
  LV<String>[][] layersArray;
  EiglspergerRunnable<String, Integer> runnable;
  EiglspergerStepsForward<String, Integer> stepsForward;
  EiglspergerStepsBackward<String, Integer> stepsBackward;

  @Before
  public void setup() {
    buildGraph();
    createLayers();
    runnable = EiglspergerRunnable.<String, Integer>builder().build();
    neighborCache = new NeighborCache<>(svGraph);

    stepsForward = new EiglspergerStepsForward<>(svGraph, neighborCache, layersArray, true);
    stepsBackward = new EiglspergerStepsBackward<>(svGraph, neighborCache, layersArray, true);
  }

  @Test
  public void runForward() {

    // save off a map of edge lists keyed on the source vertex
    Map<Integer, List<LE<String, Integer>>> edgesKeyedOnSource = new LinkedHashMap<>();
    svGraph
        .edgeSet()
        .forEach(
            e -> {
              int sourceRank = e.getSource().getRank();
              if (edgesKeyedOnSource.containsKey(sourceRank)) {
                edgesKeyedOnSource.get(sourceRank).add(e);
              } else {
                ArrayList<LE<String, Integer>> list = new ArrayList<>();
                list.add(e);
                edgesKeyedOnSource.put(sourceRank, list);
              }
            });

    // save off a map of edge lists keyed on the target vertex
    Map<Integer, List<LE<String, Integer>>> edgesKeyedOnTarget = new LinkedHashMap<>();
    svGraph
        .edgeSet()
        .forEach(
            e -> {
              int targetRank = e.getTarget().getRank();
              if (edgesKeyedOnTarget.containsKey(targetRank)) {
                edgesKeyedOnTarget.get(targetRank).add(e);
              } else {
                ArrayList<LE<String, Integer>> list = new ArrayList<>();
                list.add(e);
                edgesKeyedOnTarget.put(targetRank, list);
              }
            });
    //    EiglspergerRunnable<String, Integer> runnable =
    //            EiglspergerRunnable.<String, Integer>builder().build();
    //    Map<LV<String>, Integer> pos = new HashMap<>();
    //    Map<LV<String>, Integer> measure = new HashMap<>();
    //    EiglspergerStepsForward<String, Integer> stepsForward =
    //            new EiglspergerStepsForward<>(svGraph, layersArray);
    //    EiglspergerStepsBackward<String, Integer> stepsBackward =
    //            new EiglspergerStepsBackward<>(svGraph, layersArray);

    stepsForward.sweep(layersArray);

    stepsBackward.sweep(layersArray);

    stepsForward.sweep(layersArray);

    stepsBackward.sweep(layersArray);

    stepsForward.sweep(layersArray);

    stepsBackward.sweep(layersArray);
  }

  @Test
  public void runTestForward() {

    // save off a map of edge lists keyed on the source vertex
    Map<Integer, List<LE<String, Integer>>> edgesKeyedOnSource = new LinkedHashMap<>();
    svGraph
        .edgeSet()
        .forEach(
            e -> {
              int sourceRank = e.getSource().getRank();
              if (edgesKeyedOnSource.containsKey(sourceRank)) {
                edgesKeyedOnSource.get(sourceRank).add(e);
              } else {
                ArrayList<LE<String, Integer>> list = new ArrayList<>();
                list.add(e);
                edgesKeyedOnSource.put(sourceRank, list);
              }
            });

    //    Map<LV<String>, Integer> pos = new HashMap<>();
    //    Map<LV<String>, Integer> measure = new HashMap<>();
    stepsForward.sweep(layersArray);
  }

  @Test
  public void runTestBackward() {
    // save off a map of edge lists keyed on the target vertex
    Map<Integer, List<LE<String, Integer>>> edgesKeyedOnTarget = new LinkedHashMap<>();
    svGraph
        .edgeSet()
        .forEach(
            e -> {
              int targetRank = e.getTarget().getRank();
              if (edgesKeyedOnTarget.containsKey(targetRank)) {
                edgesKeyedOnTarget.get(targetRank).add(e);
              } else {
                ArrayList<LE<String, Integer>> list = new ArrayList<>();
                list.add(e);
                edgesKeyedOnTarget.put(targetRank, list);
              }
            });

    //    Map<LV<String>, Integer> pos = new HashMap<>();
    //    Map<LV<String>, Integer> measure = new HashMap<>();
    stepsBackward.sweep(layersArray);
  }

  @Test
  public void stepOneTests() {
    List<LV<String>> list = EiglspergerUtil.createListOfVertices(layersArray[2]);
    list = EiglspergerUtil.scan(list);

    EiglspergerStepsForward<String, Integer> stepsForward =
        new EiglspergerStepsForward<>(svGraph, neighborCache, layersArray, true);

    stepsForward.stepOne(list);
    log.info("biLayer");
  }

  //
  /** transform the graph, remove cycles, and perform layering and virtual vertex/edge creation */
  private void createLayers() {
    TransformedGraphSupplier<String, Integer> transformedGraphSupplier =
        new TransformedGraphSupplier(graph);
    this.svGraph = transformedGraphSupplier.get();
    GreedyCycleRemoval<LV<String>, LE<String, Integer>> greedyCycleRemoval =
        new GreedyCycleRemoval(svGraph);
    Collection<LE<String, Integer>> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();

    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
    for (LE<String, Integer> se : feedbackArcs) {
      svGraph.removeEdge(se);
      LE<String, Integer> newEdge = LE.of(se.getEdge(), se.getTarget(), se.getSource());
      svGraph.addEdge(newEdge.getSource(), newEdge.getTarget(), newEdge);
    }

    List<List<LV<String>>> layers = GraphLayers.assign(svGraph);

    Synthetics<String, Integer> synthetics = new Synthetics<>(svGraph);
    List<LE<String, Integer>> edges = new ArrayList<>(svGraph.edgeSet());
    this.layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
    log.info("layers are {}", layersArray);
    rearrangeLayers();
  }

  private void rearrangeLayers() {
    LV<String>[] layer = layersArray[1];
    layersArray[1] = new LV[] {layer[1], layer[0], layer[2]};

    layer = layersArray[2];
    layersArray[2] = new LV[] {layer[1], layer[0], layer[2]};

    layer = layersArray[5];
    layersArray[5] = new LV[] {layer[1], layer[0], layer[2]};

    layer = layersArray[7];
    layersArray[7] =
        new LV[] {layer[3], layer[5], layer[2], layer[0], layer[4], layer[1], layer[6]};
  }

  //  private <V> void swapLayers(LV<V>[][] layersArray) {
  //    // fix the layer orders so that they match the paper
  //    // 0 is fine
  //    swap(layersArray[1], 0, 1);
  //
  //    //        LV<String>[] row = new LV[] {}
  //
  //    swap(layersArray[2], 3, 0);
  //    swap(layersArray[2], 3, 2);
  //    swap(layersArray[2], 3, 4);
  //
  //    swap(layersArray[3], 3, 0);
  //    swap(layersArray[3], 2, 1);
  //    swap(layersArray[3], 5, 1);
  //  }
  //
  //  private <T> void swap(T[] array, int a, int b) {
  //    T temp = array[a];
  //    array[a] = array[b];
  //    array[b] = temp;
  //  }

  private void buildGraph() {
    graph =
        GraphTypeBuilder.<String, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    graph.addVertex("00"); // rank 0, index 0
    graph.addVertex("10");
    graph.addVertex("20");
    graph.addVertex("30");
    graph.addVertex("40");
    graph.addVertex("50");
    graph.addVertex("60");

    graph.addVertex("70");
    graph.addVertex("71");

    graph.addVertex("80");
    graph.addVertex("81");
    graph.addVertex("82");
    graph.addVertex("83");
    graph.addVertex("84");
    graph.addVertex("85");
    graph.addVertex("86");

    graph.addEdge("00", "82");
    graph.addEdge("00", "10");
    graph.addEdge("00", "71");

    graph.addEdge("10", "80");
    graph.addEdge("10", "20");
    graph.addEdge("10", "84");

    graph.addEdge("20", "30");

    graph.addEdge("30", "40");

    graph.addEdge("40", "81");
    graph.addEdge("40", "50");
    graph.addEdge("40", "86");

    graph.addEdge("50", "60");

    graph.addEdge("60", "70");
    graph.addEdge("60", "71");

    graph.addEdge("70", "80");
    graph.addEdge("70", "81");
    graph.addEdge("70", "82");
    graph.addEdge("70", "83");
    graph.addEdge("70", "84");
    graph.addEdge("70", "85");
    graph.addEdge("70", "86");

    graph.addEdge("71", "85");
  }
}
