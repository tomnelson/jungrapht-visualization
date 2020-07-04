package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
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

  static <V> List<LV<V>> assignIndices(List<LV<V>> inList) {
    int i = 0;
    for (LV<V> v : inList) {
      if (v instanceof Container && ((Container<V>) v).size() == 0) {
        continue;
      }
      v.setIndex(i++);
    }
    //    IntStream.range(0, inList.size()).forEach(i -> inList.get(i).setIndex(i));
    return inList;
  }

  /**
   * Iterate over the supplied list, creating an alternating list of vertices and Containers
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
          Container<V> previousContainer = (Container<V>) previous;
          Container<V> thisContainer = (Container<V>) v;
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
    if (!outList.isEmpty() && !(outList.get(outList.size() - 1) instanceof Container)) {
      outList.add(Container.createSubContainer());
      if (log.isTraceEnabled()) log.trace("appended empty container");
    }
    return assignIndices(outList);
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
