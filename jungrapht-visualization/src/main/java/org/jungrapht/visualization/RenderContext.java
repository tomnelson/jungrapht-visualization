package org.jungrapht.visualization;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.NetworkElementAccessor;
import org.jungrapht.visualization.renderers.EdgeLabelRenderer;
import org.jungrapht.visualization.renderers.NodeLabelRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.Context;
import org.jungrapht.visualization.util.EdgeIndexFunction;

/**
 * @param <N>
 * @param <E>
 * @author Tom Nelson
 */
public interface RenderContext<N, E> {

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

  Function<E, Font> getEdgeFontFunction();

  void setEdgeFontFunction(Function<E, Font> edgeFontFunction);

  Predicate<E> getEdgeIncludePredicate();

  void setEdgeIncludePredicate(Predicate<E> edgeIncludePredicate);

  public float getEdgeLabelCloseness();

  public void setEdgeLabelCloseness(float closeness);

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

  Function<Context<Graph<N, E>, E>, Shape> getEdgeShapeFunction();

  void setEdgeShapeFunction(Function<Context<Graph<N, E>, E>, Shape> edgeShapeFunction);

  Function<E, String> getEdgeLabelFunction();

  void setEdgeLabelFunction(Function<E, String> edgeStringer);

  Function<E, Stroke> edgeStrokeFunction();

  void setEdgeStrokeFunction(Function<E, Stroke> edgeStrokeFunction);

  Function<E, Stroke> getEdgeArrowStrokeFunction();

  void setEdgeArrowStrokeFunction(Function<E, Stroke> edgeArrowStrokeFunction);

  GraphicsDecorator getGraphicsContext();

  void setGraphicsContext(GraphicsDecorator graphicsContext);

  EdgeIndexFunction<N, E> getParallelEdgeIndexFunction();

  void setParallelEdgeIndexFunction(EdgeIndexFunction<N, E> parallelEdgeIndexFunction);

  MutableSelectedState<E> getSelectedEdgeState();

  void setSelectedEdgeState(MutableSelectedState<E> selectedEdgeState);

  MutableSelectedState<N> getSelectedNodeState();

  void setSelectedNodeState(MutableSelectedState<N> selectedNodeState);

  CellRendererPane getRendererPane();

  void setRendererPane(CellRendererPane rendererPane);

  JComponent getScreenDevice();

  void setScreenDevice(JComponent screenDevice);

  Function<N, Font> getNodeFontFunction();

  void setNodeFontFunction(Function<N, Font> nodeFontFunction);

  Function<N, Icon> getNodeIconFunction();

  void setNodeIconFunction(Function<N, Icon> nodeIconFunction);

  Predicate<N> getNodeIncludePredicate();

  void setNodeIncludePredicate(Predicate<N> nodeIncludePredicate);

  NodeLabelRenderer getNodeLabelRenderer();

  void setNodeLabelRenderer(NodeLabelRenderer nodeLabelRenderer);

  Function<N, Paint> getNodeFillPaintFunction();

  void setNodeFillPaintFunction(Function<N, Paint> nodeFillPaintFunction);

  Function<N, Paint> getNodeDrawPaintFunction();

  void setNodeDrawPaintFunction(Function<N, Paint> nodeDrawPaintFunction);

  Function<N, Shape> getNodeShapeFunction();

  void setNodeShapeFunction(Function<N, Shape> nodeShapeFunction);

  Function<N, String> getNodeLabelFunction();

  void setNodeLabelFunction(Function<N, String> nodeStringer);

  Function<N, Paint> getNodeLabelDrawPaintFunction();

  void setNodeLabelDrawPaintFunction(Function<N, Paint> nodeLabelDrawPaintFunction);

  Function<N, Stroke> getNodeStrokeFunction();

  void setNodeStrokeFunction(Function<N, Stroke> nodeStrokeFunction);

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
  NetworkElementAccessor<N, E> getPickSupport();

  /** @param pickSupport the pickSupport to set */
  void setPickSupport(NetworkElementAccessor<N, E> pickSupport);

  Renderer.NodeLabel.Position getNodeLabelPosition();

  void setNodeLabelPosition(Renderer.NodeLabel.Position position);

  boolean isComplexRendering();

  void setComplexRendering(boolean complexRendering);
}
