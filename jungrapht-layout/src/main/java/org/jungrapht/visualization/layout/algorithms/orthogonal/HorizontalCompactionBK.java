package org.jungrapht.visualization.layout.algorithms.orthogonal;

import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.HDirection;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.HDirection.LtoR;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.HDirection.RtoL;
import static org.jungrapht.visualization.layout.algorithms.eiglsperger.VerticalAlignment.VDirection;

import java.util.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 */
public class HorizontalCompactionBK<V, E> {

  private static final Logger log = LoggerFactory.getLogger(HorizontalCompactionBK.class);

  protected List<Rectangle> layers;

  protected Graph<V, E> svGraph;
  protected Graph<Rectangle, Integer> compactionGraph;
  protected final HDirection hDirection;
  protected final VDirection vDirection;
  protected Map<Rectangle, Rectangle> rootMap;
  protected Map<Rectangle, Rectangle> alignMap;
  protected int deltaX;
  protected int deltaY;

  protected Map<Rectangle, Rectangle> sink = new HashMap<>();
  protected Map<Rectangle, Integer> shift = new HashMap<>();
  protected Map<Rectangle, Integer> x = new HashMap<>();
  protected Map<Rectangle, Integer> y = new HashMap<>();

  public HorizontalCompactionBK(
      HDirection hDirection,
      VDirection vDirection,
      Graph<Rectangle, Integer> compactionGraph,
      List<Rectangle> layers,
      Map<Rectangle, Rectangle> rootMap,
      Map<Rectangle, Rectangle> alignMap,
      int deltaX,
      int deltaY) {
    this.layers = layers;
    this.hDirection = hDirection;
    this.vDirection = vDirection;
    this.compactionGraph = compactionGraph;
    this.rootMap = rootMap;
    this.alignMap = alignMap;
    this.deltaX = deltaX;
    this.deltaY = deltaY;

    layers.forEach(
        v -> {
          sink.put(v, v);
          shift.put(v, Integer.MAX_VALUE);
          x.put(v, (int) v.x);
          y.put(v, (int) v.y);
        });
  }

  public void horizontalCompaction() {

    if (log.isTraceEnabled()) {
      layers
          .stream()
          .forEach(v -> log.trace("v:{}, root(v):{} equal: {}", v, root(v), (v == root(v))));
      log.trace("compactionGraph vertices: {}", compactionGraph.vertexSet());
    }

    Set<Rectangle> verticesInCompactionGraphAndSegmentEnds = new HashSet<>();
    for (Rectangle v : compactionGraph.vertexSet()) {
      Rectangle root = root(v);
      if (root == v) {
        verticesInCompactionGraphAndSegmentEnds.add(v);
      }
    }
    if (log.isTraceEnabled()) {
      log.trace(
          "verticesInCompactionGraphAndSegmentEnds = {}", verticesInCompactionGraphAndSegmentEnds);
    }
    for (Rectangle v : compactionGraph.vertexSet()) {
      Rectangle root = root(v);
      if (root == v) {
        if (log.isTraceEnabled()) {
          log.trace("(v) will placeBlock({})", v);
        }
        placeBlock(v);
      }
    }

    for (Rectangle v : layers) {
      // x[v] <- x[root[v]]
      x(v, x(root(v)));
      y(v, (int) v.y * deltaY); // 'i' is the rank
      // if shift[sink[root[v]]] < infinity
      if (shift(sink(root(v))) < Integer.MAX_VALUE) {
        // x[v] <- x[v] + shift[sink[root[v]]]
        x(v, x(v) + shift(sink(root(v))));
      }
    }
  }

  protected void placeBlock(Rectangle v) {
    //        log.trace("placeBlock: {} and x.containsKey(v) is {}", v, x.containsKey(v));
    // if x[v] undefined
    if (!x.containsKey(v)) {
      // x[v] <- 0, w <- v
      x(v, 0);
      Rectangle w = v;
      do {
        if (log.isTraceEnabled()) {
          log.trace("look for predecessor of {}", w);
        }
        Rectangle cw;
        cw = w;
        if (compactionGraph.containsVertex(cw)) {
          if (log.isTraceEnabled()) {
            log.trace("inDegree of {} is {}", cw, compactionGraph.inDegreeOf(cw));
            log.trace("outDegree of {} is {}", cw, compactionGraph.outDegreeOf(cw));
          }
          if ((hDirection == LtoR && compactionGraph.inDegreeOf(cw) > 0)
              || (hDirection == RtoL && compactionGraph.outDegreeOf(cw) > 0)) {
            var edges =
                hDirection == LtoR
                    ? compactionGraph.incomingEdgesOf(cw)
                    : compactionGraph.outgoingEdgesOf(cw);

            for (var edge : edges) {
              Rectangle u;
              Rectangle pred =
                  hDirection == LtoR
                      ? compactionGraph.getEdgeSource(edge)
                      : compactionGraph.getEdgeTarget(edge);
              u = root(pred);
              if (log.isTraceEnabled()) {
                log.trace("(u) will placeBlock({})", u);
              }
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
        w = align(w);
      } while (w != v);
    }
  }

  public Map<Rectangle, Integer> getXMap() {
    return x;
  }

  //  protected int pos(Rectangle v) {
  //    return (int)v.y;
  //  }
  //
  //  protected int idx(Rectangle v) {
  //    return (int)v.y;
  //  }

  void checkValuesInLayersForSameX(Rectangle[][] layers) {
    for (Rectangle[] layer : layers) {
      Map<Integer, Rectangle> xValuesThisLayer = new HashMap<>();
      for (Rectangle v : layer) {
        int x = x(v);
        if (xValuesThisLayer.containsKey(x)) {
          if (log.isTraceEnabled()) {
            log.trace("already seen {} in this layer {}", x, layer);
          }
        } else {
          xValuesThisLayer.put(x, v);
        }
      }
    }
  }

  protected Rectangle sink(Rectangle v) {
    return sink.get(v);
  }

  protected void sink(Rectangle k, Rectangle v) {
    sink.put(k, v);
  }

  protected int shift(Rectangle v) {
    return shift.get(v);
  }

  protected void shift(Rectangle k, int v) {
    shift.put(k, v);
  }

  protected int x(Rectangle v) {
    //    return (int)v.x;
    return x.get(v);
  }

  protected void x(Rectangle v, int d) {
    //    v. = d;
    x.put(v, d);
  }

  protected int y(Rectangle v) {
    return y.get(v);
  }

  protected void y(Rectangle v, int d) {
    y.put(v, d);
  }

  protected Rectangle root(Rectangle v) {
    return rootMap.get(v);
  }

  protected void root(Rectangle k, Rectangle v) {
    rootMap.put(k, v);
  }

  protected Rectangle align(Rectangle v) {
    return alignMap.get(v);
  }

  protected void align(Rectangle k, Rectangle v) {
    alignMap.put(k, v);
  }
}
