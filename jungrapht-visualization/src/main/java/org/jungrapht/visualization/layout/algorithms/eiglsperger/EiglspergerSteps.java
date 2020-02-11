package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Comparators;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.util.InsertionSortCounter;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EiglspergerSteps<V, E> {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerSteps.class);

  protected Graph<LV<V>, LE<V, E>> svGraph;
  protected LV<V>[][] layersArray;
  protected List<LV<V>> layer;
  protected Predicate<LV<V>> joinVertexPredicate;
  protected Predicate<LV<V>> splitVertexPredicate;
  protected BiFunction<Graph<LV<V>, LE<V, E>>, LV<V>, List<LV<V>>> neighborFunction;

  protected EiglspergerSteps(
      Graph<LV<V>, LE<V, E>> svGraph,
      LV<V>[][] layersArray,
      Predicate<LV<V>> joinVertexPredicate,
      Predicate<LV<V>> splitVertexPredicate,
      BiFunction<Graph<LV<V>, LE<V, E>>, LV<V>, List<LV<V>>> neighborFunction) {
    this.svGraph = svGraph;
    //    this.layer = layer;
    this.layersArray = layersArray;
    this.joinVertexPredicate = joinVertexPredicate;
    this.splitVertexPredicate = splitVertexPredicate;
    this.neighborFunction = neighborFunction;
  }

  private void log(String label, List<LV<V>> list) {
    log.info(label);
    list.forEach(v -> log.info(" - {}", v.toString()));
  }

  private void log(String label, LV<V>[] array) {
    log.info(label);
    Arrays.stream(array).forEach(v -> log.info(" - {}", v.toString()));
  }

  /**
   * for any pVertex that is in the list, take that pVertex's segment and append it to the any prior
   * Container in the list (creating the Container as needed), and do not append the PVertex in the
   * list to be returned. Finally, scan the list to join any sequential Containers into one and to
   * insert empty Containers between sequential vertices.
   *
   * <p>// * @param biLayer // * @param <V>
   *
   * @return layerI modified so that PVertices are gone (added to previous containers)
   */
  public void stepOne(List<LV<V>> currentLayer) {

    if (log.isTraceEnabled()) log("stepOne currentLayer in", currentLayer);

    List<LV<V>> outList = new ArrayList<>();

    for (LV<V> v : currentLayer) {
      // for each PVertex, add it to the empty or not empty list
      if (joinVertexPredicate.test(v)) {
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
    List<LV<V>> scannedList = EiglspergerUtil.scan(outList);
    currentLayer.clear();
    currentLayer.addAll(scannedList);
    if (log.isTraceEnabled())
      log("stepOne currentLayer out (merged pvertices into containers)", currentLayer);
  }

  public void stepTwo(List<LV<V>> currentLayer, List<LV<V>> downstreamLayer) {

    if (log.isTraceEnabled()) log("stepTwo currentLayer in", currentLayer);
    if (log.isTraceEnabled()) log("stepTwo downstreamLayer in", downstreamLayer);

    assignPositions(currentLayer);

    List<Container<V, Segment<V>>> containersFromCurrentLayer =
        currentLayer
            .stream()
            .filter(v -> v instanceof Container)
            .map(v -> (Container<V, Segment<V>>) v)
            .filter(c -> c.size() > 0)
            .collect(Collectors.toList());

    // add to downstreamLayer, any currentLayer containers that are not already present
    containersFromCurrentLayer
        .stream()
        .filter(c -> !downstreamLayer.contains(c))
        .forEach(downstreamLayer::add);

    assignMeasures(downstreamLayer);
    if (log.isTraceEnabled())
      log("stepTwo currentLayer out (computed pos for currentLayer)", currentLayer);
    if (log.isTraceEnabled())
      log("stepTwo downstreamLayer out (computed measures for downstreamLayer)", downstreamLayer);
  }

  public void stepThree(List<LV<V>> downstreamLayer) {

    if (log.isTraceEnabled()) log("stepThree downstreamLayer in", downstreamLayer);

    List<LV<V>> listV = new LinkedList<>();
    List<Container<V, Segment<V>>> listS = new LinkedList<>();

    List<SegmentVertex<V>> segmentVertexList = new ArrayList<>();
    for (LV<V> v : downstreamLayer) {
      if (splitVertexPredicate.test(v)) { // skip any QVertex for top to bottom
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

    downstreamLayer.clear();
    downstreamLayer.addAll(mergedList);
    if (log.isTraceEnabled())
      log("stepThree downstreamLayer out (initial ordering for downstreamLayer)", downstreamLayer);
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

  public void stepFour(List<LV<V>> downstreamLayer, int downstreamRank) {
    if (log.isTraceEnabled()) log("stepFour downstreamLayer in", downstreamLayer);

    // for each qVertex, get its Segment, find the segment in one of the containers in downstreamLayer

    // gather the qVertices
    List<SegmentVertex<V>> qVertices =
        downstreamLayer
            .stream()
            .filter(v -> splitVertexPredicate.test(v)) // QVertices
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

        //        if (containerPair.first.size() > 0) {
        //          virtualEdges.add(VirtualEdge.of(container, containerPair.first));
        //        }
        //        if (containerPair.second.size() > 0) {
        //          virtualEdges.add(VirtualEdge.of(container, containerPair.second));
        //        }
        //
        //        virtualEdges.add(VirtualEdge.of(container, q));
        //        virtualEdges.removeIf(e -> e.getSource() == container && e.getTarget() == container);
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
      } else {
        log.error("container opt was empty for segment {}", segment);
      }
    }

    IntStream.range(0, downstreamLayer.size()).forEach(i -> downstreamLayer.get(i).setIndex(i));
    Arrays.sort(layersArray[downstreamRank], Comparator.comparingInt(LV::getIndex));
    if (log.isTraceEnabled())
      log("stepFour downstreamLayer out (split containers for Q/PVertices)", downstreamLayer);
    if (log.isTraceEnabled())
      log("layersArray[" + downstreamRank + "] out", layersArray[downstreamRank]);
  }

  public int stepFive(
      boolean forwards, List<LV<V>> downstreamLayer, int currentRank, int downstreamRank) {
    if (forwards) {
      return transposeDownwards(downstreamLayer, currentRank, downstreamRank);
    } else {
      return transposeUpwards(downstreamLayer, currentRank, downstreamRank);
    }
  }

  private int transposeDownwards(List<LV<V>> downstreamLayer, int currentRank, int downstreamRank) {
    int crossCount = 0;

    List<LE<V, E>> biLayerEdges =
        svGraph
            .edgeSet()
            .stream()
            .filter(
                e ->
                    svGraph.getEdgeSource(e).getRank() == currentRank
                        && svGraph.getEdgeTarget(e).getRank() == downstreamRank)
            .collect(Collectors.toList());
    for (int j = 0; j < downstreamLayer.size() - 1; j++) {
      LV<V> vj = downstreamLayer.get(j);
      LV<V> vnext = downstreamLayer.get(j + 1);
      if (vj instanceof Container || vnext instanceof Container) {
        continue;
      }
      int vw = crossingCount(biLayerEdges);
      // count with j and j+1 swapped
      swap(downstreamLayer, j, j + 1);
      int wv = crossingCount(biLayerEdges);
      swap(downstreamLayer, j, j + 1);

      if (vw > wv) {
        swap(downstreamLayer, j, j + 1);
        crossCount += wv;
      } else {
        crossCount += vw;
      }
    }
    return crossCount;
  }

  private int transposeUpwards(List<LV<V>> downstreamLayer, int currentRank, int downstreamRank) {

    int crossCount = 0;
    List<LE<V, E>> biLayerEdges =
        svGraph
            .edgeSet()
            .stream()
            .filter(
                e ->
                    svGraph.getEdgeSource(e).getRank() == currentRank
                        && svGraph.getEdgeTarget(e).getRank() == downstreamRank)
            .collect(Collectors.toList());
    List<LE<V, E>> swapped = swapEdgeEndpoints(biLayerEdges);
    for (int j = 0; j < downstreamLayer.size() - 1; j++) {
      LV<V> vj = downstreamLayer.get(j);
      LV<V> vnext = downstreamLayer.get(j + 1);
      if (vj instanceof Container || vnext instanceof Container) {
        continue;
      }

      int vw = crossingCount(swapped);
      // count with j and j+1 swapped
      swap(downstreamLayer, j, j + 1);
      int wv = crossingCount(biLayerEdges);
      swap(downstreamLayer, j, j + 1);

      if (vw > wv) {
        swap(downstreamLayer, j, j + 1);
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
        weight += ((Container<V, Segment<V>>) edge.getTarget()).size();
      }
      targetIndices.add(target.getIndex());
    }
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

  public void stepSix(List<LV<V>> downstreamLayer) {

    if (log.isTraceEnabled()) log("stepSix downstreamLayer in", downstreamLayer);
    List<LV<V>> scanned = EiglspergerUtil.scan(downstreamLayer);
    downstreamLayer.clear();
    downstreamLayer.addAll(scanned);
    if (log.isTraceEnabled())
      log("stepSix downstreamLayer out (padded with and compressed containers)", downstreamLayer);
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

  void assignPositions(List<LV<V>> currentLayer) {
    LV<V> previousVertex = null;
    Container<V, Segment<V>> previousContainer = null;
    for (int i = 0; i < currentLayer.size(); i++) {
      LV<V> v = currentLayer.get(i);

      if (i % 2 == 0) {
        // this is a container
        Container<V, Segment<V>> container = (Container<V, Segment<V>>) v;
        if (container.size() > 0) {
          if (previousContainer == null) {
            // first container non empty
            container.setPos(0);
          } else {
            // there has to be a previousVertex
            int pos = previousVertex.getPos() + 1;
            container.setPos(pos);
          }
        }
        previousContainer = container;
      } else {
        // this is a vertex
        if (previousVertex == null) {
          // first vertex (position 1)
          int pos = previousContainer.size();
          v.setPos(pos);
        } else {
          int pos = previousVertex.getPos() + previousContainer.size() + 1;
          v.setPos(pos);
        }
        previousVertex = v;
      }
    }
  }

  void assignMeasures(List<LV<V>> downstreamLayer) {
    downstreamLayer
        .stream()
        .filter(v -> v instanceof Container)
        .map(v -> (Container<V, Segment<V>>) v)
        .filter(c -> c.size() > 0)
        .forEach(
            c -> {
              double measure = c.getPos();
              c.setMeasure(measure);
            });

    for (int i = 0; i < downstreamLayer.size(); i++) {
      LV<V> v = downstreamLayer.get(i);
      if (splitVertexPredicate.test(v)) { // QVertex for top to bottom
        continue;
      }
      if (v instanceof Container) {
        Container<V, Segment<V>> container = (Container<V, Segment<V>>) v;
        double measure = container.getPos();
        container.setMeasure(measure);
      } else {
        // not a container (nor QVertex for top to bottom)
        // measure will be related to the median of the pos of predecessor vert
        List<LV<V>> neighbors = neighborFunction.apply(svGraph, v);
        int[] poses = new int[neighbors.size()];
        IntStream.range(0, poses.length).forEach(idx -> poses[idx] = neighbors.get(idx).getPos());
        Arrays.sort(poses);
        if (poses.length > 0) {
          double measure = medianValue(poses);
          //poses[(poses.length - 1) / 2];
          v.setMeasure(measure);
        } else {
          // leave the measure as as the current pos
          if (v.getPos() < 0) {
            log.error("no pos for {}", v);
          }
          double measure = v.getPos();
          v.setMeasure(measure);
        }
      }
    }
  }

  static double medianValue(int[] P) {
    int m = P.length / 2;
    if (P.length == 0) {
      return -1;
    } else if (P.length % 2 == 1) {
      return P[m];
    } else if (P.length == 2) {
      return (P[0] + P[1]) / 2;
    } else {
      double left = P[m - 1] - P[0];
      double right = P[P.length - 1] - P[m];
      return (P[m - 1] * right + P[m] * left) / (left + right);
    }
  }
}
