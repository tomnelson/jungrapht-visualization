package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.util.InsertionSortCounter;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubEiglspergerSteps {

  private static final Logger log = LoggerFactory.getLogger(SubEiglspergerSteps.class);

  /**
   * for any pVertex that is in the list, take that pVertex's segment and append it to the any prior
   * Container in the list (creating the Container as needed), and do not append the PVertex in the
   * list to be returned. Finally, scan the list to join any sequential Containers into one and to
   * insert empty Containers between sequential vertices.
   *
   * @param biLayer
   * @param <V>
   * @return layerI modified so that PVertices are gone (added to previous containers)
   */
  public static <V, E> void stepOne(BiLayer<V, E> biLayer) {
    List<LV<V>> outList = new ArrayList<>();

    List<LV<V>> currentLayer = biLayer.currentLayer;

    for (LV<V> v : currentLayer) {
      // for each PVertex, add it to the empty or not empty list
      if (biLayer.joinVertexPredicate.test(v)) {
        SegmentVertex<V> segmentVertex = (SegmentVertex<V>) v;
        log.info("will add segment for {}", segmentVertex);
        if (outList.isEmpty()) {
          SubEiglspergerUtil.addSegmentVertexToEmptyList(outList, segmentVertex);
        } else {
          SubEiglspergerUtil.addSegmentVertexToNonEmptyList(outList, segmentVertex);
        }
      } else {
        outList.add(v);
      }
    }
    biLayer.currentLayer = SubEiglspergerUtil.scan(outList);
  }

  /**
   * @param biLayer layer for rank (i)
   * @param graph // * @param pos // * @param measure
   * @param <V>
   * @param <E>
   * @return a List for layerIplus1 representing the subsequent rank (i+1) not sorted yet
   */
  public static <V, E> void stepTwo(
      BiLayer<V, E> biLayer, List<VirtualEdge<V, E>> virtualEdges, Graph<LV<V>, LE<V, E>> graph) {

    // assign positions for all vertices and container in currentLayer
    List<LV<V>> currentLayer = biLayer.currentLayer;
    SubEiglspergerUtil.assignPositions(biLayer);

    // assign measures to all non-q vertices (for downwards, non-p vertices for upwards) in downstreamLayer
    List<LV<V>> downstreamLayer = biLayer.downstreamLayer;

    List<SubContainer<V, Segment<V>>> containersFromCurrentLayer =
        currentLayer
            .stream()
            .filter(v -> v instanceof SubContainer)
            .map(v -> (SubContainer<V, Segment<V>>) v)
            .filter(c -> c.size() > 0)
            .collect(Collectors.toList());
    containersFromCurrentLayer.stream().forEach(c -> virtualEdges.add(VirtualEdge.of(c, c)));

    // add to downstreamLayer, any currentLayer containers that are not already present
    //    for (Container<V> container : containersFromCurrentLayer) {
    //      int idx = Math.min(container.getIndex(), downstreamLayer.size());
    //      downstreamLayer.add(idx, container);
    //    }
    containersFromCurrentLayer
        .stream()
        .filter(c -> !downstreamLayer.contains(c))
        .forEach(downstreamLayer::add);

    log.info(
        "added these containers from rank {} to the downstream layer rank {}: {}",
        biLayer.currentRank,
        biLayer.downstreamRank,
        containersFromCurrentLayer);

    SubEiglspergerUtil.assignMeasures(biLayer, graph);
  }

  /**
   * @param biLayer unsorted List of LV (including non-empty? containers) // * @param pos //
   *     * @param measure
   * @param <V>
   * @return layerEyePlus1 reordered
   */
  public static <V, E> void stepThree(BiLayer<V, E> biLayer) {
    List<LV<V>> currentLayer = biLayer.currentLayer;
    List<LV<V>> downstreamLayer = biLayer.downstreamLayer;

    List<LV<V>> listV = new LinkedList<>();
    List<SubContainer<V, Segment<V>>> listS = new LinkedList<>();

    List<SegmentVertex<V>> segmentVertexList = new ArrayList<>();
    for (LV<V> v : downstreamLayer) {
      if (biLayer.splitVertexPredicate.test(v)) { // skip any QVertex for top to bottom
        segmentVertexList.add((SegmentVertex<V>) v);
        continue;
      } else if (v instanceof SubContainer) {
        SubContainer<V, Segment<V>> container = (SubContainer<V, Segment<V>>) v;
        //        if (container.size() > 0) {
        listS.add(container);
        //        }
      } else {
        listV.add(v);
      }
    }
    //    List<LV<V>> missingKeys =
    //        listV.stream().filter(v -> v.getMeasure() < 0).collect(Collectors.toList());
    //    if (missingKeys.size() > 0) {
    //      log.error("missing keys:{}", missingKeys);
    //    }
    // set the indices to the measures
    //    listS.forEach(s -> s.setIndex(biLayer.measure.get(s)));
    //    listV.forEach(v -> v.setIndex(biLayer.measure.get(v)));
    //    IntStream.range(0, listS.size()).forEach(i -> listS.get(i).setIndex(i));
    //    IntStream.range(0, listV.size()).forEach(i -> listV.get(i).setIndex(i));
    // sort the list by elements measures
    if (!biLayer.measure.keySet().containsAll(listS)) {
      log.error("something missing");
    }
    if (!biLayer.measure.keySet().containsAll(listV)) {
      log.error("something missing");
    }
    try {
      listS.sort(Comparator.comparingInt(biLayer.measure::get));
      listV.sort(Comparator.comparingInt(biLayer.measure::get));
    } catch (Exception ex) {
      log.info("biLayer.measure: {}, listS: {}, listV: {}", biLayer.measure, listS, listV);
    }
    /*
    if the measure of the head of the vertex list LV is <= position of the head of the container list LS,
    then pop the vertex from the vertex list and append it to the Li+1 list

    if the measure of the head of the vertex list is >= (position of the head of the container list + size of the head of the container list - 1)
    then pop the head of the container list and append it to the Li+1 list

    else
       pop S the first container and v the first vertex from their lists
       k = ceiling(measure(v)-pos(S))
       split S at k into S1, S2
       append S1 to the Li+1 list, append v to the L+1 list,
       set pos(S2) to be pos(S) + k,
       push S2 onto container list LS
     */
    List<LV<V>> mergedList = new ArrayList<>();
    if (listS.isEmpty() || listV.isEmpty()) {
      mergedList.addAll(listS);
      mergedList.addAll(listV);
    } else {
      while (!listV.isEmpty() && !listS.isEmpty()) {
        //        LV<V> got = listV.get(0);
        //        Container<V> contain = listS.get(0);
        //        int m = measure.get(got);
        //        int cm = pos.getOrDefault(contain, 0);
        LV<V> headV = listV.get(0);
        int headVMeasure = biLayer.measure.get(headV);
        SubContainer<V, Segment<V>> headS = listS.get(0);
        int headSPos = biLayer.pos.get(headS); //pos.get(headS);
        int headSSize = headS.size();
        if (biLayer.measure.get(listV.get(0)) <= biLayer.pos.get(listS.get(0))) {
          //        if (measure.get(listV.get(0)) <= pos.get(listS.get(0))) {
          LV<V> v = listV.remove(0);
          mergedList.add(v);
        } else if (biLayer.measure.get(listV.get(0))
            >= (biLayer.pos.get(listS.get(0)) + listS.get(0).size() - 1)) {
          //        } else if (measure.get(listV.get(0)) >= (pos.get(listS.get(0)) + listS.get(0).size() - 1)) {
          SubContainer<V, Segment<V>> container = listS.remove(0);
          mergedList.add(container);
        } else {
          SubContainer<V, Segment<V>> container = listS.remove(0);
          LV<V> v = listV.remove(0);
          int k = (int) Math.ceil(biLayer.measure.get(v) - biLayer.pos.get(container));
          log.info("will split {} at {}", container, k);
          Pair<SubContainer<V, Segment<V>>> containerPair = SubContainer.split(container, k);
          log.info("got {} and {}", containerPair.first, containerPair.second);
          mergedList.add(containerPair.first);
          mergedList.add(v);
          biLayer.pos.put(containerPair.second, biLayer.pos.get(container) + k);
          //          pos.put(containerPair.second, pos.get(container) + k);
          listS.add(0, containerPair.second);
        }
      }
      // add any leftovers to listPlusOne
      mergedList.addAll(listV);
      mergedList.addAll(listS);
    }
    mergedList.addAll(segmentVertexList);
    biLayer.downstreamLayer = mergedList;
    //    return EiglspergerUtil.scan(listPlusOne);
    //    return listPlusOne;
  }

  public static <V, E> void stepFour(BiLayer<V, E> biLayer, List<VirtualEdge<V, E>> virtualEdges) {

    List<LV<V>> currentLayer = biLayer.currentLayer;
    List<LV<V>> downstreamLayer = biLayer.downstreamLayer;
    // for each qVertex, get its Segment, find the segment in one of the containers in layerEye

    // gather the qVertices
    List<SegmentVertex<V>> qVertices =
        downstreamLayer
            .stream()
            .filter(v -> biLayer.splitVertexPredicate.test(v)) // QVertices
            .map(v -> (SegmentVertex<V>) v)
            .collect(Collectors.toList());

    for (SegmentVertex q : qVertices) {
      List<SubContainer<V, Segment<V>>> containerList =
          downstreamLayer
              .stream()
              .filter(v -> v instanceof SubContainer)
              .map(v -> (SubContainer<V, Segment<V>>) v)
              .collect(Collectors.toList());
      // find its container
      Segment<V> segment = q.getSegment();
      Optional<SubContainer<V, Segment<V>>> containerOpt =
          containerList.stream().filter(c -> c.contains(segment)).findFirst();
      if (containerOpt.isPresent()) {
        SubContainer<V, Segment<V>> container = containerOpt.get();
        int loserIdx = downstreamLayer.indexOf(container);

        log.info("splitting on {} because of {}", segment, q);
        Pair<SubContainer<V, Segment<V>>> containerPair = SubContainer.split(container, segment);

        log.info(
            "container pair is now {} and {}",
            containerPair.first.printTree("\n"),
            containerPair.second.printTree("\n"));

        //        Container<V> newContainer = container.split(segment);
        if (containerPair.first.size() > 0) {
          virtualEdges.add(VirtualEdge.of(container, containerPair.first));
        }
        if (containerPair.second.size() > 0) {
          virtualEdges.add(VirtualEdge.of(container, containerPair.second));
        }
        //        virtualEdges.add(VirtualEdge.of(container, newContainer));
        virtualEdges.add(VirtualEdge.of(container, q));
        virtualEdges.removeIf(e -> e.getSource() == container && e.getTarget() == container);
        downstreamLayer.remove(q);
        log.info("removed container {}", container.printTree("\n"));
        log.info("adding container {}", containerPair.first.printTree("\n"));
        log.info("adding container {}", containerPair.second.printTree("\n"));
        downstreamLayer.remove(container);
        downstreamLayer.add(loserIdx, containerPair.first);
        downstreamLayer.add(loserIdx + 1, q);
        downstreamLayer.add(loserIdx + 2, containerPair.second);
      } else {
        log.error(
            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!container opt was empty for segment {}", segment);
      }
    }

    IntStream.range(0, downstreamLayer.size()).forEach(i -> downstreamLayer.get(i).setIndex(i));

    //    for (LV<V> v : biLayer.downstreamArray) {
    //      if (biLayer.measure.containsKey(v) == false) {
    //        log.error("missing key");
    //      }
    //    }
    Arrays.sort(biLayer.downstreamArray, Comparator.comparingInt(LV::getIndex));
    //    log.info("downstream array: {}", Arrays.toString(biLayer.downstreamArray));

    //    return layerEyePlus1;
  }

  public static <V, E> void stepFive(
      Graph<LV<V>, LE<V, E>> graph,
      boolean forwards,
      BiLayer<V, E> biLayer,
      List<VirtualEdge<V, E>> virtualEdges) {
    if (forwards) {
      transposeDownwards(graph, biLayer, virtualEdges);
    } else {
      transposeUpwards(graph, biLayer, virtualEdges);
    }
  }

  private static <V, E> void transposeDownwards(
      Graph<LV<V>, LE<V, E>> graph, BiLayer<V, E> biLayer, List<VirtualEdge<V, E>> virtualEdges) {

    for (int j = 0; j < biLayer.downstreamLayer.size() - 1; j++) {
      List<LE<V, E>> biLayerEdges =
          graph
              .edgeSet()
              .stream()
              .filter(
                  e ->
                      biLayer.currentLayer.contains(graph.getEdgeSource(e))
                          && biLayer.downstreamLayer.contains(graph.getEdgeTarget(e)))
              .collect(Collectors.toList());
      biLayerEdges.addAll(virtualEdges);
      int vw = crossingCount(biLayerEdges);
      // count with j and j+1 swapped
      int wv = crossingCountSwapped(j, j + 1, biLayer.downstreamLayer, biLayerEdges);
      if (vw > wv) {
        swap(biLayer.downstreamLayer, j, j + 1);
      }
    }
  }

  private static <V, E> void transposeUpwards(
      Graph<LV<V>, LE<V, E>> graph, BiLayer<V, E> biLayer, List<VirtualEdge<V, E>> virtualEdges) {

    for (int j = 0; j < biLayer.downstreamLayer.size() - 1; j++) {
      List<LE<V, E>> biLayerEdges =
          graph
              .edgeSet()
              .stream()
              .filter(
                  e ->
                      biLayer.currentLayer.contains(graph.getEdgeTarget(e))
                          && biLayer.downstreamLayer.contains(graph.getEdgeSource(e)))
              .collect(Collectors.toList());
      biLayerEdges.addAll(virtualEdges);

      int vw = crossingCount(biLayerEdges);
      // count with j and j+1 swapped
      int wv = crossingCountSwapped(j, j + 1, biLayer.downstreamLayer, biLayerEdges);
      if (vw > wv) {
        swap(biLayer.downstreamLayer, j, j + 1);
      }
    }
  }

  //  private static <V, E> void transposeDownwards(
  //          LV<V>[][] ranks, Map<Integer, List<LE<V, E>>> reducedEdgeMap) {
  //    GraphLayers.checkLayers(ranks);
  //
  //    boolean improved = true;
  //    int sanityLimit = Integer.getInteger(PREFIX + "sugiyama.transpose.limit", 10);
  //    int sanityCheck = 0;
  //    while (improved) {
  //      improved = false;
  //      for (int i = 0; i < ranks.length; i++) {
  //        LV<V>[] rank = ranks[i];
  //        for (int j = 0; j < rank.length - 1; j++) {
  //          List<LE<V, E>> biLayerEdges =
  //                  reducedEdgeMap.getOrDefault(i, Collections.emptyList());
  //
  //          int vw = crossingCount(biLayerEdges);
  //          // count with j and j+1 swapped
  //          int wv = crossingCountSwapped(j, j + 1, rank, biLayerEdges);
  //          if (vw > wv) {
  //            improved = true;
  //            swap(rank, j, j + 1);
  //          }
  //        }
  //      }
  //      sanityCheck++;
  //      if (sanityCheck > sanityLimit) {
  //        break;
  //      }
  //    }
  //    GraphLayers.checkLayers(ranks);
  //  }

  //  private static <V, E> void transposeUpwards(
  //          LV<V>[][] ranks, Map<Integer, List<LE<V, E>>> reducedEdgeMap) {
  //    GraphLayers.checkLayers(ranks);
  //
  //    boolean improved = true;
  //    int sanityLimit = Integer.getInteger(PREFIX + "sugiyama.transpose.limit", 10);
  //    int sanityCheck = 0;
  //    while (improved) {
  //      improved = false;
  //      for (int i = ranks.length - 1; i >= 0; i--) {
  //        LV<V>[] rank = ranks[i];
  //        for (int j = 0; j < rank.length - 1; j++) {
  //          List<LE<V, E>> biLayerEdges =
  //                  reducedEdgeMap.getOrDefault(i, Collections.emptyList());
  //
  //          int vw = crossingCount(biLayerEdges);
  //          // count with j and j+1 swapped
  //          int wv = crossingCountSwapped(j, j + 1, rank, biLayerEdges);
  //          if (vw > wv) {
  //            improved = true;
  //            swap(rank, j, j + 1);
  //          }
  //        }
  //      }
  //      sanityCheck++;
  //      if (sanityCheck > sanityLimit) {
  //        break;
  //      }
  //    }
  //    GraphLayers.checkLayers(ranks);
  //  }

  private static <V, E> int crossingCount(List<LE<V, E>> edges) {
    Comparator<LE<V, E>> sourceIndexComparator =
        Comparator.comparingInt(e -> e.getSource().getIndex());
    Comparator<LE<V, E>> targetIndexComparator =
        Comparator.comparingInt(e -> e.getTarget().getIndex());
    Comparator<LE<V, E>> biLevelEdgeComparator =
        sourceIndexComparator.thenComparing(targetIndexComparator);
    edges.sort(biLevelEdgeComparator);
    List<Integer> targetIndices = new ArrayList<>();
    int weight = 1;
    for (LE<V, E> edge : edges) {
      //      if (edge.getTarget() instanceof Container) {
      //        weight += ((Container<V>)edge.getTarget()).size();
      //      }
      targetIndices.add(edge.getTarget().getIndex());
    }

    return weight * InsertionSortCounter.insertionSortCounter(targetIndices);
  }

  private static <V, E> int crossingCountSwapped(
      int i, int j, List<LV<V>> layer, List<LE<V, E>> edges) {
    Comparator<LE<V, E>> sourceIndexComparator =
        Comparator.comparingInt(e -> e.getSource().getIndex());
    Comparator<LE<V, E>> targetIndexComparator =
        Comparator.comparingInt(e -> e.getTarget().getIndex());
    Comparator<LE<V, E>> biLevelEdgeComparator =
        sourceIndexComparator.thenComparing(targetIndexComparator);
    swap(layer, i, j);
    edges.sort(biLevelEdgeComparator);
    List<Integer> targetIndices = new ArrayList<>();
    int weight = 1;
    for (LE<V, E> edge : edges) {
      targetIndices.add(edge.getTarget().getIndex());
      //      if (edge.getTarget() instanceof Container) {
      //        weight += ((Container<V>)edge.getTarget()).size();
      //      }
    }
    swap(layer, i, j);
    return weight * InsertionSortCounter.insertionSortCounter(targetIndices);
  }

  private static <V> void swap(LV<V>[] array, int i, int j) {
    LV<V> temp = array[i];
    array[i] = array[j];
    array[j] = temp;
    array[i].setIndex(i);
    array[j].setIndex(j);
  }

  private static <V> void swap(List<LV<V>> array, int i, int j) {
    Collections.swap(array, i, j);
    array.get(i).setIndex(i);
    array.get(j).setIndex(j);
  }

  public static <V, E> void stepSix(BiLayer<V, E> biLayer) {

    biLayer.downstreamLayer = SubEiglspergerUtil.scan(biLayer.downstreamLayer);
  }

  /**
   * return the segment to which v is incident, if v is a PVertex or a QVertex. Otherwise, return v
   *
   * @param v
   * @param <V>
   * @return
   */
  static <V> LV<V> s(LV<V> v) {
    if (v instanceof SegmentVertex) {
      SegmentVertex<V> pVertex = (SegmentVertex<V>) v;
      return pVertex.getSegment();
    } else {
      return v;
    }
  }
}
