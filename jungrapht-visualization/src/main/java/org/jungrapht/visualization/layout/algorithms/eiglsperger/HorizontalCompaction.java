package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.HDirection;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.HDirection.LtoR;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.HDirection.RtoL;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.VDirection;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.VDirection.BtoT;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.VDirection.TtoB;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 */
public class HorizontalCompaction<V, E>
    extends org.jungrapht.visualization.layout.algorithms.sugiyama.HorizontalCompaction<V> {

  private static final Logger log = LoggerFactory.getLogger(HorizontalCompaction.class);

  protected Graph<LV<V>, LE<V, E>> svGraph;
  protected Graph<LV<V>, Integer> compactionGraph;
  protected final HDirection hDirection;
  protected final VDirection vDirection;

  public HorizontalCompaction(
      HDirection hDirection,
      VDirection vDirection,
      Graph<LV<V>, LE<V, E>> svGraph,
      Graph<LV<V>, Integer> compactionGraph,
      LV<V>[][] layers,
      Map<LV<V>, LV<V>> rootMap,
      Map<LV<V>, LV<V>> alignMap,
      int deltaX,
      int deltaY) {
    super(layers, rootMap, alignMap, deltaX, deltaY);
    this.hDirection = hDirection;
    this.vDirection = vDirection;
    this.svGraph = svGraph;
    this.compactionGraph = compactionGraph;
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
      } while (w != v);
    }
  }

  protected int pos(LV<V> v) {
    return v.getPos();
  }

  protected int idx(LV<V> v) {
    return v.getIndex();
  }

  void checkValuesInLayersForSameX(LV<V>[][] layers) {
    for (LV<V>[] layer : layers) {
      Map<Integer, LV<V>> xValuesThisLayer = new HashMap<>();
      for (LV<V> v : layer) {
        int x = x(v);
        if (xValuesThisLayer.containsKey(x)) {
          log.trace("already seen {} in this layer {}", x, layer);
        } else {
          xValuesThisLayer.put(x, v);
        }
      }
    }
  }
}
