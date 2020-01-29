package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.util.InsertionSortCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubEiglspergerUtil {

  private static final Logger log = LoggerFactory.getLogger(SubEiglspergerUtil.class);

  /**
   * Creates and returns a list of the vertices in a rank of the sparse layering array.<br>
   * No Containers are inserted in the list, they will be inserted as needed in stepSix of the
   * algorithm
   *
   * @param rank one rank of a sparse layering 2d array
   * @param <V> vertex type
   * @return a {@link List} containing the vertices (LV, PVertex, QVertex) from the supplied rank.
   */
  public static <V> List<LV<V>> createListOfVertices(LV<V>[] rank) {
    return Arrays.stream(rank).collect(Collectors.toList());
  }

  // empty list
  static <V> List<LV<V>> addSegmentVertexToEmptyList(
      List<LV<V>> wasEmptyList, SegmentVertex<V> segmentVertex) {
    // cast the vertex, get its segment, append it to a newly
    // created Container, add the Container to the wasEmptyList and
    // return the wasEmptyList
    Segment<V> segment = segmentVertex.getSegment();
    // clear out previous Container data from the segment
    //    segment.initialize();
    SubContainer<V, Segment<V>> firstContainer = SubContainer.createSubContainer();
    firstContainer.append(segment);

    //    SubContainer<V, Segment<V>> iost = SubContainer.createSubContainer();
    //    iost.append(segment);
    log.info("added segment {} for {}", segment, segmentVertex);
    // the firstContainer is the first item in the outList
    // the PVertex is not added to the outList
    wasEmptyList.add(firstContainer);
    log.info("wasEmptyContainer now {}", firstContainer);

    return wasEmptyList; // which is no longer empty
  }

  static <V> List<LV<V>> addRegularVertexToEmptyList(List<LV<V>> wasEmptyList, LV<V> vertex) {
    wasEmptyList.add(vertex);
    return wasEmptyList; // which is no longer empty
  }

  static <V> List<LV<V>> addContainerToEmptyList(
      List<LV<V>> wasEmptyList, SubContainer<V, Segment<V>> container) {
    wasEmptyList.add(container);
    return wasEmptyList; // which is no longer empty
  }

  // non-empty list
  static <V> List<LV<V>> addSegmentVertexToNonEmptyList(
      List<LV<V>> notEmptyList, SegmentVertex<V> segmentVertex) {
    LV<V> lastItem = notEmptyList.get(notEmptyList.size() - 1);
    // if the lastItem is a container, append the PVertex's Segment to the container
    if (lastItem instanceof SubContainer) {
      SubContainer<V, Segment<V>> lastContainer = (SubContainer<V, Segment<V>>) lastItem;
      Segment<V> segment = segmentVertex.getSegment();
      // clear out previous Container data from the segment
      //      segment.initialize();
      log.info("added segment {} for {}", segment, segmentVertex);
      lastContainer.append(segment);
      log.info("lastContainer now {}", lastContainer);
    }
    return notEmptyList;
  }

  static <V> List<LV<V>> addRegularVertexToNonEmptyList(List<LV<V>> notEmptyList, LV<V> vertex) {
    notEmptyList.add(vertex);
    return notEmptyList;
  }

  static <V> List<LV<V>> addContainerToNonEmptyList(
      List<LV<V>> notEmptyList, SubContainer<V, Segment<V>> container) {
    LV<V> lastItem = notEmptyList.get(notEmptyList.size() - 1);
    // if the lastItem is a container, join the incoming container with it
    if (lastItem instanceof SubContainer) {
      SubContainer<V, Segment<V>> lastContainer = (SubContainer<V, Segment<V>>) lastItem;
      log.info("join {} with {}", lastContainer, container);
      lastContainer.join(container);
      log.info("lastContainer now {}", lastContainer);
    } else {
      // the last item is some kind of vertex, just add the container to the list
      notEmptyList.add(container);
    }
    return notEmptyList;
  }

  static <V> List<LV<V>> assignIndices(List<LV<V>> inList) {
    int i = 0;
    for (LV<V> v : inList) {
      if (v instanceof SubContainer && ((SubContainer<V, Segment<V>>) v).size() == 0) {
        continue;
      }
      v.setIndex(i++);
    }
    //    IntStream.range(0, inList.size()).forEach(i -> inList.get(i).setIndex(i));
    return inList;
  }

  /**
   * Iterate over the supplied list, considering each Duo in the list.<br>
   * If any Duo has a null container, give it an empty container.<br>
   * If there is a previous Duo from the iteration, and if that previous Duo's vertex is null, then
   * join this Duo's container to the previous Duo's container and set the previous Duo's vertex to
   * this Duo's vertex, then remove this Duo from the list: i.e. collapse this Duo into the previous
   * one by joining the containers.
   *
   * @param list
   * @param <V>
   * @return
   */
  static <V> List<LV<V>> scan(List<LV<V>> list) {
    List<LV<V>> outList = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      LV<V> v = list.get(i);
      if (outList.isEmpty()) {
        if (v instanceof SubContainer) {
          outList.add(v);
        } else {
          outList.add(SubContainer.createSubContainer());
          outList.add(v);
        }

      } else {
        LV<V> previous = outList.get(outList.size() - 1);
        if (previous instanceof SubContainer && v instanceof SubContainer) {
          // join them
          log.info("joining {} and {}", previous, v);
          SubContainer<V, Segment<V>> previousContainer = (SubContainer<V, Segment<V>>) previous;
          SubContainer<V, Segment<V>> thisContainer = (SubContainer<V, Segment<V>>) v;
          previousContainer.join(thisContainer);
          log.info("previous now joined as {}", previous);

          // previous container is already in the outList
        } else if (!(previous instanceof SubContainer) && !(v instanceof SubContainer)) {
          // ad empty container between 2 non containers
          log.info("added empty container between {} and {}", previous, v);
          outList.add(SubContainer.createSubContainer());
          outList.add(v);
        } else {
          outList.add(v);
        }
      }
    }
    if (!(outList.get(outList.size() - 1) instanceof SubContainer)) {
      outList.add(SubContainer.createSubContainer());
      log.info("appended empty container");
    }
    return assignIndices(outList);
  }

  public static int decrement(int i) {
    return i - 1;
  }

  public static int increment(int i) {
    return i + 1;
  }

  /**
   * used in stepTwo to assign position values for
   *
   * @param biLayer an alternating layer // * @param pos
   * @param <V>
   */
  static <V, E> void assignPositions(BiLayer<V, E> biLayer) {
    List<LV<V>> currentLayer = biLayer.currentLayer;
    LV<V> previousVertex = null;
    SubContainer<V, Segment<V>> previousContainer = null;
    for (int i = 0; i < currentLayer.size(); i++) {
      LV<V> v = currentLayer.get(i);

      if (i % 2 == 0) {
        // this is a container
        SubContainer<V, Segment<V>> container = (SubContainer<V, Segment<V>>) v;
        if (container.size() > 0) {
          if (previousContainer == null) {
            // first container non empty
            biLayer.pos.put(container, 0);
          } else {
            // there has to be a previousVertex
            biLayer.pos.put(container, biLayer.pos.get(previousVertex) + 1);
          }
        }
        previousContainer = container;
      } else {
        // this is a vertex
        if (previousVertex == null) {
          // first vertex (position 1)
          biLayer.pos.put(v, previousContainer.size());
        } else {
          biLayer.pos.put(v, biLayer.pos.get(previousVertex) + previousContainer.size() + 1);
        }
        previousVertex = v;
      }

      //      if (v instanceof Container) {
      //        Container<V> container = (Container<V>) v;
      //        // reject empty containers
      //        if (container.size() == 0) {
      //          continue;
      //        }
      //        // first item in layerI
      //        if (i == 0) {
      //          //          pos.put(container, 0);
      ////          biLayer.pos.put(container, 0);
      //          biLayer.pos.put(container, 0);
      //        } else {
      //          LV<V> prev = currentLayer.get(i - 1);
      //          if (prev instanceof Container) {
      //            throw new RuntimeException("2 consecutive Containers should not happen");
      //          }
      //          //          pos.put(container, pos.get(prev) + 1);
      ////          container.setPos(prev.getPos() + 1);
      //          biLayer.pos.put(container, biLayer.pos.get(prev) + 1);
      //        }
      //      } else {
      //        // v is just a vertex
      //        if (i == 0) {
      //          //          pos.put(v, 0);
      ////          v.setPos(0);
      //          biLayer.pos.put(v, 0);
      //        } else if (i == 1) {
      //          // this is Vi0
      //          //          pos.put(v, ((Container<V>) layerI.get(0)).size());
      ////          v.setPos(((Container<V>) currentLayer.get(0)).size());
      //          biLayer.pos.put(v, ((Container<V>) currentLayer.get(0)).size());
      //        } else {
      //          // i had better be at least 3
      //          //          pos.put(v, pos.get(layerI.get(i - 2)) + ((Container<V>) layerI.get(i - 1)).size() + 1);
      ////          v.setPos(currentLayer.get(i - 2).getPos() + ((Container<V>) currentLayer.get(i - 1)).size() + 1);
      //          biLayer.pos.put(v, biLayer.pos.get(currentLayer.get(i - 2)) + ((Container<V>) currentLayer.get(i - 1)).size() + 1);
      //        }
      //      }
    }
  }

  //  public static <V, E> int crossing(
  //      LV<V> v, LV<V> w, List<LE<V, E>> edges) {
  //
  //    List<Integer> targetIndices = new ArrayList<>();
  //    for (LE<V, E> edge : edges) {
  //      if (edge.getSource() instanceof PVertex) {
  //        continue;
  //      }
  //      if (edge.getSource().equals(v) || edge.getSource().equals(w)) {
  //
  //        int weight = 1;
  //        if (edge.getSource() instanceof Container) {// && !(edge.getTarget() instanceof Container)) {
  //          weight = ((Container<V>) edge.getSource()).size();
  //        } else if (edge.getTarget() instanceof Container) {// && !(edge.getSource() instanceof Container)) {
  //          weight = ((Container<V>) edge.getTarget()).size();
  //        }
  //        targetIndices.add(weight * edge.getTarget().getIndex());
  //      }
  //    }
  //    return InsertionSortCounter.insertionSortCounter(targetIndices);
  //  }

  static Comparator<LE> sourceIndexComparator =
      Comparator.comparingInt(e -> e.getSource().getIndex());
  static Comparator<LE> targetIndexComparator =
      Comparator.comparingInt(e -> e.getTarget().getIndex());
  static Comparator<LE> biLevelEdgeComparator =
      sourceIndexComparator.thenComparing(targetIndexComparator);

  static <V, E> int crossingCount(List<LE<V, E>> edges) {
    edges.sort(biLevelEdgeComparator);
    List<Integer> targetIndices = new ArrayList<>();
    for (LE<V, E> edge : edges) {
      int weight = 1;
      if (edge.getSource() instanceof SubContainer && edge.getTarget() instanceof SubContainer) {
        continue;
      }
      if (edge.getSource()
          instanceof SubContainer) { // && !(edge.getTarget() instanceof Container)) {
        weight = ((SubContainer<V, Segment<V>>) edge.getSource()).size();
      } else if (edge.getTarget()
          instanceof SubContainer) { // && !(edge.getSource() instanceof Container)) {
        weight = ((SubContainer<V, Segment<V>>) edge.getTarget()).size();
      }
      targetIndices.add(weight * edge.getTarget().getIndex());
    }
    return InsertionSortCounter.insertionSortCounter(targetIndices);
  }

  static <V, E> int crossingCountSwapped(int i, int j, List<LV<V>> layer, List<LE<V, E>> edges) {
    swap(layer, i, j);
    int count = crossingCount(edges);
    swap(layer, i, j);
    return count;
  }

  private static <V> void swap(LV<V>[] array, int i, int j) {
    LV<V> temp = array[i];
    array[i] = array[j];
    array[j] = temp;
    array[i].setIndex(i);
    array[j].setIndex(j);
  }

  static <V> void swap(List<LV<V>> list, int i, int j) {
    Collections.swap(list, i, j);
    list.get(i).setIndex(i);
    list.get(j).setIndex(j);
  }

  static <V> List<LV<V>> fixIndices(List<LV<V>> layer) {
    IntStream.range(0, layer.size()).forEach(i -> layer.get(i).setIndex(i));
    return layer;
  }

  static <V> LV<V>[] fixIndices(LV<V>[] layer) {
    IntStream.range(0, layer.length).forEach(i -> layer[i].setIndex(i));
    return layer;
  }

  static <V> void check(LV<V>[][] layers) {
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        LV<V> v = layers[i][j];
        if (v.getIndex() != j) {
          log.info("{} needs fix", v);
        }
      }
    }
  }

  static <V, E> void assignMeasures(BiLayer<V, E> biLayer, Graph<LV<V>, LE<V, E>> graph) {

    List<LV<V>> currentLayer = biLayer.currentLayer;
    List<LV<V>> downstreamLayer = biLayer.downstreamLayer;

    downstreamLayer
        .stream()
        .filter(v -> v instanceof SubContainer)
        .map(v -> (SubContainer<V, Segment<V>>) v)
        .filter(c -> c.size() > 0)
        .forEach(c -> biLayer.measure.put(c, biLayer.pos.get(c)));

    for (int i = 0; i < downstreamLayer.size(); i++) {
      LV<V> v = downstreamLayer.get(i);
      if (biLayer.splitVertexPredicate.test(v)) { // QVertex for top to bottom
        continue;
      }
      if (v instanceof SubContainer) {
        SubContainer<V, Segment<V>> container = (SubContainer<V, Segment<V>>) v;
        biLayer.measure.put(container, biLayer.pos.get(container));
      } else {
        // not a container (nor QVertex for top to bottom)
        // measure will be related to the median of the pos of predecessor vert
        List<LV<V>> neighbors = biLayer.neighborFunction.apply(graph, v);
        int[] poses = new int[neighbors.size()];
        IntStream.range(0, poses.length)
            .forEach(idx -> poses[idx] = biLayer.pos.get(neighbors.get(idx)));
        //        neighbors.sort(Comparator.comparingInt(LV::getIndex));
        //        int[] neighborIndices = biLayer.neighborIndexFunction.apply(graph, v);
        Arrays.sort(poses);
        if (poses.length > 0) {
          biLayer.measure.put(v, poses[(poses.length - 1) / 2]);
        } else {
          // leave the measure as as the current pos
          biLayer.measure.put(v, biLayer.pos.getOrDefault(v, 0));
          // stop and look at where to put it
          //          log.info("no neighbors for {}", v);
        }
      }
    }
  }

  //  static <V, E> void assignMeasures(
  //      List<LV<V>> layerI,
  //      List<LV<V>> arrayIplus1,
  //      Graph<LV<V>, LE<V, E>> graph,
  //      Predicate<LV<V>> vertexPredicate,
  //      BiFunction<
  //              Graph<LV<V>, LE<V, E>>, LV<V>,
  //              List<LV<V>>>
  //          neighborFunction) {
  //    // assign the measure to all non-qvertices in layerEyePlus1
  //    //    List<LV<V>> layerIplus1 = new ArrayList<>();
  //    List<Container<V>> containersFromLayerI =
  //        layerI
  //            .stream()
  //            .filter(v -> v instanceof Container)
  //            .map(v -> (Container<V>) v)
  //            .filter(c -> c.size() > 0)
  //            .collect(Collectors.toList());
  //    containersFromLayerI.stream().forEach(c -> c.setMeasure(c.getPos()));
  //    //    layerIplus1.addAll(containersFromLayerI);
  //    for (int i = 0; i < arrayIplus1.size(); i++) {
  //      LV<V> v = arrayIplus1.get(i);
  //      if (vertexPredicate.test(v)) {
  //        continue;
  //      }
  //      if (v instanceof Container) {
  //        Container<V> container = (Container<V>) v;
  //        if (container.getPos() != -1) {
  //          container.setMeasure(container.getPos());
  //        } else {
  //          container.setMeasure(container.getPos());
  //        }
  //      } else {
  //        // not a container (nor PVertex)
  //        List<LV<V>> succ = neighborFunction.apply(graph, v);
  //        //Graphs.successorListOf(graph, v);
  //        for (LV<V> vv : succ) {
  //          if (vv.getPos() == -1) {
  //            log.error("trouble here. pos not set for {}", vv);
  //          }
  //        }
  //        int[] successorIndices =
  //            neighborFunction
  //                .apply(graph, v)
  //                .stream()
  //                .mapToInt(LV::getPos)
  //                .sorted()
  //                .toArray();
  //        //                Graphs.successorListOf(graph, v).stream().mapToInt(pos::get).sorted().toArray();
  //        if (successorIndices.length > 0) {
  //          v.setMeasure(successorIndices[successorIndices.length / 2]);
  //        } else {
  //          v.setMeasure(v.getPos());
  //          //          measure.put(v, v.getIndex()); // if no neighbors, leave the index as is
  //        }
  //      }
  //    }
  //  }

  static <V> String stringify(LV<V>[][] layers) {
    StringBuilder builder = new StringBuilder("\n");
    for (int i = 0; i < layers.length; i++) {
      builder.append(i + ",");
      for (int j = 0; j < layers[i].length; j++) {
        LV<V> v = layers[i][j];
        builder.append(v.getClass().getSimpleName() + ":" + v.getVertex() + ", ");
      }
      builder.append("\n");
    }
    return builder.toString();
  }
  /**
   * return the segment to which v is incident, if v is a PVertex or a QVertex. Otherwise, return v
   *
   * @param v
   * @param <V>
   * @return
   */
  static <V> LV<V> s(LV<V> v) {
    if (v instanceof PVertex) {
      PVertex<V> pVertex = (PVertex) v;
      return pVertex.getSegment();
    } else if (v instanceof QVertex) {
      QVertex qVertex = (QVertex) v;
      return qVertex.getSegment();
    } else {
      return v;
    }
  }
}
