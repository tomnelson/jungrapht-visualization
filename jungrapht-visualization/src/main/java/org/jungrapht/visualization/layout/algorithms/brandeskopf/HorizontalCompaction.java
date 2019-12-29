package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaVertex;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizontalCompaction<V> {

  private static final Logger log = LoggerFactory.getLogger(HorizontalCompaction.class);

  protected SugiyamaVertex<V>[][] layers;
  protected Map<SugiyamaVertex<V>, SugiyamaVertex<V>> rootMap;
  protected Map<SugiyamaVertex<V>, SugiyamaVertex<V>> alignMap;
  int deltaX;
  int deltaY;

  Map<SugiyamaVertex<V>, SugiyamaVertex<V>> sink = new HashMap<>();
  Map<SugiyamaVertex<V>, Integer> shift = new HashMap<>();
  Map<SugiyamaVertex<V>, Integer> x = new HashMap<>();
  Map<SugiyamaVertex<V>, Integer> y = new HashMap<>();

  public HorizontalCompaction(
      SugiyamaVertex<V>[][] layers,
      Map<SugiyamaVertex<V>, SugiyamaVertex<V>> rootMap,
      Map<SugiyamaVertex<V>, SugiyamaVertex<V>> alignMap,
      int deltaX,
      int deltaY) {
    this.layers = layers;
    this.rootMap = rootMap;
    this.alignMap = alignMap;
    this.deltaX = deltaX;
    this.deltaY = deltaY;
    Arrays.stream(layers)
        .flatMap(Arrays::stream)
        //        .stream()
        //        .flatMap(Collection::stream)
        .forEach(
            v -> {
              sink.put(v, v);
              shift.put(v, Integer.MAX_VALUE);
            });
    horizontalCompaction();
  }

  public Point getPoint(SugiyamaVertex<V> v) {
    return Point.of(x.get(v), y.get(v));
  }

  public void horizontalCompaction() {
    Arrays.stream(layers)
        .flatMap(Arrays::stream)
        //    layers.stream().flatMap(Collection::stream)
        .filter(v -> root(v) == v)
        .forEach(this::placeBlock);

    for (int i = 0; i < layers.length; i++) {
      SugiyamaVertex<V>[] list = layers[i];
      for (SugiyamaVertex<V> v : list) {
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

  void placeBlock(SugiyamaVertex<V> v) {
    // if x[v] undefined
    if (!x.containsKey(v)) {
      // x[v] <- 0, w <- v
      x(v, 0);
      SugiyamaVertex<V> w = v;
      do {
        // if pos[w] > 0
        if (pos(w) > 0) {
          // u gets root[pred[w]]
          SugiyamaVertex<V> u = root(pred(w));
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

  private int pos(SugiyamaVertex<V> v) {
    return v.getIndex();
  }

  private SugiyamaVertex<V> sink(SugiyamaVertex<V> v) {
    return sink.get(v);
  }

  private void sink(SugiyamaVertex<V> k, SugiyamaVertex<V> v) {
    sink.put(k, v);
  }

  private int shift(SugiyamaVertex<V> v) {
    return shift.get(v);
  }

  private void shift(SugiyamaVertex<V> k, int v) {
    shift.put(k, v);
  }

  private int x(SugiyamaVertex<V> v) {
    return x.get(v);
  }

  private void x(SugiyamaVertex<V> v, int d) {
    x.put(v, d);
  }

  private int y(SugiyamaVertex<V> v) {
    return y.get(v);
  }

  private void y(SugiyamaVertex<V> v, int d) {
    y.put(v, d);
  }

  private SugiyamaVertex<V> root(SugiyamaVertex<V> v) {
    return rootMap.get(v);
  }

  private void root(SugiyamaVertex<V> k, SugiyamaVertex<V> v) {
    rootMap.put(k, v);
  }

  private SugiyamaVertex<V> align(SugiyamaVertex<V> v) {
    return alignMap.get(v);
  }

  private void align(SugiyamaVertex<V> k, SugiyamaVertex<V> v) {
    alignMap.put(k, v);
  }

  private SugiyamaVertex<V> pred(SugiyamaVertex<V> v) {
    int layerOfV = v.getRank();
    int indexOfV = v.getIndex();
    if (indexOfV < 1) return null;
    SugiyamaVertex<V>[] list = layers[layerOfV];
    return list[indexOfV - 1];
  }
}
