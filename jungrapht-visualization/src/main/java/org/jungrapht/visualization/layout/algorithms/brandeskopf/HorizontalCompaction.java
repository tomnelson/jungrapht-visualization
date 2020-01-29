package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizontalCompaction<V> {

  private static final Logger log = LoggerFactory.getLogger(HorizontalCompaction.class);

  protected LV<V>[][] layers;
  protected Map<LV<V>, LV<V>> rootMap;
  protected Map<LV<V>, LV<V>> alignMap;
  int deltaX;
  int deltaY;

  Map<LV<V>, LV<V>> sink = new HashMap<>();
  Map<LV<V>, Integer> shift = new HashMap<>();
  Map<LV<V>, Integer> x = new HashMap<>();
  Map<LV<V>, Integer> y = new HashMap<>();

  public HorizontalCompaction(
      LV<V>[][] layers,
      Map<LV<V>, LV<V>> rootMap,
      Map<LV<V>, LV<V>> alignMap,
      int deltaX,
      int deltaY) {
    this.layers = layers;
    this.rootMap = rootMap;
    this.alignMap = alignMap;
    this.deltaX = deltaX;
    this.deltaY = deltaY;
    Arrays.stream(layers)
        .flatMap(Arrays::stream)
        .forEach(
            v -> {
              sink.put(v, v);
              shift.put(v, Integer.MAX_VALUE);
            });
    horizontalCompaction();
  }

  public Point getPoint(LV<V> v) {
    return Point.of(x.get(v), y.get(v));
  }

  public void horizontalCompaction() {
    Arrays.stream(layers)
        .flatMap(Arrays::stream)
        .filter(v -> root(v) == v)
        .forEach(this::placeBlock);

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

  void placeBlock(LV<V> v) {
    // if x[v] undefined
    if (!x.containsKey(v)) {
      // x[v] <- 0, w <- v
      x(v, 0);
      LV<V> w = v;
      do {
        // if pos[w] > 0
        if (pos(w) > 0) {
          // u gets root[pred[w]]
          LV<V> u = root(pred(w));
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
            x(v, Math.max(x(v), x(u) + deltaX));
          }
        }
        w = align(w);
      } while (w != v);
    }
  }

  private int pos(LV<V> v) {
    return v.getIndex();
  }

  private LV<V> sink(LV<V> v) {
    return sink.get(v);
  }

  private void sink(LV<V> k, LV<V> v) {
    sink.put(k, v);
  }

  private int shift(LV<V> v) {
    return shift.get(v);
  }

  private void shift(LV<V> k, int v) {
    shift.put(k, v);
  }

  private int x(LV<V> v) {
    return x.get(v);
  }

  private void x(LV<V> v, int d) {
    x.put(v, d);
  }

  private int y(LV<V> v) {
    return y.get(v);
  }

  private void y(LV<V> v, int d) {
    y.put(v, d);
  }

  private LV<V> root(LV<V> v) {
    return rootMap.get(v);
  }

  private void root(LV<V> k, LV<V> v) {
    rootMap.put(k, v);
  }

  private LV<V> align(LV<V> v) {
    return alignMap.get(v);
  }

  private void align(LV<V> k, LV<V> v) {
    alignMap.put(k, v);
  }

  private LV<V> pred(LV<V> v) {
    int layerOfV = v.getRank();
    int indexOfV = v.getIndex();
    if (indexOfV < 1) return null;
    LV<V>[] list = layers[layerOfV];
    return list[indexOfV - 1];
  }
}
