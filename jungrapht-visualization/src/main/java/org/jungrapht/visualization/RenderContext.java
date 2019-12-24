package org.jungrapht.visualization;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.GraphElementAccessor;
import org.jungrapht.visualization.layout.event.RenderContextStateChange;
import org.jungrapht.visualization.renderers.EdgeLabelRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.renderers.VertexLabelRenderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.Context;
import org.jungrapht.visualization.util.EdgeIndexFunction;

/**
 * @param <V>
 * @param <E>
 * @author Tom Nelson
 */
public interface RenderContext<V, E> extends RenderContextStateChange.Producer {

  class Builder<V, E, T extends RenderContext<V, E>, B extends Builder<V, E, T, B>> {

    Graph<V, E> graph;

    public Builder(Graph<V, E> graph) {
      this.graph = graph;
    }

    T build() {
      return (T) new DefaultRenderContext<>(this);
    }
  }

  static <V, E> Builder<V, E, ?, ?> builder(Graph<V, E> graph) {
    return new Builder<>(graph);
  }

  float[] dotting = {1.0f, 3.0f};
  float[] dashing = {5.0f};

  /**
   * A stroke for a dotted line: 1 pixel width, round caps, round joins, and an array of {1.0f,
   * 3.0f}.
   */
  Stroke DOTTED =
      new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dotting, 0f);

  /**
   * A stroke for a dashed line: 1 pixel width, square caps, beveled joins, and an array of {5.0f}.
   */
  Stroke DASHED =
      new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1.0f, dashing, 0f);

  Stroke LINE = new BasicStroke(1.0f);

  /** Specifies the offset for the edge labels. */
  int LABEL_OFFSET = 10;

  int getLabelOffset();

  void setLabelOffset(int labelOffset);

  float getArrowPlacementTolerance();

  void setArrowPlacementTolerance(float arrow_placement_tolerance);

  Shape getEdgeArrow();

  void setEdgeArrow(Shape shape);

  boolean renderEdgeArrow();

  void setRenderEdgeArrow(boolean render);

  void setArrowsOnUndirectedEdges(boolean setArrowsOnUndirectedEdges);

  boolean getArrowsOnUndirectedEdges();

  Function<E, Font> getEdgeFontFunction();

  void setEdgeFontFunction(Function<E, Font> edgeFontFunction);

  Predicate<E> getEdgeIncludePredicate();

  void setEdgeIncludePredicate(Predicate<E> edgeIncludePredicate);

  float getEdgeLabelCloseness();

  void setEdgeLabelCloseness(float closeness);

  EdgeLabelRenderer getEdgeLabelRenderer();

  void setEdgeLabelRenderer(EdgeLabelRenderer edgeLabelRenderer);

  Function<E, Paint> getEdgeFillPaintFunction();

  void setEdgeFillPaintFunction(Function<E, Paint> edgePaintFunction);

  Function<E, Paint> getEdgeDrawPaintFunction();

  void setEdgeDrawPaintFunction(Function<E, Paint> edgeDrawPaintFunction);

  Function<E, Paint> getArrowDrawPaintFunction();

  void setArrowDrawPaintFunction(Function<E, Paint> arrowDrawPaintFunction);

  Function<E, Paint> getArrowFillPaintFunction();

  void setArrowFillPaintFunction(Function<E, Paint> arrowFillPaintFunction);

  Function<Context<Graph<V, E>, E>, Shape> getEdgeShapeFunction();

  void setEdgeShapeFunction(Function<Context<Graph<V, E>, E>, Shape> edgeShapeFunction);

  Function<E, String> getEdgeLabelFunction();

  void setEdgeLabelFunction(Function<E, String> edgeStringer);

  Function<E, Stroke> edgeStrokeFunction();

  void setEdgeStrokeFunction(Function<E, Stroke> edgeStrokeFunction);

  Function<E, Stroke> getEdgeArrowStrokeFunction();

  void setEdgeArrowStrokeFunction(Function<E, Stroke> edgeArrowStrokeFunction);

  GraphicsDecorator getGraphicsContext();

  void setGraphicsContext(GraphicsDecorator graphicsContext);

  EdgeIndexFunction<V, E> getParallelEdgeIndexFunction();

  void setParallelEdgeIndexFunction(EdgeIndexFunction<V, E> parallelEdgeIndexFunction);

  MutableSelectedState<E> getSelectedEdgeState();

  void setSelectedEdgeState(MutableSelectedState<E> selectedEdgeState);

  MutableSelectedState<V> getSelectedVertexState();

  void setSelectedVertexState(MutableSelectedState<V> selectedVertexState);

  CellRendererPane getRendererPane();

  void setRendererPane(CellRendererPane rendererPane);

  JComponent getScreenDevice();

  void setScreenDevice(JComponent screenDevice);

  Function<V, Font> getVertexFontFunction();

  void setVertexFontFunction(Function<V, Font> vertexFontFunction);

  Function<V, Icon> getVertexIconFunction();

  void setVertexIconFunction(Function<V, Icon> vertexIconFunction);

  Predicate<V> getVertexIncludePredicate();

  void setVertexIncludePredicate(Predicate<V> vertexIncludePredicate);

  VertexLabelRenderer getVertexLabelRenderer();

  void setVertexLabelRenderer(VertexLabelRenderer vertexLabelRenderer);

  Function<V, Paint> getVertexFillPaintFunction();

  void setVertexFillPaintFunction(Function<V, Paint> vertexFillPaintFunction);

  Function<V, Paint> getVertexDrawPaintFunction();

  void setVertexDrawPaintFunction(Function<V, Paint> vertexDrawPaintFunction);

  Function<V, Shape> getVertexShapeFunction();

  void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction);

  Function<V, String> getVertexLabelFunction();

  void setVertexLabelFunction(Function<V, String> vertexStringer);

  Function<V, Paint> getVertexLabelDrawPaintFunction();

  void setVertexLabelDrawPaintFunction(Function<V, Paint> vertexLabelDrawPaintFunction);

  Function<V, Stroke> getVertexStrokeFunction();

  void setVertexStrokeFunction(Function<V, Stroke> vertexStrokeFunction);

  class DirectedEdgeArrowPredicate implements Predicate<Graph<?, ?>> {

    public boolean test(Graph<?, ?> graph) {
      return graph.getType().isDirected();
    }
  }

  class UndirectedEdgeArrowPredicate implements Predicate<Graph<?, ?>> {

    public boolean test(Graph<?, ?> graph) {
      return !graph.getType().isDirected();
    }
  }

  MultiLayerTransformer getMultiLayerTransformer();

  void setMultiLayerTransformer(MultiLayerTransformer basicTransformer);

  /** @return the pickSupport */
  GraphElementAccessor<V, E> getPickSupport();

  /** @param pickSupport the pickSupport to set */
  void setPickSupport(GraphElementAccessor<V, E> pickSupport);

  Renderer.VertexLabel.Position getVertexLabelPosition();

  void setVertexLabelPosition(Renderer.VertexLabel.Position position);
}
