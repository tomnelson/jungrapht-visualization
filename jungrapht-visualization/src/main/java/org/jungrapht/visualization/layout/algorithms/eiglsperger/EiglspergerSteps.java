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
import org.jungrapht.visualization.layout.algorithms.util.InsertionSortCounter;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EiglspergerSteps {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerSteps.class);

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
        if (log.isTraceEnabled()) log.trace("will add segment for {}", segmentVertex);
        if (outList.isEmpty()) {
          EiglspergerUtil.addSegmentVertexToEmptyList(outList, segmentVertex);
        } else {
          EiglspergerUtil.addSegmentVertexToNonEmptyList(outList, segmentVertex);
        }
      } else {
        outList.add(v);
      }
    }
    biLayer.currentLayer = EiglspergerUtil.scan(outList);
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
    EiglspergerUtil.assignPositions(biLayer);

    // assign measures to all non-q vertices (for downwards, non-p vertices for upwards) in downstreamLayer
    List<LV<V>> downstreamLayer = biLayer.downstreamLayer;

    List<Container<V, Segment<V>>> containersFromCurrentLayer =
        currentLayer
            .stream()
            .filter(v -> v instanceof Container)
            .map(v -> (Container<V, Segment<V>>) v)
            .filter(c -> c.size() > 0)
            .collect(Collectors.toList());
    containersFromCurrentLayer.stream().forEach(c -> virtualEdges.add(VirtualEdge.of(c, c)));

    // add to downstreamLayer, any currentLayer containers that are not already present
    containersFromCurrentLayer
        .stream()
        .filter(c -> !downstreamLayer.contains(c))
        .forEach(downstreamLayer::add);

    if (log.isTraceEnabled())
      log.trace(
          "added these containers from rank {} to the downstream layer rank {}: {}",
          biLayer.currentRank,
          biLayer.downstreamRank,
          containersFromCurrentLayer);

    EiglspergerUtil.assignMeasures(biLayer, graph);
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
    List<Container<V, Segment<V>>> listS = new LinkedList<>();

    List<SegmentVertex<V>> segmentVertexList = new ArrayList<>();
    for (LV<V> v : downstreamLayer) {
      if (biLayer.splitVertexPredicate.test(v)) { // skip any QVertex for top to bottom
        segmentVertexList.add((SegmentVertex<V>) v);
        continue;
      } else if (v instanceof Container) {
        Container<V, Segment<V>> container = (Container<V, Segment<V>>) v;
        if (container.size() > 0) {
          listS.add(container);
        }
      } else {
        listV.add(v);
      }
    }
    // sort the list by elements measures
    if (log.isTraceEnabled()) {
      log.trace("listS measures: {}", listS);
    }
    if (listS.size() > 0
        && listS.stream().mapToDouble(s -> s.getMeasure()).min().getAsDouble() < 0) {
      log.error("something missing");
    }
    if (log.isTraceEnabled()) {
      log.trace("listV measures: {}", listV);
    }
    if (listV.size() > 0
        && listV.stream().mapToDouble(s -> s.getMeasure()).min().getAsDouble() < 0) {
      log.error("something missing");
    }
    try {
      listS.sort(Comparator.comparingDouble(Container::getMeasure));
      listV.sort(Comparator.comparingDouble(LV::getMeasure));

      if (log.isTraceEnabled()) {
        StringBuilder sbuilder = new StringBuilder("S3 listS:\n");
        listS.forEach(s -> sbuilder.append(s.toString()).append("\n"));
        log.trace(sbuilder.toString());
        StringBuilder vbuilder = new StringBuilder("S3 listV:\n");
        listV.forEach(v -> vbuilder.append(v.toString()).append("\n"));
        log.trace(vbuilder.toString());
      }
    } catch (Exception ex) {
      log.error("listS: {}, listV: {} exception: {}", listS, listV, ex);
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
      mergedList.sort(Comparator.comparingDouble(LV::getMeasure));
    } else {
      while (!listV.isEmpty() && !listS.isEmpty()) {
        LV<V> headV = listV.get(0);
        Container<V, Segment<V>> headS = listS.get(0);
        if (listV.get(0).getMeasure() <= listS.get(0).getPos()) {
          LV<V> v = listV.remove(0);
          mergedList.add(v);
        } else if (listV.get(0).getMeasure() >= (listS.get(0).getPos() + listS.get(0).size() - 1)) {
          Container<V, Segment<V>> container = listS.remove(0);
          mergedList.add(container);
        } else {
          Container<V, Segment<V>> container = listS.remove(0);
          LV<V> v = listV.remove(0);
          int k = (int) Math.ceil(v.getMeasure() - container.getPos());
          if (log.isTraceEnabled()) log.trace("will split {} at {}", container, k);
          Pair<Container<V, Segment<V>>> containerPair = Container.split(container, k);
          if (log.isTraceEnabled())
            log.trace("got {} and {}", containerPair.first, containerPair.second);
          mergedList.add(containerPair.first);
          mergedList.add(v);
          int pos = container.getPos() + k;
          containerPair.second.setPos(pos);
          listS.add(0, containerPair.second);
        }
      }
      // add any leftovers to listPlusOne
      mergedList.addAll(listV);
      mergedList.addAll(listS);
    }
    mergedList.addAll(segmentVertexList);
    StringBuilder builder = new StringBuilder("S3 mergedList:\n");
    mergedList.forEach(v -> builder.append(v.toString()).append("\n"));
    if (log.isTraceEnabled()) {
      log.trace(builder.toString());
    }

    biLayer.downstreamLayer = mergedList;
  }

  static <V, E> String elementStringer(BiLayer biLayer, List<LV<V>> layer) {
    StringBuilder builder = new StringBuilder();
    layer.forEach(s -> builder.append(elementStringer(biLayer, s)));
    return builder.toString();
  }

  static <V> String elementStringer(BiLayer biLayer, LV<V> v) {
    StringBuilder builder = new StringBuilder();
    builder.append(v.toString()).append("\n");
    return builder.toString();
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
      List<Container<V, Segment<V>>> containerList =
          downstreamLayer
              .stream()
              .filter(v -> v instanceof Container)
              .map(v -> (Container<V, Segment<V>>) v)
              .collect(Collectors.toList());
      // find its container
      Segment<V> segment = q.getSegment();
      Optional<Container<V, Segment<V>>> containerOpt =
          containerList.stream().filter(c -> c.contains(segment)).findFirst();
      if (containerOpt.isPresent()) {
        if (log.isTraceEnabled()) {
          log.trace("S4 currentLayer: \n{}", elementStringer(biLayer, currentLayer));
        }
        Container<V, Segment<V>> container = containerOpt.get();
        int loserIdx = downstreamLayer.indexOf(container);
        if (log.isTraceEnabled()) {
          log.trace(
              "found container {} at index {} with list index {} for qVertex {} with index {} and list index {}",
              container,
              container.getIndex(),
              loserIdx,
              q,
              q.getIndex(),
              downstreamLayer.indexOf(q));
          log.trace("downstreamLayer: \n{}", elementStringer(biLayer, downstreamLayer));
          log.trace("splitting on {} because of {}", segment, q);
        }

        Pair<Container<V, Segment<V>>> containerPair = Container.split(container, segment);

        if (log.isTraceEnabled())
          log.trace(
              "splitFound container into {} and {}", containerPair.first, containerPair.second);
        log.trace(
            "container pair is now {} and {}",
            containerPair.first.printTree("\n"),
            containerPair.second.printTree("\n"));

        if (containerPair.first.size() > 0) {
          virtualEdges.add(VirtualEdge.of(container, containerPair.first));
        }
        if (containerPair.second.size() > 0) {
          virtualEdges.add(VirtualEdge.of(container, containerPair.second));
        }

        virtualEdges.add(VirtualEdge.of(container, q));
        virtualEdges.removeIf(e -> e.getSource() == container && e.getTarget() == container);
        downstreamLayer.remove(q);
        if (log.isTraceEnabled()) {
          log.trace("removed container {}", container.printTree("\n"));
          log.trace("adding container {}", containerPair.first.printTree("\n"));
          log.trace("adding container {}", containerPair.second.printTree("\n"));
        }
        downstreamLayer.remove(container);
        downstreamLayer.add(loserIdx, containerPair.first);
        downstreamLayer.add(loserIdx + 1, q);
        downstreamLayer.add(loserIdx + 2, containerPair.second);

        if (log.isTraceEnabled()) {
          log.trace("S4 downstreamLayer now: \n{}", elementStringer(biLayer, downstreamLayer));
          log.trace("virtualEdges: \n{}", virtualEdges);
        }

      } else {
        log.error("container opt was empty for segment {}", segment);
      }
    }

    IntStream.range(0, downstreamLayer.size()).forEach(i -> downstreamLayer.get(i).setIndex(i));
    Arrays.sort(biLayer.downstreamArray, Comparator.comparingInt(LV::getIndex));
  }

  public static <V, E> int stepFive(
      Graph<LV<V>, LE<V, E>> graph,
      boolean forwards,
      BiLayer<V, E> biLayer,
      List<VirtualEdge<V, E>> virtualEdges) {
    if (forwards) {
      return transposeDownwards(graph, biLayer, virtualEdges);
    } else {
      return transposeUpwards(graph, biLayer, virtualEdges);
    }
  }

  private static <V, E> int transposeDownwards(
      Graph<LV<V>, LE<V, E>> graph, BiLayer<V, E> biLayer, List<VirtualEdge<V, E>> virtualEdges) {
    int crossCount = 0;

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
    for (int j = 0; j < biLayer.downstreamLayer.size() - 1; j++) {
      LV<V> vj = biLayer.downstreamLayer.get(j);
      LV<V> vnext = biLayer.downstreamLayer.get(j + 1);
      if (vj instanceof Container || vnext instanceof Container) {
        continue;
      }
      int vw = crossingCount(biLayerEdges);
      // count with j and j+1 swapped
      int wv = crossingCountSwapped(j, j + 1, biLayer.downstreamLayer, biLayerEdges);
      if (vw > wv) {
        swap(biLayer.downstreamLayer, j, j + 1);
        crossCount += wv;
      } else {
        crossCount += vw;
      }
    }
    return crossCount;
  }

  private static <V, E> int transposeUpwards(
      Graph<LV<V>, LE<V, E>> graph, BiLayer<V, E> biLayer, List<VirtualEdge<V, E>> virtualEdges) {

    int crossCount = 0;
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
    List<LE<V, E>> swapped = swapEdgeEndpoints(biLayerEdges);
    for (int j = 0; j < biLayer.downstreamLayer.size() - 1; j++) {
      LV<V> vj = biLayer.downstreamLayer.get(j);
      LV<V> vnext = biLayer.downstreamLayer.get(j + 1);
      if (vj instanceof Container || vnext instanceof Container) {
        continue;
      }

      int vw = crossingCount(swapped);
      // count with j and j+1 swapped
      int wv = crossingCountSwapped(j, j + 1, biLayer.downstreamLayer, swapped);
      if (vw > wv) {
        swap(biLayer.downstreamLayer, j, j + 1);
        crossCount += wv;
      } else {
        crossCount += vw;
      }
    }
    return crossCount;
  }

  private static <V, E> List<LE<V, E>> swapEdgeEndpoints(List<LE<V, E>> list) {
    return list.stream()
        .map(e -> LE.of(e.getEdge(), e.getTarget(), e.getSource()))
        .collect(Collectors.toList());
  }

  private static <V, E> int crossingCount(List<LE<V, E>> edges) {

    Comparator<LE<V, E>> biLevelEdgeComparator = Comparators.biLevelEdgeComparator();

    edges.sort(biLevelEdgeComparator);
    List<Integer> targetIndices = new ArrayList<>();
    int weight = 1;
    for (LE<V, E> edge : edges) {
      LV<V> target = edge.getTarget();
      if (target instanceof Container) {
        //        weight += ((Container<V,Segment<V>>)edge.getTarget()).size();
      }
      targetIndices.add(target.getIndex());
    }
    int[] presorted = targetIndices.stream().mapToInt(i -> i).toArray();
    int cnt = weight * InsertionSortCounter.insertionSortCounter(targetIndices);
    return cnt;
  }

  private static <V, E> int crossingCountSwapped(
      int i, int j, List<LV<V>> layer, List<LE<V, E>> edges) {

    Comparator<LE<V, E>> biLevelEdgeComparator = Comparators.biLevelEdgeComparator();

    swap(layer, i, j);
    edges.sort(biLevelEdgeComparator);
    List<Integer> targetIndices = new ArrayList<>();
    int weight = 1;
    for (LE<V, E> edge : edges) {
      LV<V> target = edge.getTarget();
      if (target instanceof Container) {
        //        weight += ((Container<V,Segment<V>>)edge.getTarget()).size();
      }
      targetIndices.add(target.getIndex());
    }
    swap(layer, i, j);
    int[] presorted = targetIndices.stream().mapToInt(ii -> ii).toArray();
    int cnt = weight * InsertionSortCounter.insertionSortCounter(targetIndices);
    return cnt;
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

    biLayer.downstreamLayer = EiglspergerUtil.scan(biLayer.downstreamLayer);
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
