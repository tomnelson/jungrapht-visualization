package org.jungrapht.samples.sugiyama.test.algorithms;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.CompletableFuture;
import org.jungrapht.visualization.layout.algorithms.EdgeAwareLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.RenderContextAware;
import org.jungrapht.visualization.layout.algorithms.util.VertexShapeAware;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test only, as this class is hard-coded for a specific test graph
 *
 * @param <V>
 * @param <E>
 */
public class TestEiglspergerLayoutAlgorithm<V, E> extends EiglspergerLayoutAlgorithm<V, E>
    implements LayoutAlgorithm<V>, RenderContextAware<V, E>, VertexShapeAware<V> {

  private static final Logger log = LoggerFactory.getLogger(TestEiglspergerLayoutAlgorithm.class);

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
          T extends TestEiglspergerLayoutAlgorithm<V, E> & EdgeAwareLayoutAlgorithm<V, E>,
          B extends Builder<V, E, T, B>>
      extends EiglspergerLayoutAlgorithm.Builder<V, E, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {
    boolean doUpLeft = false;
    boolean doDownLeft = false;
    boolean doUpRight = false;
    boolean doDownRight = false;

    public B doUpLeft(boolean doUpLeft) {
      this.doUpLeft = doUpLeft;
      return self();
    }

    public B doUpRight(boolean doUpRight) {
      this.doUpRight = doUpRight;
      return self();
    }

    public B doDownLeft(boolean doDownLeft) {
      this.doDownLeft = doDownLeft;
      return self();
    }

    public B doDownRight(boolean doDownRight) {
      this.doDownRight = doDownRight;
      return self();
    }

    /** {@inheritDoc} */
    public T build() {
      return (T) new TestEiglspergerLayoutAlgorithm<>(this);
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

  boolean doUpLeft;
  boolean doDownLeft;
  boolean doUpRight;
  boolean doDownRight;

  public TestEiglspergerLayoutAlgorithm() {
    this(TestEiglspergerLayoutAlgorithm.edgeAwareBuilder());
  }

  protected TestEiglspergerLayoutAlgorithm(Builder builder) {
    super(builder);
    this.doUpLeft = builder.doUpLeft;
    this.doUpRight = builder.doUpRight;
    this.doDownLeft = builder.doDownLeft;
    this.doDownRight = builder.doDownRight;
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {

    Runnable runnable =
        TestEiglspergerRunnable.<V, E>builder()
            .layoutModel(layoutModel)
            .renderContext(renderContext)
            .straightenEdges(straightenEdges)
            .postStraighten(postStraighten)
            .maxLevelCross(maxLevelCross)
            .useLongestPathLayering(useLongestPathLayering)
            .doUpLeft(doUpLeft)
            .doUpRight(doUpRight)
            .doDownLeft(doDownLeft)
            .doDownRight(doDownRight)
            .build();
    if (threaded) {

      theFuture =
          CompletableFuture.runAsync(runnable)
              .thenRun(
                  () -> {
                    log.trace("Eiglsperger layout done");
                    this.run(); // run the after function
                    layoutModel.getViewChangeSupport().fireViewChanged();
                    // fire an event to say that the layout is done
                    layoutModel
                        .getLayoutStateChangeSupport()
                        .fireLayoutStateChanged(layoutModel, false);
                  });
    } else {
      runnable.run();
      after.run();
      layoutModel.getViewChangeSupport().fireViewChanged();
      // fire an event to say that the layout is done
      layoutModel.getLayoutStateChangeSupport().fireLayoutStateChanged(layoutModel, false);
    }
  }

  //  private TestEiglspergerLayoutAlgorithm(
  //      Function<V, Shape> vertexShapeFunction,
  //      boolean expandLayout,
  //      Runnable after,
  //      boolean doUpLeft,
  //      boolean doUpRight,
  //      boolean doDownLeft,
  //      boolean doDownRight) {
  //    super(builder);
  //
  //    this.vertexShapeFunction = vertexShapeFunction;
  //    this.expandLayout = expandLayout;
  //    this.after = after;
  //    this.doUpLeft = doUpLeft;
  //    this.doUpRight = doUpRight;
  //    this.doDownLeft = doDownLeft;
  //    this.doDownRight = doDownRight;
  //  }

  //  @Override
  //  public void setRenderContext(RenderContext<V, E> renderContext) {
  //    this.renderContext = renderContext;
  //  }
  //
  //  @Override
  //  public void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
  //    this.vertexShapeFunction = vertexShapeFunction;
  //  }

  //  Graph<V, E> originalGraph;
  //  //  List<List<SugiyamaVertex<V>>> layers;
  //  Graph<LV<V>, LE<V, E>> svGraph;
  //  Set<LE<V, E>> markedSegments = new HashSet<>();

  //  @Override
  //  public void visit(LayoutModel<V> layoutModel) {
  //    this.originalGraph = layoutModel.getGraph();
  //    // transform the graph to the svGraph delegate
  //    this.svGraph = new TransformedGraphSupplier<>(originalGraph).get();
  //    // remove (reverse) cycles
  //    GreedyCycleRemoval<LV<V>, LE<V, E>> greedyCycleRemoval = new GreedyCycleRemoval(svGraph);
  //    // save off the feedback arcs for later
  //    Collection<LE<V, E>> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();
  //
  //    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
  //    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
  //    for (LE<V, E> se : feedbackArcs) {
  //      svGraph.removeEdge(se);
  //      LE<V, E> newEdge = LE.of(se.getEdge(), se.getTarget(), se.getSource());
  //      svGraph.addEdge(newEdge.getSource(), newEdge.getTarget(), newEdge);
  //    }
  //
  //    // build the layered graph
  //    List<List<LV<V>>> layers = GraphLayers.assign(svGraph);
  //
  //    // check that the rank/index metadata matches the actual rank/index in layers
  //    GraphLayers.checkLayers(layers);
  //
  //    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
  //    List<LE<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
  //    // create the synthetic vertices and edges
  //    LV<V>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
  //    GraphLayers.checkLayers(layersArray);
  //    // special case only for the test graph, pretend we did the swaps to get the
  //    // indices to match what is in the paper
  //    layers = LayeredLayoutAlgorithm.arrayToList(layersArray);
  //
  //    LayeredLayoutAlgorithm.rearrangeLayers(layers);
  //
  //    layersArray = LayeredLayoutAlgorithm.listToArray(layers);
  //
  //    // check the metadata
  //    GraphLayers.checkLayers(layers);
  //    //      GraphLayers.checkLayers(layersArray);
  //
  //    //    justSetThePoints();
  //    SelectiveEiglspergerHorizontalCoordinateAssignment.horizontalCoordinateAssignment(
  //        layersArray, svGraph, markedSegments, 50, 50, doUpLeft, doUpRight, doDownLeft, doDownRight);
  //
  //    Map<LV<V>, LV<V>> vertexMap = new HashMap<>();
  //    for (List<LV<V>> layer : layers) {
  //      for (LV<V> LV : layer) {
  //        vertexMap.put(LV, LV);
  //      }
  //    }
  //
  //    Map<Integer, Integer> rowWidthMap = new HashMap<>();
  //    Map<Integer, Integer> rowMaxHeightMap = new HashMap<>();
  //    int layerIndex = 0;
  //    Function<V, Shape> vertexShapeFunction = renderContext.getVertexShapeFunction();
  //    int totalHeight = 0;
  //    int totalWidth = 0;
  //    for (List<LV<V>> layer : layers) {
  //      int width = horizontalOffset;
  //      int maxHeight = 0;
  //      for (LV<V> LV : layer) {
  //        if (!(LV instanceof SyntheticLV)) {
  //          java.awt.Rectangle bounds = vertexShapeFunction.apply(LV.getVertex()).getBounds();
  //          width += bounds.width + horizontalOffset;
  //          maxHeight = Math.max(maxHeight, bounds.height);
  //        } else {
  //          width += horizontalOffset;
  //        }
  //      }
  //      rowWidthMap.put(layerIndex, width);
  //      rowMaxHeightMap.put(layerIndex, maxHeight);
  //      layerIndex++;
  //    }
  //    int widestRowWidth = rowWidthMap.values().stream().mapToInt(v -> v).max().getAsInt();
  //    totalWidth = widestRowWidth + horizontalOffset;
  //    totalHeight = layers.size() * verticalOffset + 2 * verticalOffset;
  //
  //    layoutModel.setSize(totalWidth + horizontalOffset, totalHeight);
  //    // now all the vertices in layers (best) have points associated with them
  //    // every vertex in vertexMap has a point value
  //
  //    svGraph
  //        .vertexSet()
  //        .forEach(
  //            v -> {
  //              LV<V> LV2 = vertexMap.get(v);
  //              if (LV2 != null) {
  //                Point p = LV2.getPoint();
  //                v.setPoint(p);
  //              } else {
  //                log.error("got null");
  //              }
  //            });
  //
  //    List<ArticulatedEdge<V, E>> articulatedEdges = synthetics.makeArticulatedEdges();
  //
  //    Set<E> feedbackEdges = new HashSet<>();
  //    feedbackArcs.forEach(a -> feedbackEdges.add(a.getEdge()));
  //    articulatedEdges
  //        .stream()
  //        .filter(ae -> feedbackEdges.contains(ae.edge))
  //        .forEach(
  //            ae -> {
  //              svGraph.removeEdge(ae);
  //              LE<V, E> reversed = ae.reversed();
  //              svGraph.addEdge(reversed.getSource(), reversed.getTarget(), reversed);
  //            });
  //
  //    for (ArticulatedEdge<V, E> ae : articulatedEdges) {
  //      for (LV<V> LV : ae.getIntermediateVertices()) {
  //        LV.setPoint(vertexMap.get(LV).getPoint());
  //      }
  //    }
  //
  //    Map<E, List<Point>> edgePointMap = new HashMap<>();
  //    for (ArticulatedEdge<V, E> ae : articulatedEdges) {
  //      List<Point> points = new ArrayList<>();
  //      if (feedbackEdges.contains(ae.edge)) {
  //        points.add(ae.target.getPoint());
  //        points.addAll(ae.reversed().getIntermediatePoints());
  //        points.add(ae.source.getPoint());
  //      } else {
  //        points.add(ae.source.getPoint());
  //        points.addAll(ae.getIntermediatePoints());
  //        points.add(ae.target.getPoint());
  //      }
  //
  //      edgePointMap.put(ae.edge, points);
  //    }
  //    EdgeShape.ArticulatedLine<V, E> edgeShape = new EdgeShape.ArticulatedLine<>();
  //    edgeShape.setEdgeArticulationFunction(
  //        e -> edgePointMap.getOrDefault(e, Collections.emptyList()));
  //
  //    renderContext.setEdgeShapeFunction(edgeShape);
  //
  //    svGraph.vertexSet().forEach(v -> layoutModel.set(v.getVertex(), v.getPoint()));
  //    after.run();
  //  }

  //  private void justSetThePoints(List<List<LV<V>>> layers) {
  //    int y = 0;
  //    int x = 0;
  //    for (int i = 0; i < layers.size(); i++) {
  //      List<LV<V>> list = layers.get(i);
  //      y += verticalOffset;
  //      x = 0;
  //      for (int j = 0; j < list.size(); j++) {
  //        LV<V> v = list.get(j);
  //        x += horizontalOffset + vertexShapeFunction.apply(v.getVertex()).getBounds().width;
  //        v.setPoint(Point.of(x, y));
  //      }
  //    }
  //  }
}
