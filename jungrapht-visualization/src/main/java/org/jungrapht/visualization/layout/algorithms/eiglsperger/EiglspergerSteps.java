package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.NeighborCache;
import org.jungrapht.visualization.layout.algorithms.sugiyama.AccumulatorTreeUtil;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Comparators;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.util.InsertionSortCounter;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The five steps of the Eiglsperger optimization of the Sugiyama Layout Algorithm
 *
 * <p>Javadoc text includes descriptions from the Eiglsperger paper
 *
 * @see "Methods for Visual Understanding Hierarchical System Structures. KOZO SUGIYAMA, MEMBER,
 *     IEEE, SHOJIRO TAGAWA, AND MITSUHIKO TODA, MEMBER, IEEE"
 * @see "An E log E Line Crossing Algorithm for Levelled Graphs. Vance Waddle and Ashok Malhotra IBM
 *     Thomas J. Watson Research Center"
 * @see "Simple and Efficient Bilayer Cross Counting. Wilhelm Barth, Petra Mutzel, Institut für
 *     Computergraphik und Algorithmen Technische Universität Wien, Michael Jünger, Institut für
 *     Informatik Universität zu Köln"
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 * @see "An Efficient Implementation of Sugiyama's Algorithm for Layered Graph Drawing. Markus
 *     Eiglsperger, Martin Siebenhaller, Michael Kaufman"
 * @param <V> vertex type
 * @param <E> edge type
 */
public class EiglspergerSteps<V, E> {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerSteps.class);

  /** The delegate Graph to layout */
  protected Graph<LV<V>, LE<V, E>> svGraph;

  protected NeighborCache<LV<V>, LE<V, E>> neighborCache;

  /** the result of layering the graph vertices and introducint synthetic vertices and edges */
  protected LV<V>[][] layersArray;
  /** when sweeping top to bottom, this is a PVertex, bottom to top, this is a QVertex */
  protected Predicate<LV<V>> joinVertexPredicate;
  /** when sweeping top to bottom, this is a QVertex, bottom to top this is a PVertex */
  protected Predicate<LV<V>> splitVertexPredicate;

  protected Function<List<LE<V, E>>, List<LE<V, E>>> edgeEndpointSwapOrNot;

  /**
   * When sweeping top to bottom, this function returns predecessors When sweeping bottom to top,
   * this function returns sucessors
   */
  protected Function<LV<V>, Set<LV<V>>> neighborFunction;

  protected Function<LE<V, E>, LV<V>> edgeSourceFunction;
  protected Function<LE<V, E>, LV<V>> edgeTargetFunction;
  protected boolean transpose;
  protected Graph<LV<V>, Integer> compactionGraph;
  protected Set<LE<V, E>> typeOneConflicts = new HashSet<>();

  /**
   * @param svGraph the delegate graph
   * @param layersArray layered vertices
   * @param joinVertexPredicate vertices to join with Containers
   * @param splitVertexPredicate vertices to split from Containers
   * @param neighborFunction predecessors or successors in the Graph
   */
  protected EiglspergerSteps(
      Graph<LV<V>, LE<V, E>> svGraph,
      LV<V>[][] layersArray,
      Predicate<LV<V>> joinVertexPredicate,
      Predicate<LV<V>> splitVertexPredicate,
      Function<LE<V, E>, LV<V>> edgeSourceFunction,
      Function<LE<V, E>, LV<V>> edgeTargetFunction,
      Function<LV<V>, Set<LV<V>>> neighborFunction,
      Function<List<LE<V, E>>, List<LE<V, E>>> edgeEndpointSwapOrNot,
      boolean transpose) {
    this.svGraph = svGraph;
    this.neighborCache = new NeighborCache<>(svGraph);
    this.layersArray = layersArray;
    this.joinVertexPredicate = joinVertexPredicate;
    this.splitVertexPredicate = splitVertexPredicate;
    this.edgeSourceFunction = edgeSourceFunction;
    this.edgeTargetFunction = edgeTargetFunction;
    this.neighborFunction = neighborFunction;
    this.edgeEndpointSwapOrNot = edgeEndpointSwapOrNot;
    this.transpose = transpose;
  }

  /**
   * formatted output
   *
   * @param label identifier
   * @param list vertices to log
   */
  private void log(String label, List<LV<V>> list) {
    log.info(label);
    list.forEach(v -> log.info(" - {}", v.toString()));
  }

  protected static <V, E> void clearGraph(Graph<V, E> graph) {
    Set<E> edges = new HashSet<>(graph.edgeSet());
    Set<V> vertices = new HashSet<>(graph.vertexSet());
    graph.removeAllEdges(edges);
    graph.removeAllVertices(vertices);
  }

  public Set<LE<V, E>> getTypeOneConflicts() {
    return typeOneConflicts;
  }

  /**
   * formatted output
   *
   * @param label identifier
   * @param array vertices to log
   */
  private void log(String label, LV<V>[] array) {
    log.info(label);
    Arrays.stream(array).forEach(v -> log.info(" - {}", v.toString()));
  }

  /**
   * "In the first step we append the segment s(v) for each p-vertex v in layer L i to the container
   * preceding v. Then we join this container with the succeeding container. The result is again an
   * alternating layer (p-vertices are omitted). for any PVertex (QVertex) that is in the list, take
   * that vertex's segment and append it to the any prior Container in the list (creating the
   * Container as needed), and do not append the PVertex (QVertex) in the list to be returned.
   * Finally, scan the list to join any sequential Containers into one and to insert empty
   * Containers between sequential vertices.
   *
   * @param currentLayer the rank of vertices to operate over
   * @return layerI modified so that PVertices are gone (added to previous containers)
   */
  public void stepOne(List<LV<V>> currentLayer) {

    if (log.isTraceEnabled()) log("stepOne currentLayer in", currentLayer);

    List<LV<V>> outList = new ArrayList<>();

    for (LV<V> v : currentLayer) {
      // for each PVertex/QVertex, add it to the list's adjacent container
      if (joinVertexPredicate.test(v)) {
        if (outList.isEmpty()) {
          outList.add(Container.createSubContainer());
        }
        Container<V> lastContainer = (Container<V>) outList.get(outList.size() - 1);
        SegmentVertex<V> segmentVertex = (SegmentVertex<V>) v;
        Segment<V> segment = segmentVertex.getSegment();
        lastContainer.append(segment);
      } else {
        outList.add(v);
      }
    }
    List<LV<V>> scannedList = EiglspergerUtil.scan(outList);
    currentLayer.clear();
    currentLayer.addAll(scannedList);

    IntStream.range(0, currentLayer.size()).forEach(i -> currentLayer.get(i).setIndex(i));

    if (log.isTraceEnabled())
      log("stepOne currentLayer out (merged pvertices into containers)", currentLayer);
  }

  private static <V> void updateIndices(List<LV<V>> layer) {
    IntStream.range(0, layer.size()).forEach(i -> layer.get(i).setIndex(i));
  }

  /**
   * "In the second step we compute the measure values for the elements in L i+1 . First we assign a
   * position value pos(v i j ) to all vertices v i j in L i . pos(v i 0 ) = size(S i 0 ) and pos(v
   * i j ) = pos(v i j−1 ) + size(S i j ) + 1. Note that the pos values are the same as they would
   * be in the median or barycenter heuristic if each segment was represented as dummy vertex. Each
   * non- empty container S i j has pos value pos(v i j −1 ) + 1. If container S i 0 is non- empty
   * it has pos value 0. Now we assign the measure to all non-q-vertices and containers in L i+1 .
   * The initial containers in L i+1 are the resulting containers of the first step. Recall that the
   * measure of a container in L i+1 is its position in L i ." Assign positions to the
   * currentLayerVertices and use those posisions to calculate the measure for vertices in the
   * downstreamLayer. The measure here is the median of the positions of neghbor vertices
   *
   * @param currentLayer
   * @param downstreamLayer
   */
  public void stepTwo(List<LV<V>> currentLayer, List<LV<V>> downstreamLayer) {

    if (log.isTraceEnabled()) log("stepTwo currentLayer in", currentLayer);
    if (log.isTraceEnabled()) log("stepTwo downstreamLayer in", downstreamLayer);

    assignPositions(currentLayer);

    if (updatePositions(currentLayer)) {
      log.error("positions were off for {}", currentLayer);
    }

    List<Container<V>> containersFromCurrentLayer =
        currentLayer
            .stream()
            .filter(v -> v instanceof Container)
            .map(v -> (Container<V>) v)
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

  /**
   * "In the third step we calculate an initial ordering of L i+1 . We sort all non-q-vertices in L
   * i+1 according to their measure in a list L V . We do the same for the containers and store them
   * in a list L S . We use the following operations on these sorted lists:"
   *
   * <ul>
   *   <li>◦ l = pop(L) : Removes the first element l from list L and returns it.
   *   <li>◦ push(L, l) : Inserts element l at the head of list L.
   * </ul>
   *
   * We merge both lists in the following way: <code>
   * if m(head(L V )) ≤ pos(head(L S ))
   *    then v = pop(L V ), append(L i+1 , v)
   * if m(head(L V )) ≥ (pos(head(L S )) + size(head(L S )) − 1)
   *    then S = pop(L S ), append(L i+1 , S)
   * else S = pop(L S ), v = pop(L V ), k = ⌈m(v) − pos(S)⌉,
   *    (S 1 ,S 2 ) = split(S, k), append(L i+1 ,S 1 ), append(L i+1 , v),
   *    pos(S 2 ) = pos(S) + k, push(L S ,S 2 ).
   * </code>
   *
   * @param downstreamLayer
   */
  public void stepThree(List<LV<V>> downstreamLayer) {

    if (log.isTraceEnabled()) log("stepThree downstreamLayer in", downstreamLayer);

    List<LV<V>> listV = new LinkedList<>();
    List<Container<V>> listS = new LinkedList<>();

    List<SegmentVertex<V>> segmentVertexList = new ArrayList<>();
    for (LV<V> v : downstreamLayer) {
      if (splitVertexPredicate.test(v)) { // skip any QVertex for top to bottom
        segmentVertexList.add((SegmentVertex<V>) v);
        continue;
      } else if (v instanceof Container) {
        Container<V> container = (Container<V>) v;
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
        && listS.stream().mapToDouble(Container::getMeasure).min().getAsDouble() < 0) {
      log.debug("something missing");
    }
    if (log.isTraceEnabled()) {
      log.trace("listV measures: {}", listV);
    }
    if (listV.size() > 0 && listV.stream().mapToDouble(LV::getMeasure).min().getAsDouble() < 0) {
      log.debug("something missing");
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
          Container<V> container = listS.remove(0);
          mergedList.add(container);
        } else {
          Container<V> container = listS.remove(0);
          LV<V> v = listV.remove(0);
          int k = (int) Math.ceil(v.getMeasure() - container.getPos());
          if (log.isTraceEnabled()) log.trace("will split {} at {}", container, k);
          Pair<Container<V>> containerPair = Container.split(container, k);
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

    // fix the indices
    updateIndices(downstreamLayer);

    if (updatePositions(downstreamLayer)) {
      log.trace("positions were updated for {}", downstreamLayer);
    }
    if (log.isTraceEnabled())
      log("stepThree downstreamLayer out (initial ordering for downstreamLayer)", downstreamLayer);
  }

  /**
   * In the fourth step we place each q-vertex v of L i+1 according to the position of its
   * corresponding segment s(v). We do this by calling split(S, s(v)) for each q-vertex v in layer L
   * i+1 and placing v between the resulting containers (S denotes the container that includes
   * s(v)).
   *
   * @param downstreamLayer
   * @param downstreamRank
   */
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

    for (SegmentVertex<V> q : qVertices) {
      List<Container<V>> containerList =
          downstreamLayer
              .stream()
              .filter(v -> v instanceof Container)
              .map(v -> (Container<V>) v)
              .collect(Collectors.toList());
      // find its container
      Segment<V> segment = q.getSegment();
      Optional<Container<V>> containerOpt =
          containerList.stream().filter(c -> c.contains(segment)).findFirst();
      if (containerOpt.isPresent()) {
        Container<V> container = containerOpt.get();
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

        Pair<Container<V>> containerPair = Container.split(container, segment);

        if (log.isTraceEnabled())
          log.trace(
              "splitFound container into {} and {}", containerPair.first, containerPair.second);
        log.trace(
            "container pair is now {} and {}",
            containerPair.first.printTree("\n"),
            containerPair.second.printTree("\n"));

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

    updateIndices(downstreamLayer);
    updatePositions(downstreamLayer);
    //    IntStream.range(0, downstreamLayer.size()).forEach(i -> downstreamLayer.get(i).setIndex(i));
    Arrays.sort(layersArray[downstreamRank], Comparator.comparingInt(LV::getIndex));
    if (log.isTraceEnabled())
      log("stepFour downstreamLayer out (split containers for Q/PVertices)", downstreamLayer);
    if (log.isTraceEnabled())
      log("layersArray[" + downstreamRank + "] out", layersArray[downstreamRank]);
  }

  /**
   * In the fifth step we perform cross counting according to the scheme pro- posed by Barth et al
   * (see Section 1.2). During the cross counting step between layer L i and L i+1 we therefore
   * consider all layer elements as ver- tices. Beside the common edges between both layers, we also
   * have to handle virtual edges, which are imaginary edges between a container ele- ment in L i
   * and the resulting container elements or q-vertices in L i+1 (see Figure 5). In terms of the
   * common approach each virtual edge represents at least one edge between two dummy vertices. The
   * number of represented edges is equal to the size of the container element in L i+1 . We have to
   * consider this fact to get the right number of edge crossings. We therefore introduce edge
   * weights. The weight of a virtual edge ending with a con- tainer element S is equal to size(S).
   * The weight of the other edges is one. So a crossing between two edges e 1 and e 2 counts as
   * weight(e 1 )·weight(e 2 ) crossings.
   *
   * @param currentLayer the Li layer
   * @param downstreamLayer the Li+1 (or Li-1 for backwards) layer
   * @param currentRank the value of i for Li
   * @param downstreamRank the value of i+1 (or i-1 for backwards)
   * @return count of edge crossing weight
   */
  public int stepFive(
      List<LV<V>> currentLayer, List<LV<V>> downstreamLayer, int currentRank, int downstreamRank) {
    return transpose(currentLayer, downstreamLayer, currentRank, downstreamRank);
  }

  private int transpose(
      List<LV<V>> currentLayer, List<LV<V>> downstreamLayer, int currentRank, int downstreamRank) {

    // gather all the graph edges between the currentRank and the downstreamRank
    List<LE<V, E>> biLayerEdges =
        svGraph
            .edgeSet()
            .stream()
            .filter(
                e ->
                    edgeSourceFunction.apply(e).getRank() == currentRank
                        && edgeTargetFunction.apply(e).getRank() == downstreamRank)
            .collect(Collectors.toList());

    // create virtual edges between non-empty containers in both ranks
    // if the downstreamLayer has a QVertex/PVertex, create a virtual edge between a new synthetic vertex
    // in currentLayer and the QVertex/PVertex in the downstreamLayer
    Set<LE<V, E>> virtualEdges = new HashSet<>();
    for (LV<V> v : downstreamLayer) {

      if (v instanceof Container) {
        Container<V> container = (Container<V>) v;
        if (container.size() > 0) {
          virtualEdges.add(VirtualEdge.of(container, container));
          //          biLayerEdges.add(VirtualEdge.of(container, container));
        }
      } else if (splitVertexPredicate.test(v)) {
        // downwards, this is a QVertex, upwards its a PVertex
        SegmentVertex<V> qv = (SegmentVertex<V>) v;
        SyntheticLV<V> qvSource = SyntheticLV.of();
        qvSource.setIndex(qv.getIndex());
        qvSource.setPos(qv.getPos());
        virtualEdges.add(VirtualEdge.of(qvSource, qv));
        //        biLayerEdges.add(VirtualEdge.of(qvSource, qv));
      }
    }

    // remove any empty containers from the currentLayer and reset the index metadata
    // for the currentLayer vertices
    for (int i = 0; i < currentLayer.size(); i++) {
      LV<V> v = currentLayer.get(i);
      if (isEmptyContainer(v)) {
        currentLayer.remove(i);
      }
    }
    updateIndices(currentLayer);
    updatePositions(currentLayer);

    // remove any empty containers from the downstreamLayer and reset the index metadata
    // for the currentLayer vertices
    for (int i = 0; i < downstreamLayer.size(); i++) {
      LV<V> v = downstreamLayer.get(i);
      if (isEmptyContainer(v)) {
        downstreamLayer.remove(i);
      }
    }
    updateIndices(downstreamLayer);
    updatePositions(downstreamLayer);

    typeOneConflicts.addAll(this.getEdgesThatCrossVirtualEdge(virtualEdges, biLayerEdges));
    biLayerEdges.addAll(virtualEdges);

    // downwards, the function is a no-op, upwards the biLayerEdges endpoints are swapped
    log.trace("for ranks {} and {} ....", currentRank, downstreamRank);
    return processRanks(downstreamLayer, edgeEndpointSwapOrNot.apply(biLayerEdges));
  }

  private int processRanks(List<LV<V>> downstreamLayer, List<LE<V, E>> biLayerEdges) {
    int crossCount = Integer.MAX_VALUE;
    // define a function that will get the edge weight from its target vertex
    // in the downstream layer. If the target is a container, it's weight is
    // the size of the container
    Function<Integer, Integer> f =
        i -> {
          LE<V, E> edge = biLayerEdges.get(i);
          LV<V> target = edge.getTarget();
          if (target instanceof Container) {
            return ((Container<V>) target).size();
          }
          return 1;
        };
    if (downstreamLayer.size() < 2) {
      crossCount = 0;
    }
    for (int j = 0; j < downstreamLayer.size() - 1; j++) {

      // if either of the adjacent vertices is a container, skip them
      if (log.isTraceEnabled()) {
        // runs the crossingCount (no weights) with the insertionSort method and the AccumulatorTree method
        // these values should match and should both be <= to the crossingWeight
        int vw2 = crossingCount(biLayerEdges);
        int vw3 = AccumulatorTreeUtil.crossingCount(biLayerEdges);
        log.trace("IS count:{}, AC count:{}", vw2, vw3);
      }
      int vw = AccumulatorTreeUtil.crossingWeight(biLayerEdges, f);
      crossCount = Math.min(vw, crossCount);
      //      if (downstreamLayer.get(j) instanceof Container
      //              || downstreamLayer.get(j + 1) instanceof Container) {
      //        continue;
      //      }
      if (log.isTraceEnabled()) {
        log.trace("crossingWeight:{}", vw);
      }
      if (vw == 0) {
        // can't do better than zero
        break;
      }
      if (downstreamLayer.get(j).getMeasure() != downstreamLayer.get(j + 1).getMeasure()) {
        continue;
      }
      // count with j and j+1 swapped
      // first swap them
      swap(downstreamLayer, j, j + 1);
      if (log.isTraceEnabled()) {
        // runs the crossingCount (no weights) with the insertionSort method and the AccumulatorTree method
        // these values should match and should both be <= to the crossingWeight
        int wv2 = crossingCount(biLayerEdges);
        int wv3 = AccumulatorTreeUtil.crossingCount(biLayerEdges);
        log.trace("IS count:{}, AC count:{}", wv2, wv3);
      }
      int wv = AccumulatorTreeUtil.crossingWeight(biLayerEdges, f);
      crossCount = Math.min(wv, crossCount);
      if (log.isTraceEnabled()) {
        log.trace("swapped crossingWeight:{}", wv);
      }
      // put them back unswapped
      swap(downstreamLayer, j, j + 1);

      if (vw > wv) {
        // if the swapped weight is lower, swap them and save off the better
        swap(downstreamLayer, j, j + 1);
        if (wv == 0) {
          break;
        }
        //      } else {
        //        crossCount = Math.min(crossCount, vw);
      }
    }
    log.trace("crossCount  {}", crossCount);
    updatePositions(downstreamLayer);

    return crossCount;
  }

  Set<LE<V, E>> getEdgesThatCrossVirtualEdge(
      Set<LE<V, E>> virtualEdges, List<LE<V, E>> biLayerEdges) {
    Set<Integer> virtualEdgeIndices = new HashSet<>();
    for (LE<V, E> edge : virtualEdges) {
      virtualEdgeIndices.add(edge.getSource().getIndex());
      virtualEdgeIndices.add(edge.getTarget().getIndex());
    }
    Set<LE<V, E>> typeOneConflictEdges = new HashSet<>();
    for (LE<V, E> edge : biLayerEdges) {
      if (edge instanceof VirtualEdge) continue;
      List<Integer> sortedIndices = new ArrayList<>();
      sortedIndices.add(edge.getSource().getIndex());
      sortedIndices.add(edge.getTarget().getIndex());
      Collections.sort(sortedIndices);
      for (int virtualIndex : virtualEdgeIndices) {
        int idxZero = sortedIndices.get(0);
        int idxOne = sortedIndices.get(1);
        if (idxZero <= virtualIndex && virtualIndex < idxOne) {
          typeOneConflictEdges.add(edge);
        }
      }
    }
    return typeOneConflictEdges;
  }

  private boolean isEmptyContainer(LV<V> v) {
    return v instanceof Container && ((Container<V>) v).size() == 0;
  }

  protected static <V, E> List<LE<V, E>> swapEdgeEndpoints(List<LE<V, E>> list) {
    return list.stream()
        .map(e -> LE.of(e.getEdge(), e.getTarget(), e.getSource()))
        .collect(Collectors.toList());
    //    return list.stream().map(LE::swapped).collect(Collectors.toList());
  }

  private int crossingCount(List<LE<V, E>> edges) {
    edges.sort(Comparators.biLevelEdgeComparator());
    List<Integer> targetIndices = new ArrayList<>();
    for (LE<V, E> edge : edges) {
      targetIndices.add(edge.getTarget().getIndex());
    }
    return InsertionSortCounter.insertionSortCounter(targetIndices);
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
    updatePositions(array);
  }

  /**
   * In the sixth step we perform a scan on L i+1 and insert empty containers between two
   * consecutive vertices, and call join(S 1 , S 2 ) on two consecutive containers in the list. This
   * ensures that L i+1 is an alternating layer.
   *
   * @param downstreamLayer
   */
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
   * @param v the vertex to get a segment for
   * @param <V> vertex type
   * @return the segment for v or else v
   */
  static <V> LV<V> s(LV<V> v) {
    if (v instanceof SegmentVertex) {
      SegmentVertex<V> pVertex = (SegmentVertex<V>) v;
      return pVertex.getSegment();
    } else {
      return v;
    }
  }

  /**
   * update the positions so that the preceding container size is used
   *
   * @param layer
   */
  static <V> boolean updatePositions(List<LV<V>> layer) {
    boolean changed = false;
    int currentPos = 0;
    for (LV<V> v : layer) {
      if (v instanceof Container && ((Container<V>) v).size() == 0) {
        continue;
      }
      if (v.getPos() != currentPos) {
        changed = true;
      }
      v.setPos(currentPos);
      if (v instanceof Container) {
        currentPos += ((Container<V>) v).size();
      } else {
        currentPos++;
      }
    }
    return changed;
  }

  void assignPositions(List<LV<V>> currentLayer) {
    LV<V> previousVertex = null;
    Container<V> previousContainer = null;
    for (int i = 0; i < currentLayer.size(); i++) {
      LV<V> v = currentLayer.get(i);

      if (i % 2 == 0) {
        // this is a container
        Container<V> container = (Container<V>) v;
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
        .map(v -> (Container<V>) v)
        .filter(c -> c.size() > 0)
        .forEach(
            c -> {
              double measure = c.getPos();
              c.setMeasure(measure);
            });

    for (LV<V> v : downstreamLayer) {
      if (splitVertexPredicate.test(v)) { // QVertex for top to bottom
        continue;
      }
      if (v instanceof Container) {
        Container<V> container = (Container<V>) v;
        double measure = container.getPos();
        container.setMeasure(measure);
      } else {
        // not a container (nor QVertex for top to bottom)
        // measure will be related to the median of the pos of predecessor vert
        Set<LV<V>> neighbors = neighborFunction.apply(v);
        int[] poses = new int[neighbors.size()];
        int i = 0;
        for (LV<V> neighbor : neighbors) {
          poses[i++] = neighbor.getPos();
        }
        //        IntStream.range(0, poses.length).forEach(idx -> poses[idx] = neighbors.get(idx).getPos());
        if (poses.length > 0) {
          int measure = medianValue(poses); // poses will be sorted in medianValue method
          v.setMeasure(measure);
        } else {
          // leave the measure as as the current pos
          if (v.getPos() < 0) {
            log.debug("no pos for {}", v);
          }
          double measure = v.getPos();
          v.setMeasure(measure);
        }
      }
    }
  }

  /**
   * return the median value in the array P (which is Sorted!)
   *
   * @param P a sorted array
   * @return the median value
   */
  static int medianValue(int[] P) {
    if (P.length == 0) {
      return -1;
    } else if (P.length == 1) {
      return P[0];
    }
    Arrays.sort(P);
    int m = P.length / 2;
    if (P.length % 2 == 1) {
      return P[m];
    } else if (P.length == 2) {
      return (P[0] + P[1]) / 2;
    } else {
      int left = P[m - 1] - P[0];
      int right = P[P.length - 1] - P[m];
      return (P[m - 1] * right + P[m] * left) / (left + right);
    }
  }
}
