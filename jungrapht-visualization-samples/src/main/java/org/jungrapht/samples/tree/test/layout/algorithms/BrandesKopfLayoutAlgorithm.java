package org.jungrapht.samples.tree.test.layout.algorithms;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
public class BrandesKopfLayoutAlgorithm<V, E>
    implements LayoutAlgorithm<V>, RenderContextAware<V, E>, ShapeFunctionAware<V> {

  private static final Logger log = LoggerFactory.getLogger(BrandesKopfLayoutAlgorithm.class);

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
          T extends BrandesKopfLayoutAlgorithm<V, E> & EdgeAwareLayoutAlgorithm<V, E>,
          B extends Builder<V, E, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected Function<V, Shape> vertexShapeFunction = v -> IDENTITY_SHAPE;
    protected boolean expandLayout = true;
    protected Runnable after = () -> {};
    boolean doUpLeft = false;
    boolean doDownLeft = false;
    boolean doUpRight = false;
    boolean doDownRight = false;

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
      return (T) new BrandesKopfLayoutAlgorithm<>(this);
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
  Runnable after;
  int horizontalOffset = Integer.getInteger(PREFIX + "sugiyama.horizontal.offset", 50);
  int verticalOffset = Integer.getInteger(PREFIX + "sugiyama.vertical.offset", 50);
  boolean doUpLeft;
  boolean doDownLeft;
  boolean doUpRight;
  boolean doDownRight;

  public BrandesKopfLayoutAlgorithm() {
    this(BrandesKopfLayoutAlgorithm.edgeAwareBuilder());
  }

  private BrandesKopfLayoutAlgorithm(Builder builder) {
    this(
        builder.vertexShapeFunction,
        builder.expandLayout,
        builder.after,
        builder.doUpLeft,
        builder.doUpRight,
        builder.doDownLeft,
        builder.doDownRight);
  }

  private BrandesKopfLayoutAlgorithm(
      Function<V, Shape> vertexShapeFunction,
      boolean expandLayout,
      Runnable after,
      boolean doUpLeft,
      boolean doUpRight,
      boolean doDownLeft,
      boolean doDownRight) {
    this.vertexShapeFunction = vertexShapeFunction;
    this.expandLayout = expandLayout;
    this.after = after;
    this.doUpLeft = doUpLeft;
    this.doUpRight = doUpRight;
    this.doDownLeft = doDownLeft;
    this.doDownRight = doDownRight;
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
  //  List<List<SugiyamaVertex<V>>> layers;
  Graph<SugiyamaVertex<V>, SugiyamaEdge<V, E>> svGraph;
  Set<SugiyamaEdge<V, E>> markedSegments = new HashSet<>();

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    this.originalGraph = layoutModel.getGraph();
    // transform the graph to the svGraph delegate
    this.svGraph = new SugiyamaTransformedGraphSupplier<>(originalGraph).get();
    // remove (reverse) cycles
    GreedyCycleRemoval<SugiyamaVertex<V>, SugiyamaEdge<V, E>> greedyCycleRemoval =
        new GreedyCycleRemoval(svGraph);
    // save off the feedback arcs for later
    Collection<SugiyamaEdge<V, E>> feedbackArcs = greedyCycleRemoval.getFeedbackArcs();

    // reverse the direction of feedback arcs so that they no longer introduce cycles in the graph
    // the feedback arcs will be processed later to draw with the correct direction and correct articulation points
    for (SugiyamaEdge<V, E> se : feedbackArcs) {
      svGraph.removeEdge(se);
      SugiyamaEdge<V, E> newEdge = SugiyamaEdge.of(se.edge, se.target, se.source);
      svGraph.addEdge(newEdge.source, newEdge.target, newEdge);
    }

    // build the layered graph
    List<List<SugiyamaVertex<V>>> layers = GraphLayers.assign(svGraph);

    // check that the rank/index metadata matches the actual rank/index in layers
    GraphLayers.checkLayers(layers);

    Synthetics<V, E> synthetics = new Synthetics<>(svGraph);
    List<SugiyamaEdge<V, E>> edges = new ArrayList<>(svGraph.edgeSet());
    // create the synthetic vertices and edges
    SugiyamaVertex<V>[][] layersArray = synthetics.createVirtualVerticesAndEdges(edges, layers);
    GraphLayers.checkLayers(layersArray);
    // special case only for the test graph, pretend we did the swaps to get the
    // indices to match what is in the paper
    layers = LayeredLayoutAlgorithm.arrayToList(layersArray);

    LayeredLayoutAlgorithm.rearrangeLayers(layers);

    layersArray = LayeredLayoutAlgorithm.listToArray(layers);

    // check the metadata
    GraphLayers.checkLayers(layers);
    //      GraphLayers.checkLayers(layersArray);

    //    justSetThePoints();
    SelectiveHorizontalCoordinateAssignment.horizontalCoordinateAssignment(
        layersArray, svGraph, markedSegments, 50, 50, doUpLeft, doUpRight, doDownLeft, doDownRight);

    Map<SugiyamaVertex<V>, SugiyamaVertex<V>> vertexMap = new HashMap<>();
    for (List<SugiyamaVertex<V>> layer : layers) {
      for (SugiyamaVertex<V> sugiyamaVertex : layer) {
        vertexMap.put(sugiyamaVertex, sugiyamaVertex);
      }
    }

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
    after.run();
  }

  private void justSetThePoints(List<List<SugiyamaVertex<V>>> layers) {
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
  }
}
