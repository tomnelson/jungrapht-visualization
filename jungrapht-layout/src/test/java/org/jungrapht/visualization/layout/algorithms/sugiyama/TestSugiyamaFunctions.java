package org.jungrapht.visualization.layout.algorithms.sugiyama;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.util.synthetics.SE;
import org.jungrapht.visualization.layout.util.synthetics.SV;
import org.jungrapht.visualization.layout.util.synthetics.SVTransformedGraphSupplier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSugiyamaFunctions {

  private static final Logger log = LoggerFactory.getLogger(TestSugiyamaFunctions.class);

  Graph<String, Integer> graph;

  @Before
  public void setup() {
    // build a DAG
    graph =
        GraphTypeBuilder.<String, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    graph.addVertex("00"); // rank 0 index 0
    graph.addVertex("01"); // rank 0 index 1
    graph.addVertex("10"); // rank 1 index 0
    graph.addVertex("11"); // rank 1 index 1
    graph.addVertex("20"); // rank 2 index 0
    graph.addVertex("21"); // rank 2 index 1
    graph.addEdge("00", "10"); // connect from rank 0 to rank 1
    graph.addEdge("10", "20"); // connect from rank 1 to rank 2
    graph.addEdge("00", "11"); // connect from rank 0 -> 1
    graph.addEdge("11", "21"); // connect from rank 1 -> 2
    graph.addEdge(
        "01",
        "20"); // connect from rank 1 -> 2 (this edge should be replaced with 2 synthetic edges)
    log.info(
        "graph has {} vertices and {} edges", graph.vertexSet().size(), graph.edgeSet().size());
    log.info("graph: {}", graph);
  }

  @Test
  public void testTransformedGraph() {

    SVTransformedGraphSupplier<String, Integer> svTransformedGraphSupplier =
        new SVTransformedGraphSupplier(graph);
    Graph<SV<String>, SE<Integer>> sgraph = svTransformedGraphSupplier.get();

    Assert.assertEquals(graph.vertexSet().size(), sgraph.vertexSet().size());
    Assert.assertEquals(graph.edgeSet().size(), sgraph.edgeSet().size());

    Set<String> verticesCopy = new HashSet<>(graph.vertexSet());
    Set<Integer> edgesCopy = new HashSet<>(graph.edgeSet());
    sgraph
        .vertexSet()
        .forEach(
            v -> {
              Assert.assertTrue(graph.containsVertex(v.getVertex()));
              verticesCopy.remove(v.getVertex());
            });
    Assert.assertTrue(verticesCopy.isEmpty());
    sgraph
        .edgeSet()
        .forEach(
            e -> {
              Assert.assertTrue(graph.containsEdge(e.getEdge()));
              edgesCopy.remove(e.getEdge());
            });
    Assert.assertTrue(edgesCopy.isEmpty());
  }

  @Test
  public void testAssignLayers() {

    TransformedGraphSupplier<String, Integer> svTransformedGraphSupplier =
        new TransformedGraphSupplier(graph);
    Graph<LV<String>, LE<String, Integer>> sgraph = svTransformedGraphSupplier.get();

    List<List<LV<String>>> layers = GraphLayers.assign(sgraph);

    Assert.assertEquals(3, layers.size()); // this graph should have 3 layers

    this.checkLayers(layers);
    log.info("assign layers:");
    for (List<LV<String>> layer : layers) {
      log.info("Layer: {}", layer);
    }
    log.info("virtual vertices and edges:");
    Synthetics<String, Integer> synthetics = new Synthetics<>(sgraph);
    List<LE<String, Integer>> edges = new ArrayList<>(sgraph.edgeSet());
    log.info("there are {} edges ", edges.size());
    log.info("edges: {}", edges);
    LV<String>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
    for (int i = 0; i < layersArray.length; i++) {
      //    for (List<SugiyamaVertex<String>> layer : layers) {
      log.info("Layer: {}", Arrays.toString(layersArray[i]));
    }
    log.info("there are {} edges ", edges.size());
    log.info("edges: {}", edges);
    Assert.assertEquals(graph.edgeSet().size(), edges.size() - 1);
    checkLayers(layers);
  }

  @Test
  public void testAssignLayersWithDoubleSkip() {

    graph.addVertex("30"); // rank 2 index 1
    graph.addEdge("20", "30");
    graph.addEdge("00", "30"); // connect from rank 0 to rank 3. should add 3, subtract 1

    TransformedGraphSupplier<String, Integer> svTransformedGraphSupplier =
        new TransformedGraphSupplier(graph);
    Graph<LV<String>, LE<String, Integer>> sgraph = svTransformedGraphSupplier.get();

    log.info("incoming dag: {}", sgraph);

    //    AssignLayers<String, Integer> assignLayers = new AssignLayers<>(sgraph);
    List<List<LV<String>>> layers = GraphLayers.assign(sgraph);

    //            assignLayers.assignLayers();
    this.checkLayers(layers);

    log.info("assign layers:");
    for (List<LV<String>> layer : layers) {
      log.info("Layer: {}", layer);
    }
    log.info("virtual vertices and edges:");
    Synthetics<String, Integer> synthetics = new Synthetics<>(sgraph);
    List<LE<String, Integer>> edges = new ArrayList<>(sgraph.edgeSet());
    log.info("there are {} edges ", edges.size());
    log.info("edges: {}", edges);
    LV<String>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
    for (int i = 0; i < layersArray.length; i++) {
      log.info("Layer: {}", Arrays.toString(layersArray[i]));
    }
    log.info("there are {} edges ", edges.size());
    log.info("edges: {}", edges);
    log.info("graph.edgeSet(): {}", graph.edgeSet());
    log.info("edges.size(): {}", edges.size());
    Assert.assertEquals(graph.edgeSet().size(), edges.size() - 3);
    checkLayers(layers);

    log.info("outgoing dag: {}", sgraph.toString());
    // should look like this:
    /*
    SV{vertex=00, rank=0, index=0}, SV{vertex=01, rank=0, index=1},
    SV{vertex=10, rank=1, index=0}, SV{vertex=11, rank=1, index=1},
    SV{vertex=20, rank=2, index=0}, SV{vertex=21, rank=2, index=1},
    SV{vertex=30, rank=3, index=0}], [
    SE{edge=0, source=SV{vertex=00, rank=0, index=0}, intermediateVertices=[], target=SV{vertex=10, rank=1, index=0}}=(SV{vertex=00, rank=0, index=0},SV{vertex=10, rank=1, index=0}),
    SE{edge=1, source=SV{vertex=10, rank=1, index=0}, intermediateVertices=[], target=SV{vertex=20, rank=2, index=0}}=(SV{vertex=10, rank=1, index=0},SV{vertex=20, rank=2, index=0}),
    SE{edge=2, source=SV{vertex=00, rank=0, index=0}, intermediateVertices=[], target=SV{vertex=11, rank=1, index=1}}=(SV{vertex=00, rank=0, index=0},SV{vertex=11, rank=1, index=1}),
    SE{edge=3, source=SV{vertex=11, rank=1, index=1}, intermediateVertices=[], target=SV{vertex=21, rank=2, index=1}}=(SV{vertex=11, rank=1, index=1},SV{vertex=21, rank=2, index=1}),

    SE{edge=4,
       source=SV{vertex=01, rank=0, index=1},
       intermediateVertices=[SyntheticVertex{vertex=1048855692, rank=1, index=3}],
       target=SV{vertex=20, rank=2, index=0}}=                        (SV{vertex=01, rank=0, index=1},SV{vertex=20, rank=2, index=0}),

    SE{edge=5, source=SV{vertex=20, rank=2, index=0}, intermediateVertices=[], target=SV{vertex=30, rank=3, index=0}}=(SV{vertex=20, rank=2, index=0},SV{vertex=30, rank=3, index=0}),

    SE{edge=6,
       source=SV{vertex=00, rank=0, index=0},
       intermediateVertices=[SyntheticVertex{vertex=702061917, rank=1, index=2}, SyntheticVertex{vertex=1409545055, rank=2, index=2}],
       target=SV{vertex=30, rank=3, index=0}}=                           (SV{vertex=00, rank=0, index=0},SV{vertex=30, rank=3, index=0})])

    */

    // all edges for no skipped layers
    sgraph.edgeSet().forEach(this::testEdgeHasCorrectRanks);

    // test that the graph has 3 virtualVertices
    List<SyntheticLV<String>> virtualVertices =
        sgraph
            .vertexSet()
            .stream()
            .filter(v -> v instanceof SyntheticLV)
            .map(v -> (SyntheticLV<String>) v)
            .collect(Collectors.toList());

    Assert.assertEquals(3, virtualVertices.size());

    synthetics.makeArticulatedEdges();

    // test that one edge has 2 intermediate vertices
    List<ArticulatedEdge<String, Integer>> articulatedEdges =
        sgraph
            .edgeSet()
            .stream()
            .filter(
                e ->
                    e instanceof ArticulatedEdge
                        && ((ArticulatedEdge) e).getIntermediateVertices().size() == 2)
            .map(e -> (ArticulatedEdge<String, Integer>) e)
            .collect(Collectors.toList());
    Assert.assertEquals(1, articulatedEdges.size());
    ArticulatedEdge<String, Integer> bentEdge = articulatedEdges.get(0);
    List<Integer> ranks = new ArrayList<>();
    ranks.add(bentEdge.source.getRank());
    ranks.addAll(
        bentEdge.getIntermediateVertices().stream().map(LV::getRank).collect(Collectors.toList()));
    ranks.add(bentEdge.target.getRank());
    Assert.assertEquals(ranks, List.of(0, 1, 2, 3));

    // test that one edge has 1 intermediate vertex
    articulatedEdges =
        sgraph
            .edgeSet()
            .stream()
            .filter(
                e ->
                    e instanceof ArticulatedEdge
                        && ((ArticulatedEdge) e).getIntermediateVertices().size() == 1)
            .map(e -> (ArticulatedEdge<String, Integer>) e)
            .collect(Collectors.toList());
    Assert.assertEquals(1, articulatedEdges.size());
    bentEdge = articulatedEdges.get(0);
    ranks = new ArrayList<>();
    ranks.add(bentEdge.source.getRank());
    ranks.addAll(
        bentEdge.getIntermediateVertices().stream().map(LV::getRank).collect(Collectors.toList()));
    ranks.add(bentEdge.target.getRank());
    Assert.assertEquals(ranks, List.of(0, 1, 2));

    log.info("dag vertices: {}", sgraph.vertexSet());
    log.info("dag edges: {}", sgraph.edgeSet());
  }

  private void testEdgeHasCorrectRanks(LE<String, Integer> edge) {
    List<Integer> ranks = new ArrayList<>();
    ranks.add(edge.getSource().getRank());
    if (edge instanceof ArticulatedEdge) {
      ranks.addAll(
          ((ArticulatedEdge<String, Integer>) edge)
              .getIntermediateVertices()
              .stream()
              .map(LV::getRank)
              .collect(Collectors.toList()));
    }
    ranks.add(edge.getTarget().getRank());
    testConsecutive(ranks.stream().mapToInt(Integer::intValue).toArray());
  }

  /**
   * ensure that every SV vertex has the rank and index data assigned that matches that vertex's
   * actual rank (layer number) and index (position in layer)
   *
   * @param layers
   */
  private void checkLayers(List<List<LV<String>>> layers) {
    for (int i = 0; i < layers.size(); i++) {
      List<LV<String>> layer = layers.get(i);
      log.info("layer: {}", layer);
      for (int j = 0; j < layer.size(); j++) {
        LV<String> LV = layer.get(j);
        log.info("sv {},{}: {}", i, j, LV);
        Assert.assertEquals(i, LV.getRank());
        Assert.assertEquals(j, LV.getIndex());
      }
    }
  }

  private void testConsecutive(int[] array) {
    for (int i = 0; i < array.length - 1; i++) {
      Assert.assertEquals(array[i] + 1, array[i + 1]);
    }
  }

  public int testCountEdges(int[] tree, int n, int last) {
    if (n == last) return 0;
    int base = tree.length / 2;
    int pos = n + base;
    int sum = 0;
    while (pos >= 0) {
      if (pos % 2 != 0) { // odd
        sum += tree[pos - 1];
      }
      pos /= 2;
    }
    return tree[0] - sum;
  }

  @Test
  public void testInsertionSortCounter() {
    int[] array = new int[] {0, 1, 2, 3, 4, 5};
    int count = insertionSortCounter(array);
    log.info("count is {}", count);
    Assert.assertEquals(0, count);

    array = new int[] {0, 1, 2, 0, 3, 4, 0, 2, 3, 2, 4};
    count = insertionSortCounter(array);
    log.info("count is {}", count);
    Assert.assertEquals(12, count);

    array = new int[] {0, 1, 3, 1, 2};
    count = insertionSortCounter(array);
    log.info("count is {}", count);
    Assert.assertEquals(2, count);

    array = new int[] {1, 2, 0, 1, 3};
    count = insertionSortCounter(array);
    log.info("count is {}", count);
    Assert.assertEquals(3, count);

    List<Integer> list = Arrays.asList(0, 1, 2, 0, 3, 4, 0, 2, 3, 2, 4);
    count = insertionSortCounter(list);
    log.info("count is {}", count);
    Assert.assertEquals(12, count);

    list = Arrays.asList(0, 1, 3, 1, 2);
    count = insertionSortCounter(list);
    log.info("count is {}", count);
    Assert.assertEquals(2, count);

    list = Arrays.asList(1, 2, 0, 1, 3);
    count = insertionSortCounter(list);
    log.info("count is {}", count);
    Assert.assertEquals(3, count);
  }

  private int insertionSortCounter(List<Integer> list) {
    int counter = 0;
    for (int i = 1; i < list.size(); i++) {
      int value = list.get(i); //array[i];
      int j = i - 1;
      while (j >= 0 && list.get(j) > value) {
        list.set(j + 1, list.get(j));
        counter++;
        j--;
      }
      list.set(j + 1, value);
    }
    return counter;
  }

  private int insertionSortCounter(int[] array) {
    int counter = 0;
    for (int i = 1; i < array.length; i++) {
      int value = array[i];
      int j = i - 1;
      while (j >= 0 && array[j] > value) {
        array[j + 1] = array[j];
        counter++;
        j--;
      }
      array[j + 1] = value;
    }
    return counter;
  }

  @Test
  public void testRemoveCycles() {
    // add some cycles
    graph.addEdge("20", "10");
    graph.addEdge("21", "11"); // connect from rank 0 to rank 3. should add 3, subtract 1
    graph.addEdge("20", "00");
    log.info("graph expanded to {}", graph);
    //     //add a couple of unconnected vertices
    graph.addVertex("loner1");
    graph.addVertex("loner2");

    RemoveCycles<String, Integer> removeCycles = new RemoveCycles(graph);
    Graph<String, Integer> dag = removeCycles.removeCycles();

    log.info("feedback arcs: {}", removeCycles.getFeedbackEdges());
    // remove the cycles i know i put in
    graph.removeEdge("20", "10");
    graph.removeEdge("21", "11");
    graph.removeEdge("20", "00");
    Assert.assertEquals(graph, dag);
  }
}
