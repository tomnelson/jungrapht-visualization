package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.NeighborCache;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize for backward sweep (bottom to top)
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class EiglspergerStepsBackward<V, E> extends EiglspergerSteps<V, E> {

  private static final Logger log = LoggerFactory.getLogger(EiglspergerStepsBackward.class);

  public EiglspergerStepsBackward(
      Graph<LV<V>, LE<V, E>> svGraph,
      NeighborCache<LV<V>, LE<V, E>> neighborCache,
      LV<V>[][] layersArray,
      boolean transpose) {
    super(
        svGraph,
        layersArray,
        QVertex.class::isInstance,
        PVertex.class::isInstance,
        svGraph::getEdgeTarget,
        svGraph::getEdgeSource,
        neighborCache::successorsOf,
        EiglspergerSteps::swapEdgeEndpoints,
        transpose);
  }

  public int sweep(LV<V>[][] layersArray) {
    compactionGraph =
        GraphTypeBuilder.<LV<V>, Integer>directed()
            .allowingMultipleEdges(true)
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    if (log.isTraceEnabled())
      log.trace("<<<<<<<<<<<<<<<<<<<<<<<<<< Backward! <<<<<<<<<<<<<<<<<<<<<<<<<<");

    int crossCount = 0;
    if (log.isTraceEnabled()) log.trace("sweepBackwards");
    List<LV<V>> layerEye = null;

    for (int i = layersArray.length - 1; i > 0; i--) {
      if (layerEye == null) {
        layerEye =
            EiglspergerUtil.scan(EiglspergerUtil.createListOfVertices(layersArray[i])); // last rank
        LV<V> pred = null;
        for (LV<V> v : layerEye) {
          if (v instanceof Container) {
            Container<V> container = (Container<V>) v;
            for (Segment<V> segment : container.segments()) {
              compactionGraph.addVertex(segment);
              if (pred != null) {
                compactionGraph.addEdge(pred, segment);
              }
              pred = segment;
            }
          } else if (v instanceof SegmentVertex) {
            SegmentVertex<V> segmentVertex = (SegmentVertex<V>) v;
            //            Segment<V> segment = segmentVertex.getSegment();
            compactionGraph.addVertex(v);
            if (pred != null) {
              compactionGraph.addEdge(pred, v);
            }
            pred = v;
          } else {
            compactionGraph.addVertex(v);
            if (pred != null) {
              compactionGraph.addEdge(pred, v);
            }
            pred = v;
          }
        }
      }

      stepOne(layerEye);
      // handled PVertices by merging them into containers
      if (log.isTraceEnabled()) {
        log.trace("stepOneOut:{}", layerEye);
      }

      List<LV<V>> currentLayer = layerEye;
      List<LV<V>> downstreamLayer = EiglspergerUtil.createListOfVertices(layersArray[i - 1]);

      stepTwo(currentLayer, downstreamLayer);
      if (log.isTraceEnabled()) {
        log.trace("stepTwoOut:{}", downstreamLayer);
      }

      stepThree(downstreamLayer);
      if (log.isTraceEnabled()) {
        log.trace("stepThreeOut:{}", downstreamLayer);
      }
      EiglspergerUtil.fixIndices(downstreamLayer);

      stepFour(downstreamLayer, i - 1);
      if (log.isTraceEnabled()) {
        log.trace("stepFourOut:{}", downstreamLayer);
      }

      if (transpose) {
        crossCount += stepFive(currentLayer, downstreamLayer, i, i - 1);
      }
      stepSix(downstreamLayer);
      LV<V> pred = null;
      for (LV<V> v : downstreamLayer) {
        if (v instanceof Container) {
          Container<V> container = (Container<V>) v;
          List<Segment<V>> segments = container.segments();
          for (Segment<V> segment : segments) {
            compactionGraph.addVertex(segment);
            if (pred != null) {
              compactionGraph.addEdge(pred, segment);
            }
            pred = segment;
          }
        } else if (v instanceof SegmentVertex) {
          SegmentVertex<V> segmentVertex = (SegmentVertex<V>) v;
          Segment<V> segment = segmentVertex.getSegment();
          compactionGraph.addVertex(segment);
          if (pred != null) {
            compactionGraph.addEdge(pred, segment);
          }
          pred = segment;
        } else {
          compactionGraph.addVertex(v);
          if (pred != null) {
            compactionGraph.addEdge(pred, v);
          }
          pred = v;
        }
      }
      if (log.isTraceEnabled()) {
        log.trace("stepSixOut:{}", downstreamLayer);
      }

      Arrays.sort(layersArray[i], Comparator.comparingInt(LV::getIndex));
      EiglspergerUtil.fixIndices(layersArray[i]);
      Arrays.sort(layersArray[i - 1], Comparator.comparingInt(LV::getIndex));
      EiglspergerUtil.fixIndices(layersArray[i - 1]);
      layerEye = downstreamLayer;
    }
    log.debug("sweepBackward crossCount:{}", crossCount);
    return crossCount;
  }
}
