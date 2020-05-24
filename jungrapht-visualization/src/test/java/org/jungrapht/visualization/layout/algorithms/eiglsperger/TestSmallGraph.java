package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GraphLayers;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GreedyCycleRemoval;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.TransformedGraphSupplier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSmallGraph {

  private Graph<Integer, Integer> graph;
  private Graph<LV<Integer>, LE<Integer, Integer>> svGraph;
  private NeighborCache<LV<Integer>, LE<Integer, Integer>> neighborCache;

  @Before
  public void setup() {
    graph = createInitialGraph();
  }

  @Test
  public void testEiglsperger() {
    TransformedGraphSupplier<Integer, Integer> transformedGraphSupplier =
        new TransformedGraphSupplier<>(graph);
    this.svGraph = transformedGraphSupplier.get();
    neighborCache = new NeighborCache<>(svGraph);
    GreedyCycleRemoval<LV<Integer>, LE<Integer, Integer>> greedyCycleRemoval =
        new GreedyCycleRemoval<>(svGraph);
    Collection<LE<Integer, Integer>> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();

    // should be no feedback arcs in this graph
    Assert.assertEquals(0, feedbackArcs.size());

    List<List<LV<Integer>>> layers = GraphLayers.assign(svGraph);
    Assert.assertEquals(5, layers.size());

    Synthetics<Integer, Integer> synthetics = new Synthetics<>(svGraph);
    List<LE<Integer, Integer>> edges = new ArrayList<>(svGraph.edgeSet());
    LV<Integer>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);

    checkLayersArray(layersArray);

    EiglspergerStepsForward<Integer, Integer> stepsForward =
        new EiglspergerStepsForward<>(svGraph, neighborCache, layersArray, true);
    EiglspergerStepsBackward<Integer, Integer> stepsBackward =
        new EiglspergerStepsBackward<>(svGraph, neighborCache, layersArray, true);

    int forwardCrossCount = stepsForward.sweep(layersArray);
    //        int backwardCrossCount = stepsBackward.sweep(layersArray);

    //        Graph<LV<Integer>, Integer> compactionGraph =
    //                stepsForward.compactionGraph;
    HorizontalCoordinateAssignmentDeprecated<Integer, Integer> horizontalCoordinateAssignment =
        new HorizontalCoordinateAssignmentDeprecated<>(
            layersArray, svGraph, new HashSet<>(), 100, 100);
    horizontalCoordinateAssignment.horizontalCoordinateAssignment();
  }

  @Test
  public void testEiglspergerWithGraph() {
    TransformedGraphSupplier<Integer, Integer> transformedGraphSupplier =
        new TransformedGraphSupplier<>(graph);
    this.svGraph = transformedGraphSupplier.get();
    neighborCache = new NeighborCache<>(svGraph);
    GreedyCycleRemoval<LV<Integer>, LE<Integer, Integer>> greedyCycleRemoval =
        new GreedyCycleRemoval<>(svGraph);
    Collection<LE<Integer, Integer>> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();

    // should be no feedback arcs in this graph
    Assert.assertEquals(0, feedbackArcs.size());

    List<List<LV<Integer>>> layers = GraphLayers.assign(svGraph);
    Assert.assertEquals(5, layers.size());

    Synthetics<Integer, Integer> synthetics = new Synthetics<>(svGraph);
    List<LE<Integer, Integer>> edges = new ArrayList<>(svGraph.edgeSet());
    LV<Integer>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);

    checkLayersArray(layersArray);

    EiglspergerStepsForward<Integer, Integer> stepsForward =
        new EiglspergerStepsForward<>(svGraph, neighborCache, layersArray, true);
    //        EiglspergerStepsBackward<Integer, Integer> stepsBackward = new EiglspergerStepsBackward<>(svGraph, layersArray, true);

    int forwardCrossCount = stepsForward.sweep(layersArray);
    //        int backwardCrossCount = stepsBackward.sweep(layersArray);

    Graph<LV<Integer>, Integer> compactionGraph = stepsForward.compactionGraph;
    HorizontalCoordinateAssignment<Integer, Integer> horizontalCoordinateAssignment =
        new HorizontalCoordinateAssignment<>(
            layersArray, svGraph, compactionGraph, new HashSet<>(), 100, 100);
    horizontalCoordinateAssignment.horizontalCoordinateAssignment();
  }

  private void checkLayersArray(LV<Integer>[][] layersArray) {
    Assert.assertEquals(5, layersArray.length);
    LV<Integer>[] layer = layersArray[0];
    Assert.assertEquals(1, layer.length);
    Assert.assertTrue(layer[0] instanceof LV);
    Assert.assertEquals(0, (int) layer[0].getVertex());

    layer = layersArray[1];
    Assert.assertEquals(2, layer.length);
    Assert.assertTrue(layer[0] instanceof LV);
    Assert.assertEquals(1, (int) layer[0].getVertex());
    Assert.assertTrue(layer[1] instanceof PVertex);

    layer = layersArray[2];
    Assert.assertEquals(3, layer.length);
    Assert.assertTrue(layer[0] instanceof LV);
    Assert.assertEquals(2, (int) layer[0].getVertex());
    Assert.assertTrue(layer[1] instanceof SyntheticLV);
    Assert.assertTrue(layer[2] instanceof PVertex);

    layer = layersArray[3];
    Assert.assertEquals(4, layer.length);
    Assert.assertTrue(layer[0] instanceof LV);
    Assert.assertEquals(3, (int) layer[0].getVertex());
    Assert.assertTrue(layer[1] instanceof QVertex);
    Assert.assertTrue(layer[2] instanceof QVertex);
    Assert.assertTrue(layer[3] instanceof SyntheticLV);

    layer = layersArray[4];
    Assert.assertEquals(1, layer.length);
    Assert.assertTrue(layer[0] instanceof LV);
    Assert.assertEquals(4, (int) layer[0].getVertex());
  }

  private Graph<Integer, Integer> createInitialGraph() {

    Graph<Integer, Integer> graph =
        GraphTypeBuilder.<Integer, Integer>directed()
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .vertexSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    IntStream.rangeClosed(0, 4).forEach(graph::addVertex);
    graph.addEdge(0, 1);
    graph.addEdge(1, 2);
    graph.addEdge(2, 3);
    graph.addEdge(3, 4);
    graph.addEdge(0, 4);
    graph.addEdge(2, 4);
    graph.addEdge(1, 3);
    graph.addEdge(1, 4);

    return graph;
  }
}
/*
values without compactionGraph:
layersarray:

0 = {LV[1]@2388}
 0 = {LVI@2394} "LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}"
1 = {LV[2]@2389}
 0 = {LVI@2369} "LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}"
 1 = {PVertex@2396} "PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}"
2 = {LV[3]@2390}
 0 = {LVI@2373} "LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}"
 1 = {SyntheticLV@2374} "SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null}"
 2 = {PVertex@2399} "PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}"
3 = {LV[4]@2391}
 0 = {SyntheticLV@2382} "SyntheticLV{vertex=990355670, rank=3, index=0, pos=0, measure=0.0, p=null}"
 1 = {LVI@2383} "LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}"
 2 = {QVertex@2403} "QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}"
 3 = {QVertex@2404} "QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}"
4 = {LV[1]@2392}
 0 = {LVI@2409} "LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}"

compactionGraph:


   value = {IntrusiveEdge@2358}
    source = {LVI@2369} "LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}"
    target = {Segment@2370} "Segment{1-to-3}"
           pVertex = {PVertex@2396} "PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}"
           qVertex = {QVertex@2404} "QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}"
   value = {IntrusiveEdge@2360}
    source = {LVI@2373} "LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}"
    target = {SyntheticLV@2374} "SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null}"
   value = {IntrusiveEdge@2362}
    source = {SyntheticLV@2374} "SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null}"
    target = {Segment@2377} "Segment{2-to-3}"
             pVertex = {PVertex@2399} "PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}"
             qVertex = {QVertex@2403} "QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}"
   value = {IntrusiveEdge@2364}
    source = {Segment@2377} "Segment{2-to-3}"
             pVertex = {PVertex@2399} "PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}"
             qVertex = {QVertex@2403} "QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}"
    target = {Segment@2370} "Segment{1-to-3}"
             pVertex = {PVertex@2396} "PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}"
             qVertex = {QVertex@2404} "QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}"
   value = {IntrusiveEdge@2366}
    source = {SyntheticLV@2382} "SyntheticLV{vertex=990355670, rank=3, index=0, pos=0, measure=0.0, p=null}"
    target = {LVI@2383} "LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}"
   value = {IntrusiveEdge@2368}
    source = {LVI@2383} "LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}"
    target = {Segment@2377} "Segment{2-to-3}"
             pVertex = {PVertex@2399} "PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}"
             qVertex = {QVertex@2403} "QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}"


 - upLeft
 - alignMap:{
 LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=
   LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null},
 QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=
   PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null},
 LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=
   SyntheticLV{vertex=990355670, rank=3, index=0, pos=0, measure=0.0, p=null},
 SyntheticLV{vertex=990355670, rank=3, index=0, pos=0, measure=0.0, p=null}=
   LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
 LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=
   LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null},
 LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=
   SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null},
 SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null}=
   LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null},
 PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=
   QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null},
 PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=
   QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null},
 QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=
   PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null},
 LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=
   LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}}

 - rootMap:{
 LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=
   LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
 QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=
   PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null},
 LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=
   LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
 SyntheticLV{vertex=990355670, rank=3, index=0, pos=0, measure=0.0, p=null}=
   LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
 LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=
   SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null},
 LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=
   SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null},
 SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null}=
   SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null},
 PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=
   PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null},
 PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=
   PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null},
 QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=
   PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null},
 LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=
   LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}

 - shift:{
 LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=   2147483647,
 QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=   2147483647,
 LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=   2147483647,
 SyntheticLV{vertex=990355670, rank=3, index=0, pos=0, measure=0.0, p=null}=   2147483647,
 LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=   2147483647,
 LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=   2147483647,
 SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null}=   2147483647,
 PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=   2147483647,
 PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=   2147483647,
 QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=   2147483647,
 LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=   2147483647}

 - sink:{
 LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=
    LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null},
 QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=
    QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null},
 LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=
    LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null},
 SyntheticLV{vertex=990355670, rank=3, index=0, pos=0, measure=0.0, p=null}=
    SyntheticLV{vertex=990355670, rank=3, index=0, pos=0, measure=0.0, p=null},
 LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=
    LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null},
 LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=
    LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null},
 SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null}=
    LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
 PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=
    LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
 PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=
    LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
 QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=
    QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null},
 LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=
    LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}


x = {HashMap@2375}  size = 11
 {LVI@2365} "LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}" -> {Integer@2549} 0
 {QVertex@2400} "QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}" -> {Integer@2550} 300
 {LVI@2402} "LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}" -> {Integer@2549} 0
 {SyntheticLV@2403} "SyntheticLV{vertex=990355670, rank=3, index=0, pos=0, measure=0.0, p=null}" -> {Integer@2549} 0
 {LVI@2404} "LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}" -> {Integer@2551} 100
 {LVI@2406} "LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}" -> {Integer@2551} 100
 {SyntheticLV@2405} "SyntheticLV{vertex=2028555727, rank=2, index=1, pos=1, measure=0.0, p=null}" -> {Integer@2551} 100
 {PVertex@2401} "PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}" -> {Integer@2552} 300
 {PVertex@2407} "PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}" -> {Integer@2553} 200
 {QVertex@2408} "QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}" -> {Integer@2554} 200
 {LVI@2364} "LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}" -> {Integer@2549} 0

target = {Segment@2370} "Segment{1-to-3}"
 pVertex = {PVertex@2396} "PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}"
 qVertex = {QVertex@2404} "QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}"
target = {Segment@2377} "Segment{2-to-3}"
 pVertex = {PVertex@2399} "PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}"
 qVertex = {QVertex@2403} "QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}"
source = {Segment@2377} "Segment{2-to-3}"
 pVertex = {PVertex@2399} "PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}"
 qVertex = {QVertex@2403} "QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}"
target = {Segment@2370} "Segment{1-to-3}"
 pVertex = {PVertex@2396} "PVertex{vertex=2147046752, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}"
 qVertex = {QVertex@2404} "QVertex{vertex=715378067, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}"
target = {Segment@2377} "Segment{2-to-3}"
 pVertex = {PVertex@2399} "PVertex{vertex=247944893, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}"
 qVertex = {QVertex@2403} "QVertex{vertex=1014166943, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}"

 */

/*

   values with compaction graph

    - sweepForward crossCount:0
    - upLeft
    - alignMap:{
    LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=
       LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null},
    SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=
       LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
    LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=
       SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null},
    SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=
       LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null},
    LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=
       LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null},
    QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=
       PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null},
    LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=
       SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null},
    QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=
       PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null},
    PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=
       PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null},
    Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3},
    PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=
       PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null},
    LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=
       LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}}

    - rootMap:{
    LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=
       LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
    SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=
       LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
     LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=
       LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
     SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=
       SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null},
     LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=
       SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null},
     QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=
       PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null},
     LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=
       SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null},
     QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=
       PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null},
     PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=
       PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null},
     Segment{1-to-3}=Segment{1-to-3},
     Segment{2-to-3}=Segment{2-to-3},
     PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=
       PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null},
     LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=
       LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}

    - shift:{
    LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=2147483647,
    SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=2147483647,
    LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=2147483647,
    SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=2147483647,
    LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=2147483647,
    QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=2147483647,
    LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=2147483647,
    QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=2147483647,
    PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=2147483647,
    PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=2147483647,
    LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=2147483647}

    - sink:{
    LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null},
    SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null},
    LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null},
    SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
    LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null},
    QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null},
    LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null},
    QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null},
    PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
    PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
    LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}





    */

/*
         without graph

         08:54:56.906 [main] INFO  o.j.v.l.a.e.EiglspergerStepsForward - sweepForward crossCount:0
         08:54:56.914 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock for all v where root(v) is v
         08:54:56.915 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}
         08:54:56.915 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}
         08:54:56.915 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null} is PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}
         08:54:56.915 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}
         08:54:56.916 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} is SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}
         08:54:56.916 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null} is PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}
         08:54:56.916 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}
         08:54:56.916 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}
         08:54:56.916 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null} is QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}
         08:54:56.916 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null} is QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}
         08:54:56.916 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}
         08:54:56.918 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} and x.containsKey(v) is false
         08:54:56.918 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
         08:54:56.918 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} and x.containsKey(v) is true
         08:54:56.918 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null} and x.containsKey(v) is false
         08:54:56.918 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
         08:54:56.918 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} and x.containsKey(v) is true
         08:54:56.919 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} and x.containsKey(v) is true
         08:54:56.919 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.919 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.919 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.919 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - upLeft
         08:54:56.919 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - alignMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}}
         08:54:56.920 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - rootMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
         08:54:56.921 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - shift:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=2147483647, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=2147483647, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=2147483647, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=2147483647, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=2147483647, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=2147483647, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=2147483647, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=2147483647, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=2147483647}
         08:54:56.921 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - sink:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
         08:54:56.923 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock for all v where root(v) is v
         08:54:56.923 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}
         08:54:56.924 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} is LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}
         08:54:56.924 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}
         08:54:56.924 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} is LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}
         08:54:56.924 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} is SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}
         08:54:56.924 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} is PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}
         08:54:56.924 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} is SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}
         08:54:56.924 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}
         08:54:56.924 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} is QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}
         08:54:56.924 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}
         08:54:56.925 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} is LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}
         08:54:56.925 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} and x.containsKey(v) is false
         08:54:56.925 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is false
         08:54:56.925 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
         08:54:56.925 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is false
         08:54:56.925 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.925 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.925 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.925 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.926 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.926 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.926 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - upRight
         08:54:56.926 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - alignMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}}
         08:54:56.926 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - rootMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
         08:54:56.927 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - shift:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=2147483647, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=2147483647, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=2147483647, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=2147483647, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=2147483647, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=2147483647, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=2147483647, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=2147483647, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=2147483647}
         08:54:56.927 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - sink:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}}
         08:54:56.929 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock for all v where root(v) is v
         08:54:56.929 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}
         08:54:56.929 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}
         08:54:56.929 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null} is PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}
         08:54:56.929 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}
         08:54:56.930 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}
         08:54:56.930 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} is PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}
         08:54:56.930 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}
         08:54:56.930 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}
         08:54:56.931 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} is QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}
         08:54:56.931 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null} is QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}
         08:54:56.931 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}
         08:54:56.931 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
         08:54:56.931 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} and x.containsKey(v) is false
         08:54:56.931 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} and x.containsKey(v) is true
         08:54:56.931 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} and x.containsKey(v) is false
         08:54:56.933 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.934 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.935 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null} and x.containsKey(v) is false
         08:54:56.935 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} and x.containsKey(v) is true
         08:54:56.936 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.936 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} and x.containsKey(v) is true
         08:54:56.937 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - downLeft
         08:54:56.937 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - alignMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}}
         08:54:56.940 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - rootMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}}
         08:54:56.940 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - shift:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=2147483647, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=2147483647, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=2147483647, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=2147483647, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=2147483647, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=2147483647, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=2147483647, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=2147483647, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=2147483647}
         08:54:56.940 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - sink:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
         08:54:56.942 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock for all v where root(v) is v
         08:54:56.942 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}
         08:54:56.943 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}
         08:54:56.943 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}
         08:54:56.943 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null} is LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}
         08:54:56.943 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}
         08:54:56.943 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} is PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}
         08:54:56.943 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null} is SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}
         08:54:56.943 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}
         08:54:56.943 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} is QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}
         08:54:56.943 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is false
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} and x.containsKey(v) is false
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} and x.containsKey(v) is false
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} and x.containsKey(v) is true
         08:54:56.944 [main] INFO  o.j.v.l.a.e.HorizontalCompaction - placeBlock: LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is true
         08:54:56.945 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - downRight
         08:54:56.945 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - alignMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}}
         08:54:56.945 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - rootMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}}
         08:54:56.945 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - shift:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=2147483647, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=2147483647, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=2147483647, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=2147483647, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=2147483647, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=2147483647, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=2147483647, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=2147483647, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=2147483647}
         08:54:56.945 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignment - sink:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}


         with graph

         08:56:28.545 [main] INFO  o.j.v.l.a.e.EiglspergerStepsForward - sweepForward crossCount:0
         08:56:28.558 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}
         08:56:28.559 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}
         08:56:28.559 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null} is PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}
         08:56:28.559 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}
         08:56:28.559 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} is SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}
         08:56:28.560 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null} is PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}
         08:56:28.560 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}
         08:56:28.561 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}
         08:56:28.561 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null} is QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}
         08:56:28.561 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null} is QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}
         08:56:28.562 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}
         08:56:28.562 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - compactionGraph vertices: [
         LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null},
         LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null},
         Segment{1-to-3},
         LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null},
         SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null},
         Segment{2-to-3},
         SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null},
         LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}]

         08:56:28.564 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - verticesInCompactionGraphAndSegmentEnds = [
         SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null},
         PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null},
         PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null},
         LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}]
         08:56:28.564 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} and x.containsKey(v) is false
         08:56:28.564 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
         08:56:28.565 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
         08:56:28.565 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null} and x.containsKey(v) is false
         08:56:28.565 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - upLeft
         08:56:28.565 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - alignMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}}
         08:56:28.566 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - rootMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
         08:56:28.568 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - shift:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=2147483647, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=2147483647, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=2147483647, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=2147483647, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=2147483647, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=2147483647, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=2147483647, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=2147483647, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=2147483647}
         08:56:28.568 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - sink:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
         08:56:28.571 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}
         08:56:28.571 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} is LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}
         08:56:28.572 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}
         08:56:28.572 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} is LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}
         08:56:28.572 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} is SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}
         08:56:28.572 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} is PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}
         08:56:28.572 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} is SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}
         08:56:28.573 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}
         08:56:28.573 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} is QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}
         08:56:28.573 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} is QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}
         08:56:28.574 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} is LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}
         08:56:28.574 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - compactionGraph vertices: [LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, Segment{1-to-3}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, Segment{2-to-3}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}]
         08:56:28.574 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - verticesInCompactionGraphAndSegmentEnds = [LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}]
         08:56:28.574 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} and x.containsKey(v) is false
         08:56:28.576 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is false
         08:56:28.576 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is false
         08:56:28.576 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
         08:56:28.577 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - upRight
         08:56:28.577 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - alignMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}}
         08:56:28.580 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - rootMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
         08:56:28.580 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - shift:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=2147483647, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=2147483647, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=2147483647, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=2147483647, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=2147483647, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=2147483647, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=2147483647, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=2147483647, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=2147483647}
         08:56:28.580 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - sink:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
         08:56:28.583 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}
         08:56:28.583 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}
         08:56:28.583 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null} is PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}
         08:56:28.583 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}
         08:56:28.584 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}
         08:56:28.584 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} is PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}
         08:56:28.584 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}
         08:56:28.584 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}
         08:56:28.584 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} is QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}
         08:56:28.584 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null} is QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}
         08:56:28.584 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}
         08:56:28.584 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - compactionGraph vertices: [LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, Segment{1-to-3}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, Segment{2-to-3}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}]
         08:56:28.585 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - verticesInCompactionGraphAndSegmentEnds = [QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}]
         08:56:28.585 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null} and x.containsKey(v) is false
         08:56:28.585 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} and x.containsKey(v) is false
         08:56:28.585 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
         08:56:28.585 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} and x.containsKey(v) is false
         08:56:28.585 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - downLeft
         08:56:28.585 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - alignMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}}
         08:56:28.586 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - rootMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}}
         08:56:28.586 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - shift:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=2147483647, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=2147483647, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=2147483647, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=2147483647, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=2147483647, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=2147483647, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=2147483647, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=2147483647, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=2147483647}
         08:56:28.586 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - sink:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
         08:56:28.589 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}
         08:56:28.589 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}
         08:56:28.589 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}
         08:56:28.589 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null} is LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}
         08:56:28.589 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}
         08:56:28.590 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} is PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}
         08:56:28.590 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null} is SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}
         08:56:28.590 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} is LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}
         08:56:28.590 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} is QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}
         08:56:28.590 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}
         08:56:28.590 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - root(v) == LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} is LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}
         08:56:28.590 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - compactionGraph vertices: [LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, Segment{1-to-3}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, Segment{2-to-3}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}]
         08:56:28.590 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - verticesInCompactionGraphAndSegmentEnds = [SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}]
         08:56:28.590 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} and x.containsKey(v) is false
         08:56:28.591 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is false
         08:56:28.591 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
         08:56:28.591 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} and x.containsKey(v) is false
         08:56:28.591 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - downRight
         08:56:28.591 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - alignMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}}
         08:56:28.591 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - rootMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}}
         08:56:28.592 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - shift:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=2147483647, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=2147483647, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=2147483647, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=2147483647, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=2147483647, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=2147483647, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=2147483647, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=2147483647, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=2147483647}
         08:56:28.592 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - sink:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}


          */

/*
                     no graph put x outputs

                      */

/*
                                             with graph put x outputs
                                             08:12:47.420 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - upLeft
                                             08:12:47.420 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - alignMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}}
                                             08:12:47.420 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - rootMap:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, Segment{1-to-3}=Segment{1-to-3}, Segment{2-to-3}=Segment{2-to-3}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
                                             08:12:47.421 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - shift:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=2147483647, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=2147483647, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=2147483647, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=2147483647, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=2147483647, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=2147483647, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=2147483647, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=2147483647, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=2147483647, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=2147483647}
                                             08:12:47.421 [main] INFO  o.j.v.l.a.e.HorizontalCoordinateAssignmentWithGraph - sink:{LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}=SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}=LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}=SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}=LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}=QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}=LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}=QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}=PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}=PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}=LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}}
                                             08:12:47.424 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, root(v):LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} equal: true
                                             08:12:47.424 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, root(v):LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} equal: true
                                             08:12:47.424 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null}, root(v):LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} equal: false
                                             08:12:47.424 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, root(v):LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} equal: true
                                             08:12:47.424 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, root(v):SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} equal: true
                                             08:12:47.424 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null}, root(v):LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} equal: false
                                             08:12:47.425 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, root(v):LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} equal: false
                                             08:12:47.425 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, root(v):SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} equal: false
                                             08:12:47.425 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null}, root(v):LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} equal: false
                                             08:12:47.425 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null}, root(v):LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} equal: false
                                             08:12:47.425 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - v:LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}, root(v):LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} equal: false
                                             08:12:47.425 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - compactionGraph vertices: [LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}, LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, Segment{1-to-3}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, Segment{2-to-3}, SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null}]
                                             08:12:47.425 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - verticesInCompactionGraphAndSegmentEnds = [LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null}, LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null}, SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null}, LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null}]
                                             08:12:47.426 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} and x.containsKey(v) is false
                                             08:12:47.426 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} at x: 0
                                             08:12:47.426 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is false
                                             08:12:47.426 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} at x: 0
                                             08:12:47.426 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} and x.containsKey(v) is false
                                             08:12:47.426 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} at x: 0
                                             08:12:47.426 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - placeBlock: SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} and x.containsKey(v) is false
                                             08:12:47.426 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} at x: 0
                                             08:12:47.426 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put LVI{vertex=0, rank=0, index=0, pos=0, measure=-1.0, p=null} at x: 0
                                             08:12:47.427 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put LVI{vertex=1, rank=1, index=0, pos=0, measure=0.0, p=null} at x: 0
                                             08:12:47.427 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put PVertex{vertex=898406901, segment=Segment{1-to-3}, rank=1, index=1, pos=1, measure=0.0, p=null} at x: 0
                                             08:12:47.427 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put LVI{vertex=2, rank=2, index=0, pos=0, measure=0.0, p=null} at x: 0
                                             08:12:47.427 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put SyntheticLV{vertex=266272063, rank=2, index=1, pos=1, measure=0.0, p=null} at x: 0
                                             08:12:47.427 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put PVertex{vertex=299644693, segment=Segment{2-to-3}, rank=2, index=2, pos=2, measure=0.0, p=null} at x: 0
                                             08:12:47.427 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put SyntheticLV{vertex=341878976, rank=3, index=0, pos=0, measure=0.0, p=null} at x: 0
                                             08:12:47.427 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put LVI{vertex=3, rank=3, index=1, pos=1, measure=0.0, p=null} at x: 0
                                             08:12:47.427 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put QVertex{vertex=1771243284, segment=Segment{2-to-3}, rank=3, index=2, pos=2, measure=-1.0, p=null} at x: 0
                                             08:12:47.427 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put QVertex{vertex=1213349904, segment=Segment{1-to-3}, rank=3, index=3, pos=3, measure=-1.0, p=null} at x: 0
                                             08:12:47.427 [main] INFO  o.j.v.l.a.e.HorizontalCompactionWithGraph - put LVI{vertex=4, rank=4, index=0, pos=0, measure=1.0, p=null} at x: 0
                                             0
                                              */
