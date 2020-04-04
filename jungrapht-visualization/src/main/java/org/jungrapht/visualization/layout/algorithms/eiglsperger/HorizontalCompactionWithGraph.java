package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.*;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.HDirection.LtoR;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.HDirection.RtoL;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.VDirection.BtoT;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignmentWithCompactionGraph.VDirection.TtoB;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 */
public class HorizontalCompactionWithGraph<V, E> {

  private static final Logger log = LoggerFactory.getLogger(HorizontalCompactionWithGraph.class);

  protected Graph<LV<V>, LE<V, E>> svGraph;
  protected Graph<LV<V>, Integer> compactionGraph;
  protected final HDirection hDirection;
  protected final VDirection vDirection;
  protected LV<V>[][] layers;
  protected Map<LV<V>, LV<V>> rootMap;
  protected Map<LV<V>, LV<V>> alignMap;
  protected int deltaX;
  protected int deltaY;

  protected Map<LV<V>, LV<V>> sink = new HashMap<>();
  protected Map<LV<V>, Integer> shift = new HashMap<>();
  protected Map<LV<V>, Integer> x = new HashMap<>();
  protected Map<LV<V>, Integer> y = new HashMap<>();

  public HorizontalCompactionWithGraph(
      HDirection hDirection,
      VDirection vDirection,
      Graph<LV<V>, LE<V, E>> svGraph,
      Graph<LV<V>, Integer> compactionGraph,
      LV<V>[][] layers,
      Map<LV<V>, LV<V>> rootMap,
      Map<LV<V>, LV<V>> alignMap,
      int deltaX,
      int deltaY) {
    this.hDirection = hDirection;
    this.vDirection = vDirection;
    this.svGraph = svGraph;
    this.compactionGraph = compactionGraph;
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

    if (log.isTraceEnabled()) {
      Arrays.stream(layers)
          .flatMap(Arrays::stream)
          .forEach(v -> log.trace("v:{}, root(v):{} equal: {}", v, root(v), (v == root(v))));
    }

    log.trace("compactionGraph vertices: {}", compactionGraph.vertexSet());

    Set<LV<V>> verticesInCompactionGraphAndSegmentEnds = new HashSet<>();
    for (LV<V> v : compactionGraph.vertexSet()) {
      if (v instanceof Segment) {
        Segment<V> segment = (Segment<V>) v;
        PVertex<V> pVertex = segment.pVertex;
        if (root(pVertex) == pVertex) {
          verticesInCompactionGraphAndSegmentEnds.add(pVertex);
        }
        QVertex<V> qVertex = segment.qVertex;
        if (root(qVertex) == qVertex) {
          verticesInCompactionGraphAndSegmentEnds.add(qVertex);
        }

      } else {
        LV<V> root = root(v);
        if (root == v) {
          verticesInCompactionGraphAndSegmentEnds.add(v);
        }
      }
    }
    log.trace(
        "verticesInCompactionGraphAndSegmentEnds = {}", verticesInCompactionGraphAndSegmentEnds);
    //
    //    for (LV<V>[] layer : layers) {
    //      for (LV<V> v : layer) {
    //        if (v == root(v)) {
    //          placeBlock(v);
    //        }
    //      }
    //    }
    //    Arrays.stream(layers)
    //        .flatMap(Arrays::stream)
    //        .filter(v -> root(v) == v)
    //        .forEach(this::placeBlock);
    for (LV<V> v : compactionGraph.vertexSet()) {
      if (v instanceof Segment) {
        Segment<V> segment = (Segment<V>) v;
        PVertex<V> pVertex = segment.pVertex;
        if (root(pVertex) == pVertex) {
          log.trace("(p) will placeBlock({})", pVertex);
          placeBlock(pVertex);
        }
        QVertex<V> qVertex = segment.qVertex;
        if (root(qVertex) == qVertex) {
          log.trace("(q) will placeBlock({})", qVertex);
          placeBlock(qVertex);
        }

      } else {
        LV<V> root = root(v);
        if (root == v) {
          log.trace("(v) will placeBlock({})", v);
          placeBlock(v);
        }
      }
    }

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
    //        log.trace("placeBlock: {} and x.containsKey(v) is {}", v, x.containsKey(v));
    // if x[v] undefined
    if (!x.containsKey(v)) {
      // x[v] <- 0, w <- v
      x(v, 0);
      LV<V> w = v;
      do {

        log.trace("look for predecessor of {}", w);
        LV<V> cw;
        if (w instanceof SegmentVertex) {
          SegmentVertex<V> sv = (SegmentVertex<V>) w;
          cw = sv.segment;
        } else {
          cw = w;
        }
        if (compactionGraph.containsVertex(cw)) {
          log.trace("inDegree of {} is {}", cw, compactionGraph.inDegreeOf(cw));
          log.trace("outDegree of {} is {}", cw, compactionGraph.outDegreeOf(cw));

          if ((hDirection == LtoR && compactionGraph.inDegreeOf(cw) > 0)
              || (hDirection == RtoL && compactionGraph.outDegreeOf(cw) > 0)) {
            var edges =
                hDirection == LtoR
                    ? compactionGraph.incomingEdgesOf(cw)
                    : compactionGraph.outgoingEdgesOf(cw);

            for (var edge : edges) {
              LV<V> u;
              LV<V> pred =
                  hDirection == LtoR
                      ? compactionGraph.getEdgeSource(edge)
                      : compactionGraph.getEdgeTarget(edge);
              if (pred instanceof Segment) {
                Segment<V> segment = (Segment<V>) pred;
                u = vDirection == TtoB ? root(segment.pVertex) : root(segment.qVertex);

              } else {
                u = root(pred);
              }
              log.trace("(u) will placeBlock({})", u);
              placeBlock(u);
              if (sink(v) == v) {
                sink(v, sink(u));
              }
              if (sink(v) != sink(u)) {
                if (hDirection == LtoR) {
                  shift(sink(u), Math.min(shift(sink(u)), x(v) - x(u) - deltaX));
                } else {
                  shift(sink(u), Math.max(shift(sink(u)), x(u) - x(v) - deltaX));
                }
              } else {
                if (hDirection == LtoR) {
                  x(v, Math.max(x(v), x(u) + deltaX));
                } else {
                  x(v, Math.min(x(v), x(u) - deltaX));
                }
              }
            }
          }
        }
        if (w instanceof Segment) {
          w = vDirection == TtoB ? ((Segment<V>) w).qVertex : ((Segment<V>) w).pVertex;
        } else if (w instanceof PVertex && vDirection == TtoB) {
          w = ((PVertex<V>) w).segment;
        } else if (w instanceof QVertex && vDirection == BtoT) {
          w = ((QVertex<V>) w).segment;
        } else {
          w = align(w);
        }
        //        // if pos[w] > 0
        //        if (hasPredecessor(w)) {
        //          // u gets root[pred[w]]
        //          LV<V> pred = pred(w);
        //          LV<V> u = root(pred);
        //          int diff = pos(w) - pos(pred);
        //          placeBlock(u);
        //          if (sink(v) == v) {
        //            sink(v, sink(u));
        //          }
        //          // if (sink[v] != sink[u]
        //          if (sink(v) != sink(u)) {
        //            // shift[sink[u]] <- min({shift[sink[u]], x[v]-x[u]-delta}
        //            shift(sink(u), Math.min(shift(sink(u)), x(v) - x(u) - deltaX));
        //          } else {
        //            // x[v] <- max{x[v], x[u] + delta}
        //                        int localDeltaX = deltaX + deltaX * (pos(v) - idx(v));
        //            x(v, Math.max(x(v), x(u) + localDeltaX));
        //          }
        //        }
        //        w = align(w);
      } while (w != v);
    }
  }

  protected boolean hasPredecessor(LV<V> v) {
    return v.getIndex() > 0;
  }

  protected int pos(LV<V> v) {
    return v.getPos();
  }

  protected int idx(LV<V> v) {
    return v.getIndex();
  }

  protected LV<V> sink(LV<V> v) {
    return sink.get(v);
  }

  protected void sink(LV<V> k, LV<V> v) {
    sink.put(k, v);
  }

  protected int shift(LV<V> v) {
    return shift.get(v);
  }

  protected void shift(LV<V> k, int v) {
    shift.put(k, v);
  }

  protected int x(LV<V> v) {
    return x.get(v);
  }

  protected void x(LV<V> v, int d) {
    log.trace("put {} at x: {}", v, d);
    x.put(v, d);
  }

  protected int y(LV<V> v) {
    return y.get(v);
  }

  protected void y(LV<V> v, int d) {
    y.put(v, d);
  }

  protected LV<V> root(LV<V> v) {
    return rootMap.get(v);
  }

  protected void root(LV<V> k, LV<V> v) {
    rootMap.put(k, v);
  }

  protected LV<V> align(LV<V> v) {
    return alignMap.get(v);
  }

  protected void align(LV<V> k, LV<V> v) {
    alignMap.put(k, v);
  }

  /**
   * return the predecessor of v in the same rank
   *
   * @param v
   * @return the predecessor of v in the same rank or null if v's index is 0
   */
  protected LV<V> pred(LV<V> v) {
    int layerOfV = v.getRank();
    int indexOfV = v.getIndex();
    if (indexOfV < 1) {
      return null;
    }
    LV<V>[] list = layers[layerOfV];
    return list[indexOfV - 1];
  }

  public Map<LV<V>, LV<V>> getSink() {
    return sink;
  }

  public Map<LV<V>, Integer> getShift() {
    return shift;
  }

  public Map<LV<V>, Integer> getX() {
    return x;
  }

  public Map<LV<V>, Integer> getY() {
    return y;
  }

  void checkValuesInLayersForSameX(LV<V>[][] layers) {
    for (LV<V>[] layer : layers) {
      Map<Integer, LV<V>> xValuesThisLayer = new HashMap<>();
      for (LV<V> v : layer) {
        int x = x(v);
        if (xValuesThisLayer.containsKey(x)) {
          log.info("already seen {} in this layer {}", x, layer);
        } else {
          xValuesThisLayer.put(x, v);
        }
      }
    }
  }
}
