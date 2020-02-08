package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Comparators;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.util.InsertionSortCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EiglspergerUtil {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerUtil.class);

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

    Container<V, Segment<V>> firstContainer = Container.createSubContainer();
    firstContainer.append(segment);
    if (log.isTraceEnabled()) log.trace("added segment {} for {}", segment, segmentVertex);
    // the firstContainer is the first item in the outList
    // the PVertex is not added to the outList
    wasEmptyList.add(firstContainer);
    if (log.isTraceEnabled()) log.trace("wasEmptyContainer now {}", firstContainer);

    return wasEmptyList; // which is no longer empty
  }

  static <V> List<LV<V>> addRegularVertexToEmptyList(List<LV<V>> wasEmptyList, LV<V> vertex) {
    wasEmptyList.add(vertex);
    return wasEmptyList; // which is no longer empty
  }

  static <V> List<LV<V>> addContainerToEmptyList(
      List<LV<V>> wasEmptyList, Container<V, Segment<V>> container) {
    wasEmptyList.add(container);
    return wasEmptyList; // which is no longer empty
  }

  // non-empty list
  static <V> List<LV<V>> addSegmentVertexToNonEmptyList(
      List<LV<V>> notEmptyList, SegmentVertex<V> segmentVertex) {
    LV<V> lastItem = notEmptyList.get(notEmptyList.size() - 1);
    // if the lastItem is a container, append the PVertex's Segment to the container
    if (lastItem instanceof Container) {
      Container<V, Segment<V>> lastContainer = (Container<V, Segment<V>>) lastItem;
      Segment<V> segment = segmentVertex.getSegment();
      if (log.isTraceEnabled()) log.trace("added segment {} for {}", segment, segmentVertex);
      lastContainer.append(segment);
      if (log.isTraceEnabled()) log.trace("lastContainer now {}", lastContainer);
    }
    return notEmptyList;
  }

  static <V> List<LV<V>> addRegularVertexToNonEmptyList(List<LV<V>> notEmptyList, LV<V> vertex) {
    notEmptyList.add(vertex);
    return notEmptyList;
  }

  static <V> List<LV<V>> addContainerToNonEmptyList(
      List<LV<V>> notEmptyList, Container<V, Segment<V>> container) {
    LV<V> lastItem = notEmptyList.get(notEmptyList.size() - 1);
    // if the lastItem is a container, join the incoming container with it
    if (lastItem instanceof Container) {
      Container<V, Segment<V>> lastContainer = (Container<V, Segment<V>>) lastItem;
      if (log.isTraceEnabled()) log.trace("join {} with {}", lastContainer, container);
      lastContainer.join(container);
      if (log.isTraceEnabled()) log.trace("lastContainer now {}", lastContainer);
    } else {
      // the last item is some kind of vertex, just add the container to the list
      notEmptyList.add(container);
    }
    return notEmptyList;
  }

  static <V> List<LV<V>> assignIndices(List<LV<V>> inList) {
    int i = 0;
    for (LV<V> v : inList) {
      if (v instanceof Container && ((Container<V, Segment<V>>) v).size() == 0) {
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
        if (v instanceof Container) {
          outList.add(v);
        } else {
          outList.add(Container.createSubContainer());
          outList.add(v);
        }

      } else {
        LV<V> previous = outList.get(outList.size() - 1);
        if (previous instanceof Container && v instanceof Container) {
          // join them
          if (log.isTraceEnabled()) log.trace("joining {} and {}", previous, v);
          Container<V, Segment<V>> previousContainer = (Container<V, Segment<V>>) previous;
          Container<V, Segment<V>> thisContainer = (Container<V, Segment<V>>) v;
          previousContainer.join(thisContainer);
          if (log.isTraceEnabled()) log.trace("previous now joined as {}", previous);

          // previous container is already in the outList
        } else if (!(previous instanceof Container) && !(v instanceof Container)) {
          // ad empty container between 2 non containers
          if (log.isTraceEnabled())
            log.trace("added empty container between {} and {}", previous, v);
          outList.add(Container.createSubContainer());
          outList.add(v);
        } else {
          outList.add(v);
        }
      }
    }
    if (!(outList.get(outList.size() - 1) instanceof Container)) {
      outList.add(Container.createSubContainer());
      if (log.isTraceEnabled()) log.trace("appended empty container");
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
          //          if (biLayer.splitVertexPredicate.test(v)) {
          //            ((SegmentVertex<V>) v).setSegmentVertexPos(pos);
          //          }
        }
        previousVertex = v;
      }
    }

    if (log.isTraceEnabled()) {
      log.trace(
          "S2 currentLayer with pos values:\n{}",
          EiglspergerSteps.elementStringer(biLayer, currentLayer));
    }
  }

  static <V, E> int crossingCount(List<LE<V, E>> edges) {
    Comparator<LE<V, E>> biLevelEdgeComparator = Comparators.biLevelEdgeComparator();
    edges.sort(biLevelEdgeComparator);
    List<Integer> targetIndices = new ArrayList<>();
    int weight = 1;
    for (LE<V, E> edge : edges) {
      if (edge.getSource() instanceof Container && edge.getTarget() instanceof Container) {
        continue;
      }
      if (edge.getSource() instanceof Container) { // && !(edge.getTarget() instanceof Container)) {
        //        weight += ((Container<V, Segment<V>>) edge.getSource()).size();
      } else if (edge.getTarget()
          instanceof Container) { // && !(edge.getSource() instanceof Container)) {
        //        weight += ((Container<V, Segment<V>>) edge.getTarget()).size();
      }
      targetIndices.add(edge.getTarget().getIndex());
    }
    return weight * InsertionSortCounter.insertionSortCounter(targetIndices);
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

  public static <V> void check(LV<V>[][] layers) {
    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        LV<V> v = layers[i][j];
        if (v.getIndex() != j) {
          log.error("{} needs fix", v);
        }
      }
    }
  }

  static <V, E> void assignMeasures(BiLayer<V, E> biLayer, Graph<LV<V>, LE<V, E>> graph) {

    List<LV<V>> currentLayer = biLayer.currentLayer;
    List<LV<V>> downstreamLayer = biLayer.downstreamLayer;

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
      if (biLayer.splitVertexPredicate.test(v)) { // QVertex for top to bottom
        continue;
      }
      if (v instanceof Container) {
        Container<V, Segment<V>> container = (Container<V, Segment<V>>) v;
        double measure = container.getPos();
        container.setMeasure(measure);
      } else {
        // not a container (nor QVertex for top to bottom)
        // measure will be related to the median of the pos of predecessor vert
        List<LV<V>> neighbors = biLayer.neighborFunction.apply(graph, v);
        int[] poses = new int[neighbors.size()];
        IntStream.range(0, poses.length).forEach(idx -> poses[idx] = neighbors.get(idx).getPos());
        //        neighbors.sort(Comparator.comparingInt(LV::getIndex));
        //        int[] neighborIndices = biLayer.neighborIndexFunction.apply(graph, v);
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

    if (log.isTraceEnabled()) {
      log.trace(
          "S2 downstreamLayer with measure values:\n{}",
          EiglspergerSteps.elementStringer(biLayer, downstreamLayer));
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
