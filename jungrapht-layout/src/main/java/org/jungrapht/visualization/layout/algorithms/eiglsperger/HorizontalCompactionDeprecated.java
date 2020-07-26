package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.Arrays;
import java.util.Map;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 */
public class HorizontalCompactionDeprecated<V>
    extends org.jungrapht.visualization.layout.algorithms.sugiyama.HorizontalCompaction<V> {

  private static final Logger log = LoggerFactory.getLogger(HorizontalCompactionDeprecated.class);

  public HorizontalCompactionDeprecated(
      LV<V>[][] layers,
      Map<LV<V>, LV<V>> rootMap,
      Map<LV<V>, LV<V>> alignMap,
      int deltaX,
      int deltaY) {
    super(layers, rootMap, alignMap, deltaX, deltaY);
  }

  public Point getPoint(LV<V> v) {
    return Point.of(x.get(v), y.get(v));
  }

  public void horizontalCompaction() {
    //    EiglspergerUtil.layerSanityCheck(layers);
    //    log.info("placeBlock for all v where root(v) is v");
    if (log.isTraceEnabled()) {
      Arrays.stream(layers)
          .flatMap(Arrays::stream)
          .forEach(
              v ->
                  log.trace(
                      "v == {} root(v) == {} they're equal = {}", v, root(v), (root(v) == v)));
    }
    Arrays.stream(layers)
        .flatMap(Arrays::stream)
        .filter(v -> root(v) == v)
        .forEach(
            v -> {
              log.trace("(v) will placeBlock({})", v);
              placeBlock(v);
            });

    for (int i = 0; i < layers.length; i++) {
      LV<V>[] list = layers[i];
      for (LV<V> v : list) {
        // x[v] <- x[root[v]]
        x(v, x(root(v)));
        y(v, i * deltaY); // 'i' is the rank
        // if shift[sink[root[v]]] < infinity
        if (shift(sink(root(v))) < Integer.MAX_VALUE) {
          // x[v] <- x[v] + shift[sink[root[v]]]
          x(v, x(v) + shift(sink(root(v))));
        }
      }
    }
  }

  protected void placeBlock(LV<V> v) {
    //    log.info("placeBlock: {} and x.containsKey(v) is {}", v, x.containsKey(v));
    // if x[v] undefined
    if (!x.containsKey(v)) {
      // x[v] <- 0, w <- v
      x(v, 0);
      LV<V> w = v;
      do {
        // if pos[w] > 0
        log.trace("looking for predecessor of {}", w);
        if (hasPredecessor(w)) {
          // u gets root[pred[w]]
          LV<V> pred = pred(w);
          LV<V> u = root(pred);
          int diff = pos(w) - pos(pred);
          log.trace("(u) will placeBlock({})", u);
          placeBlock(u);
          if (sink(v) == v) {
            sink(v, sink(u));
          }
          // if (sink[v] != sink[u]
          if (sink(v) != sink(u)) {
            // shift[sink[u]] <- min({shift[sink[u]], x[v]-x[u]-delta}
            shift(sink(u), Math.min(shift(sink(u)), x(v) - x(u) - deltaX));
          } else {
            // x[v] <- max{x[v], x[u] + delta}
            int localDeltaX = deltaX + deltaX * (pos(v) - idx(v));
            x(v, Math.max(x(v), x(u) + localDeltaX));
          }
        }
        w = align(w);
      } while (w != v);
    }
  }

  protected int pos(LV<V> v) {
    return v.getPos();
  }

  protected int idx(LV<V> v) {
    return v.getIndex();
  }
}
