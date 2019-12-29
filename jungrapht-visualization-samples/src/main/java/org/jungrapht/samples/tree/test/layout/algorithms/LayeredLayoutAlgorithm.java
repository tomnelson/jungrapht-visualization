package org.jungrapht.samples.tree.test.layout.algorithms;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.EdgeAwareLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ShapeFunctionAware;
import org.jungrapht.visualization.layout.algorithms.sugiyama.ArticulatedEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GraphLayers;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GreedyCycleRemoval;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaEdge;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaTransformedGraphSupplier;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SugiyamaVertex;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticSugiyamaVertex;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Synthetics;
import org.jungrapht.visualization.layout.algorithms.util.RenderContextAware;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test only, as this class is hard-coded for a specific test graph
 *
 * @param <V>
 * @param <E>
 */
public class LayeredLayoutAlgorithm<V, E>
    implements LayoutAlgorithm<V>, RenderContextAware<V, E>, ShapeFunctionAware<V> {

  private static final Logger log = LoggerFactory.getLogger(LayeredLayoutAlgorithm.class);

  private static final Shape IDENTITY_SHAPE = new Ellipse2D.Double();

  /**
   * a Builder to create a configured instance
   *
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param <T> the type that is built
   * @param <B> the builder type
   */
  public static class Builder<
          V,
          E,
          T extends LayeredLayoutAlgorithm<V, E> & EdgeAwareLayoutAlgorithm<V, E>,
          B extends Builder<V, E, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected Function<V, Shape> vertexShapeFunction = v -> IDENTITY_SHAPE;
    protected boolean expandLayout = true;
    protected Runnable after = () -> {};

    /** {@inheritDoc} */
    protected B self() {
      return (B) this;
    }

    public B vertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
      this.vertexShapeFunction = vertexShapeFunction;
      return self();
    }

    /** {@inheritDoc} */
    public B expandLayout(boolean expandLayout) {
      this.expandLayout = expandLayout;
      return self();
    }

    public B after(Runnable after) {
      this.after = after;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new LayeredLayoutAlgorithm<>(this);
    }
  }

  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder ready to configure
   */
  public static <V, E> Builder<V, E, ?, ?> edgeAwareBuilder() {
    return new Builder<>();
  }

  protected Rectangle bounds = Rectangle.IDENTITY;
  protected List<V> roots;

  protected Function<V, Shape> vertexShapeFunction;
  protected boolean expandLayout;
  protected RenderContext<V, E> renderContext;
  CompletableFuture theFuture;
  Runnable after;
  int horizontalOffset = Integer.getInteger(PREFIX + "sugiyama.horizontal.offset", 50);
  int verticalOffset = Integer.getInteger(PREFIX + "sugiyama.vertical.offset", 50);

  public LayeredLayoutAlgorithm() {
    this(LayeredLayoutAlgorithm.edgeAwareBuilder());
  }

  private LayeredLayoutAlgorithm(Builder builder) {
    this(builder.vertexShapeFunction, builder.expandLayout, builder.after);
  }

  private LayeredLayoutAlgorithm(
      Function<V, Shape> vertexShapeFunction, boolean expandLayout, Runnable after) {
    this.vertexShapeFunction = vertexShapeFunction;
    this.expandLayout = expandLayout;
    this.after = after;
  }

  @Override
  public void setRenderContext(RenderContext<V, E> renderContext) {
    this.renderContext = renderContext;
  }

  @Override
  public void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
    this.vertexShapeFunction = vertexShapeFunction;
  }

  Graph<V, E> originalGraph;
  List<List<SugiyamaVertex<V>>> layers;
  Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph;
  Set<SugiyamaEdge<V, E>> markedSegments = new HashSet<>();

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    this.originalGraph = layoutModel.getGraph();
    this.svGraph = new SugiyamaTransformedGraphSupplier<>(originalGraph).get();
    GreedyCycleRemoval<SugiyamaVertex<V>, SugiyamaEdge<V, E>> greedyCycleRemoval =
        new GreedyCycleRemoval(svGraph);
    Collection<SugiyamaEdge<V, E>> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();

    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
    for (SugiyamaEdge<V, E> se : feedbackArcs) {
      svGraph.removeEdge(se);
      SugiyamaEdge<V, E> newEdge = SugiyamaEdge.of(se.edge, se.target, se.source);
      svGraph.addEdge(newEdge.source, newEdge.target, newEdge);
    }

    this.layers = GraphLayers.assign(svGraph);
    if (log.isTraceEnabled()) {
      GraphLayers.checkLayers(layers);
    }

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<SugiyamaEdge<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    SugiyamaVertex<V>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);

    // rearranged locations of sythetic vertices to match the graph layering from the
    // Brandes Kopf paper
    layers = arrayToList(layersArray);
    LayeredLayoutAlgorithm.rearrangeLayers(layers);
    layersArray = listToArray(layers);

    Map<SugiyamaVertex<V>, SugiyamaVertex<V>> vertexMap = new HashMap<>();
    for (List<SugiyamaVertex<V>> layer : layers) {
      for (SugiyamaVertex<V> sugiyamaVertex : layer) {
        vertexMap.put(sugiyamaVertex, sugiyamaVertex);
      }
    }

    // report the info from the preprocessing step to compare log output with the visual
    // layered graph
    preprocessing();

    // set the vertex points with the vertex shape function and
    // the rank/index values
    int y = 0;
    int x = 0;
    for (int i = 0; i < layers.size(); i++) {
      List<SugiyamaVertex<V>> list = layers.get(i);
      y += verticalOffset;
      x = 0;
      for (int j = 0; j < list.size(); j++) {
        SugiyamaVertex<V> v = list.get(j);
        x += horizontalOffset + vertexShapeFunction.apply(v.vertex).getBounds().width;
        v.setPoint(Point.of(x, y));
      }
    }

    // determine the overall width / height of the layout area
    Map<Integer, Integer> rowWidthMap = new HashMap<>();
    Map<Integer, Integer> rowMaxHeightMap = new HashMap<>();
    int layerIndex = 0;
    Function<V, Shape> vertexShapeFunction = renderContext.getVertexShapeFunction();
    int totalHeight = 0;
    int totalWidth = 0;
    for (List<SugiyamaVertex<V>> layer : layers) {
      int width = horizontalOffset;
      int maxHeight = 0;
      for (SugiyamaVertex<V> sugiyamaVertex : layer) {
        if (!(sugiyamaVertex instanceof SyntheticSugiyamaVertex)) {
          java.awt.Rectangle bounds = vertexShapeFunction.apply(sugiyamaVertex.vertex).getBounds();
          width += bounds.width + horizontalOffset;
          maxHeight = Math.max(maxHeight, bounds.height);
        } else {
          width += horizontalOffset;
        }
      }
      rowWidthMap.put(layerIndex, width);
      rowMaxHeightMap.put(layerIndex, maxHeight);
      layerIndex++;
    }
    int widestRowWidth = rowWidthMap.values().stream().mapToInt(v -> v).max().getAsInt();
    totalWidth = widestRowWidth + horizontalOffset;
    totalHeight = layers.size() * verticalOffset + 2 * verticalOffset;
    layoutModel.setSize(totalWidth + horizontalOffset, totalHeight);
    // now all the vertices in layers (best) have points associated with them
    // every vertex in vertexMap has a point value

    svGraph
        .vertexSet()
        .forEach(
            v -> {
              SugiyamaVertex<V> sugiyamaVertex2 = vertexMap.get(v);
              if (sugiyamaVertex2 != null) {
                Point p = sugiyamaVertex2.getPoint();
                v.setPoint(p);
              } else {
                log.error("got null");
              }
            });

    List<ArticulatedEdge<V, E>> articulatedEdges = synthetics.makeArticulatedEdges();

    // reverse the direction of any feedback arcs (put them back in the direction
    // they belong in
    Set<E> feedbackEdges = new HashSet<>();
    feedbackArcs.forEach(a -> feedbackEdges.add(a.edge));
    articulatedEdges
        .stream()
        .filter(ae -> feedbackEdges.contains(ae.edge))
        .forEach(
            ae -> {
              svGraph.removeEdge(ae);
              SugiyamaEdge<V, E> reversed = ae.reversed();
              svGraph.addEdge(reversed.source, reversed.target, reversed);
            });

    for (ArticulatedEdge<V, E> ae : articulatedEdges) {
      for (SugiyamaVertex<V> sugiyamaVertex : ae.getIntermediateVertices()) {
        sugiyamaVertex.setPoint(vertexMap.get(sugiyamaVertex).getPoint());
      }
    }

    Map<E, List<Point>> edgePointMap = new HashMap<>();
    for (ArticulatedEdge<V, E> ae : articulatedEdges) {
      List<Point> points = new ArrayList<>();
      if (feedbackEdges.contains(ae.edge)) {
        points.add(ae.target.getPoint());
        points.addAll(ae.reversed().getIntermediatePoints());
        points.add(ae.source.getPoint());
      } else {
        points.add(ae.source.getPoint());
        points.addAll(ae.getIntermediatePoints());
        points.add(ae.target.getPoint());
      }

      edgePointMap.put(ae.edge, points);
    }
    EdgeShape.ArticulatedLine<V, E> edgeShape = new EdgeShape.ArticulatedLine<>();
    edgeShape.setEdgeArticulationFunction(
        e -> edgePointMap.getOrDefault(e, Collections.emptyList()));

    renderContext.setEdgeShapeFunction(edgeShape);

    svGraph.vertexSet().forEach(v -> layoutModel.set(v.vertex, v.getPoint()));
  }

  void preprocessing() {
    int h = layers.size();
    //    for (int i = 2; i <= h - 2; i++) {
    for (int i = 1; i <= h - 2; i++) { // zero based

      int k0 = 0;
      int el = 0;
      List<SugiyamaVertex<V>> thisLayer = layers.get(i); // Li
      List<SugiyamaVertex<V>> nextLayer = layers.get(i + 1); // Li+1
      //      for (int el1 = 1; el1 <= nextLayer.size(); el1++) {
      for (int el1 = 0; el1 <= nextLayer.size() - 1; el1++) { // zero based
        // get the vertex at next layer index el1
        SugiyamaVertex<V> vel1nextLayer = nextLayer.get(el1);
        //        SugiyamaVertex<V> velNextLayer = nextLayer.get(el);
        if (el1 == nextLayer.size() - 1 || vel1nextLayer instanceof SyntheticSugiyamaVertex) {
          int k1 = thisLayer.size() - 1;
          if (vel1nextLayer instanceof SyntheticSugiyamaVertex) {
            Optional<SugiyamaEdge<V, E>> incomingEdgeOpt =
                svGraph.incomingEdgesOf(vel1nextLayer).stream().findFirst();
            if (incomingEdgeOpt.isPresent()) {
              SugiyamaEdge<V, E> incomingEdge = incomingEdgeOpt.get();
              SugiyamaVertex<V> upperNeighbor = svGraph.getEdgeSource(incomingEdge);
              k1 = upperNeighbor.getIndex();
            }
          }
          while (el <= el1) {
            SugiyamaVertex<V> velNextLayer = nextLayer.get(el);
            for (SugiyamaVertex<V> upperNeighbor : getUpperNeighbors(svGraph, velNextLayer)) {
              int k = upperNeighbor.getIndex();
              if (k < k0 || k > k1) {
                markedSegments.add(
                    svGraph.getEdge(layers.get(i).get(k), layers.get(i + 1).get(el)));
                log.info(
                    "added edge from {} to {} to marked segments", upperNeighbor, velNextLayer);
              }
            }
            el++;
          }
          k0 = k1;
        }
      }
    }
  }

  List<SugiyamaVertex<V>> getUpperNeighbors(
      Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> graph, SugiyamaVertex<V> v) {
    return graph.incomingEdgesOf(v).stream().map(graph::getEdgeSource).collect(Collectors.toList());
  }

  public static <V> void rearrangeLayers(List<List<SugiyamaVertex<V>>> layers) {
    // layer 0 is okay
    // layer 1, move 0 to 3
    List<SugiyamaVertex<V>> list = layers.get(1);
    SugiyamaVertex<V> v = list.remove(0);
    list.add(3, v);
    Collections.swap(list, 0, 1);
    Collections.swap(list, 1, 2);

    // layer 2, move 0, 1 to 2, 3
    list = layers.get(2);
    v = list.remove(0);
    list.add(3, v);
    v = list.remove(0);
    list.add(3, v);
    Collections.swap(list, 0, 1);
    Collections.swap(list, 1, 4);

    // layer 3 move 0, 1 to 2, 3
    list = layers.get(3);
    v = list.remove(0);
    list.add(3, v);
    v = list.remove(0);
    list.add(3, v);
    Collections.swap(list, 0, 1);
    Collections.swap(list, 1, 4);

    // layer 4 move 0, 1 to 2, 5
    list = layers.get(4);
    v = list.remove(0);
    list.add(3, v);
    v = list.remove(0);
    list.add(5, v);
    Collections.swap(list, 0, 4);
    Collections.swap(list, 6, 1);
    Collections.swap(list, 3, 4);
    Collections.swap(list, 4, 6);

    // layer 5 move 0, 1, 2 to 2, 3, 6
    list = layers.get(5);
    v = list.remove(2);
    list.add(6, v);
    v = list.remove(1);
    list.add(3, v);
    v = list.remove(0);
    list.add(2, v);
    Collections.swap(list, 5, 0);
    Collections.swap(list, 1, 7);
    Collections.swap(list, 4, 5);
    Collections.swap(list, 5, 7);

    // layer 6 move 1, 2, 3 to 2, 3, 4
    list = layers.get(6);
    v = list.remove(3);
    list.add(4, v);
    v = list.remove(2);
    list.add(3, v);
    v = list.remove(1);
    list.add(2, v);
    Collections.swap(list, 7, 1);
    Collections.swap(list, 6, 7);

    // layer 7 move 1, 2, 3 to 2, 3, 5
    list = layers.get(7);
    v = list.remove(3);
    list.add(5, v);
    v = list.remove(2);
    list.add(3, v);
    v = list.remove(1);
    list.add(2, v);
    Collections.swap(list, 1, 6);
    Collections.swap(list, 4, 6);

    // layer 8 ok
    // layer 9 ok

    // update all the metadata indices
    for (int j = 0; j < layers.size(); j++) {
      List<SugiyamaVertex<V>> rank = layers.get(j);
      log.info("rank {}", j);
      for (int i = 0; i < rank.size(); i++) {
        SugiyamaVertex<V> vv = rank.get(i);
        log.info("changed index from {} to {}", vv.getIndex(), i);
        vv.setIndex(i);
      }
    }
  }

  static <V> List<List<SugiyamaVertex<V>>> arrayToList(SugiyamaVertex<V>[][] array) {
    List<List<SugiyamaVertex<V>>> layers = new ArrayList<>();
    for (int i = 0; i < array.length; i++) {
      List<SugiyamaVertex<V>> list = new LinkedList<>();
      layers.add(list);
      for (int j = 0; j < array[i].length; j++) {
        list.add(array[i][j]);
      }
    }
    return layers;
  }

  static <V> SugiyamaVertex<V>[][] listToArray(List<List<SugiyamaVertex<V>>> layers) {
    SugiyamaVertex<V>[][] array = new SugiyamaVertex[layers.size()][];
    for (int i = 0; i < layers.size(); i++) {
      List<SugiyamaVertex<V>> list = layers.get(i);
      array[i] = new SugiyamaVertex[list.size()];
      for (int j = 0; j < list.size(); j++) {
        array[i][j] = list.get(j);
      }
    }
    return array;
  }
}
