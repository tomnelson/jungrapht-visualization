/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.ParallelEdgeShapeFunction;
import org.jungrapht.visualization.layout.NetworkElementAccessor;
import org.jungrapht.visualization.renderers.DefaultEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.DefaultNodeLabelRenderer;
import org.jungrapht.visualization.renderers.EdgeLabelRenderer;
import org.jungrapht.visualization.renderers.NodeLabelRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.ArrowFactory;
import org.jungrapht.visualization.util.Context;
import org.jungrapht.visualization.util.EdgeIndexFunction;
import org.jungrapht.visualization.util.ParallelEdgeIndexFunction;

/**
 * @author Tom Nelson
 * @param <N> node type
 * @param <E> edge type
 */
public class DefaultRenderContext<N, E> implements RenderContext<N, E> {

  private static final String PREFIX = "jungrapht.";

  // node visual property symbols
  private static final String NODE_SHAPE = PREFIX + "nodeShape";
  private static final String NODE_SIZE = PREFIX + "nodeSize";
  private static final String NODE_DRAW_COLOR = PREFIX + "nodeDrawColor";
  private static final String NODE_FILL_COLOR = PREFIX + "nodeFillColor";
  private static final String PICKED_NODE_COLOR = PREFIX + "pickedNodeFillColor";
  private static final String NODE_STROKE_WIDTH = PREFIX + "nodeStrokeWidth";

  // node label visual property symbols
  private static final String NODE_LABEL_FONT = PREFIX + "nodeLabelFont";
  private static final String NODE_LABEL_POSITION = PREFIX + "nodeLabelPosition";
  private static final String NODE_LABEL_DRAW_COLOR = PREFIX + "nodeLabelDrawColor";

  // edge visual property symbols
  private static final String EDGE_SHAPE = PREFIX + "edgeShape";
  private static final String EDGE_COLOR = PREFIX + "edgeColor";
  private static final String PICKED_EDGE_COLOR = PREFIX + "pickedEdgeColor";
  private static final String EDGE_STROKE = PREFIX + "edgeStroke";

  // edge label visual property symbols
  private static final String EDGE_LABEL_FONT = PREFIX + "edgeLabelFont";
  private static final String DIRECTED_EDGE_LABEL_CLOSENESS = PREFIX + "edgeLabelFont";
  private static final String UNDIRECTED_EDGE_LABEL_CLOSENESS = PREFIX + "edgeLabelFont";

  // edge arrow visual property symbols
  private static final String ARROW_STYLE = PREFIX + "arrowStyle";
  private static final String EDGE_ARROW_LENGTH = PREFIX + "edgeArrowLength";
  private static final String EDGE_ARROW_WIDTH = PREFIX + "edgeArrowWidth";
  private static final String EDGE_ARROW_NOTCH_DEPTH = PREFIX + "edgeArrowNotchDepth";
  private static final String EDGE_ARROW_STROKE = PREFIX + "edgeArrowStroke";
  private static final String ARROW_PLACEMENT_TOLERANCE = PREFIX + "arrowPlacementTolerance";

  protected MutableSelectedState<N> pickedNodeState;
  protected MutableSelectedState<E> pickedEdgeState;

  // node properties
  private int nodeSize = Integer.getInteger(NODE_SIZE, 12);
  private String nodeShapeString = System.getProperty(NODE_SHAPE, "CIRCLE");
  private Paint nodeDrawPaint = Color.getColor(NODE_DRAW_COLOR, Color.BLACK);
  private Paint nodeFillPaint = Color.getColor(NODE_FILL_COLOR, Color.RED);
  private Paint pickedNodeFillPaint = Color.getColor(PICKED_NODE_COLOR, Color.YELLOW);
  private Shape nodeShape = getNodeShape(nodeShapeString, nodeSize);

  // node functions
  protected Predicate<N> nodeIncludePredicate = n -> true;
  protected Function<N, Stroke> nodeStrokeFunction =
      n -> new BasicStroke(Float.parseFloat(System.getProperty(NODE_STROKE_WIDTH, "1.0")));
  protected Function<N, Shape> nodeShapeFunction = n -> nodeShape;

  protected Function<N, Paint> nodeDrawPaintFunction = n -> nodeDrawPaint;
  protected Function<N, Paint> nodeFillPaintFunction =
      n ->
          pickedNodeState != null && pickedNodeState.isSelected(n)
              ? pickedNodeFillPaint
              : nodeFillPaint;

  // node label properties
  private Font nodeFont = Font.getFont(NODE_LABEL_FONT, new Font("Helvetica", Font.PLAIN, 12));
  private Paint nodeLabelDrawPaint = Color.getColor(NODE_LABEL_DRAW_COLOR, Color.BLACK);
  private Renderer.NodeLabel.Position nodeLabelPosition =
      getPosition(System.getProperty(NODE_LABEL_POSITION));

  // node label functions
  protected Function<N, Font> nodeFontFunction = n -> nodeFont;
  protected Function<N, Paint> nodeLabelDrawPaintFunction = n -> nodeLabelDrawPaint;

  // edge properties
  private Stroke edgeStroke =
      new BasicStroke(Float.parseFloat(System.getProperty(EDGE_STROKE, "1.0")));
  private Color pickedEdgePaint = Color.getColor(PICKED_EDGE_COLOR, Color.CYAN);
  private Color edgePaint = Color.getColor(EDGE_COLOR, Color.BLACK);

  // edge functions
  protected Function<E, Stroke> edgeStrokeFunction = e -> edgeStroke;
  protected Function<E, Paint> edgeFillPaintFunction = n -> null;
  protected Function<E, Paint> edgeDrawPaintFunction =
      e -> pickedEdgeState != null && pickedEdgeState.isSelected(e) ? pickedEdgePaint : edgePaint;

  // edge label properties
  private Font edgeLabelFont = Font.getFont(EDGE_LABEL_FONT, new Font("Helvetica", Font.PLAIN, 12));

  // edge label functions
  protected Function<E, Font> edgeFontFunction = n -> edgeLabelFont;
  protected Function<E, String> edgeLabelFunction = e -> null;

  // edge arrow properties
  private int edgeArrowLength = Integer.getInteger(EDGE_ARROW_LENGTH, 10);
  private int edgeArrowWidth = Integer.getInteger(EDGE_ARROW_WIDTH, 8);
  private int edgeArrowNotchDepth = Integer.getInteger(EDGE_ARROW_NOTCH_DEPTH, 4);
  protected Shape edgeArrow;
  protected float arrowPlacementTolerance =
      Float.parseFloat(System.getProperty(ARROW_PLACEMENT_TOLERANCE, "1.0"));
  private Stroke edgeArrowStroke =
      new BasicStroke(Float.parseFloat(System.getProperty(EDGE_ARROW_STROKE, "1.0")));

  // edge arrow functions
  protected Function<E, Stroke> edgeArrowStrokeFunction = e -> edgeArrowStroke;
  protected Function<E, Paint> arrowFillPaintFunction =
      e -> pickedEdgeState != null && pickedEdgeState.isSelected(e) ? pickedEdgePaint : edgePaint;
  protected Function<E, Paint> arrowDrawPaintFunction =
      e -> pickedEdgeState != null && pickedEdgeState.isSelected(e) ? pickedEdgePaint : edgePaint;

  protected Function<N, String> nodeLabelFunction = n -> null;
  protected Function<N, Icon> nodeIconFunction;

  protected boolean renderEdgeArrow;

  protected Predicate<E> edgeIncludePredicate = n -> true;

  private static final float directedEdgeLabelCloseness =
      Float.parseFloat(System.getProperty(DIRECTED_EDGE_LABEL_CLOSENESS, "0.65f"));
  private static final float undirectedEdgeLabelCloseness =
      Float.parseFloat(System.getProperty(UNDIRECTED_EDGE_LABEL_CLOSENESS, "0.65f"));
  protected float edgeLabelCloseness;

  protected Function<Context<Graph<N, E>, E>, Shape> edgeShapeFunction;

  protected EdgeIndexFunction<N, E> parallelEdgeIndexFunction;

  protected MultiLayerTransformer multiLayerTransformer = new BasicTransformer();

  /** pluggable support for picking graph elements by finding them based on their coordinates. */
  protected NetworkElementAccessor<N, E> pickSupport;

  protected int labelOffset = LABEL_OFFSET;

  /** the JComponent that this Renderer will display the graph on */
  protected JComponent screenDevice;

  /**
   * The CellRendererPane is used here just as it is in JTree and JTable, to allow a pluggable
   * JLabel-based renderer for Node and Edge label strings and icons.
   */
  protected CellRendererPane rendererPane = new CellRendererPane();

  /** A default GraphLabelRenderer - picked Node labels are blue, picked edge labels are cyan */
  protected NodeLabelRenderer nodeLabelRenderer = new DefaultNodeLabelRenderer(Color.blue);

  protected EdgeLabelRenderer edgeLabelRenderer = new DefaultEdgeLabelRenderer(Color.cyan);

  protected GraphicsDecorator graphicsContext;

  //  private EdgeShape edgeShape;

  DefaultRenderContext(Graph<N, E> graph) {
    this.parallelEdgeIndexFunction = new ParallelEdgeIndexFunction<>();
    setEdgeShape(System.getProperty(EDGE_SHAPE, "QUAD_CURVE"));
    setupArrows(graph.getType().isDirected());
  }

  void setupArrows(boolean directed) {
    if (directed) {
      this.edgeArrow =
          ArrowFactory.getNotchedArrow(edgeArrowWidth, edgeArrowLength, edgeArrowNotchDepth);
      this.renderEdgeArrow = true;
      this.edgeLabelCloseness = directedEdgeLabelCloseness;
    } else {
      this.edgeArrow = ArrowFactory.getWedgeArrow(edgeArrowWidth, edgeArrowLength);
      this.renderEdgeArrow = false;
      this.edgeLabelCloseness = undirectedEdgeLabelCloseness;
    }
  }

  /** @return the nodeShapeFunction */
  public Function<N, Shape> getNodeShapeFunction() {
    return nodeShapeFunction;
  }

  /** @param nodeShapeFunction the nodeShapeFunction to set */
  public void setNodeShapeFunction(Function<N, Shape> nodeShapeFunction) {
    this.nodeShapeFunction = nodeShapeFunction;
  }

  /** @return the nodeStrokeFunction */
  public Function<N, Stroke> getNodeStrokeFunction() {
    return nodeStrokeFunction;
  }

  /** @param nodeStrokeFunction the nodeStrokeFunction to set */
  public void setNodeStrokeFunction(Function<N, Stroke> nodeStrokeFunction) {
    this.nodeStrokeFunction = nodeStrokeFunction;
  }

  public static float[] getDashing() {
    return dashing;
  }

  public static float[] getDotting() {
    return dotting;
  }

  public float getArrowPlacementTolerance() {
    return arrowPlacementTolerance;
  }

  public void setArrowPlacementTolerance(float arrow_placement_tolerance) {
    this.arrowPlacementTolerance = arrow_placement_tolerance;
  }

  public Shape getEdgeArrow() {
    return edgeArrow;
  }

  public void setEdgeArrow(Shape shape) {
    this.edgeArrow = shape;
  }

  public boolean renderEdgeArrow() {
    return this.renderEdgeArrow;
  }

  public void setRenderEdgeArrow(boolean render) {
    this.renderEdgeArrow = render;
  }

  public Function<E, Font> getEdgeFontFunction() {
    return edgeFontFunction;
  }

  public void setEdgeFontFunction(Function<E, Font> edgeFontFunction) {
    this.edgeFontFunction = edgeFontFunction;
  }

  public Predicate<E> getEdgeIncludePredicate() {
    return edgeIncludePredicate;
  }

  public void setEdgeIncludePredicate(Predicate<E> edgeIncludePredicate) {
    this.edgeIncludePredicate = edgeIncludePredicate;
  }

  public float getEdgeLabelCloseness() {
    return edgeLabelCloseness;
  }

  public void setEdgeLabelCloseness(float closeness) {
    this.edgeLabelCloseness = closeness;
  }

  public EdgeLabelRenderer getEdgeLabelRenderer() {
    return edgeLabelRenderer;
  }

  public void setEdgeLabelRenderer(EdgeLabelRenderer edgeLabelRenderer) {
    this.edgeLabelRenderer = edgeLabelRenderer;
  }

  public Function<E, Paint> getEdgeFillPaintFunction() {
    return edgeFillPaintFunction;
  }

  public void setEdgeDrawPaintFunction(Function<E, Paint> edgeDrawPaintFunction) {
    this.edgeDrawPaintFunction = edgeDrawPaintFunction;
  }

  public Function<E, Paint> getEdgeDrawPaintFunction() {
    return edgeDrawPaintFunction;
  }

  public void setEdgeFillPaintFunction(Function<E, Paint> edgeFillPaintFunction) {
    this.edgeFillPaintFunction = edgeFillPaintFunction;
  }

  public Function<Context<Graph<N, E>, E>, Shape> getEdgeShapeFunction() {
    return edgeShapeFunction;
  }

  public void setEdgeShapeFunction(Function<Context<Graph<N, E>, E>, Shape> edgeShapeFunction) {
    this.edgeShapeFunction = edgeShapeFunction;
    if (edgeShapeFunction instanceof ParallelEdgeShapeFunction) {
      @SuppressWarnings("unchecked")
      ParallelEdgeShapeFunction<N, E> function =
          (ParallelEdgeShapeFunction<N, E>) edgeShapeFunction;
      function.setEdgeIndexFunction(this.parallelEdgeIndexFunction);
    }
  }

  public Renderer.NodeLabel.Position getNodeLabelPosition() {
    return nodeLabelPosition;
  }

  public void setNodeLabelPosition(Renderer.NodeLabel.Position nodeLabelPosition) {
    this.nodeLabelPosition = nodeLabelPosition;
  }

  public Function<E, String> getEdgeLabelFunction() {
    return edgeLabelFunction;
  }

  public void setEdgeLabelFunction(Function<E, String> edgeLabelFunction) {
    this.edgeLabelFunction = edgeLabelFunction;
  }

  public Function<E, Stroke> edgeStrokeFunction() {
    return edgeStrokeFunction;
  }

  public void setEdgeStrokeFunction(Function<E, Stroke> edgeStrokeFunction) {
    this.edgeStrokeFunction = edgeStrokeFunction;
  }

  public Function<E, Stroke> getEdgeArrowStrokeFunction() {
    return edgeArrowStrokeFunction;
  }

  public void setEdgeArrowStrokeFunction(Function<E, Stroke> edgeArrowStrokeFunction) {
    this.edgeArrowStrokeFunction = edgeArrowStrokeFunction;
  }

  public GraphicsDecorator getGraphicsContext() {
    return graphicsContext;
  }

  public void setGraphicsContext(GraphicsDecorator graphicsContext) {
    this.graphicsContext = graphicsContext;
  }

  public int getLabelOffset() {
    return labelOffset;
  }

  public void setLabelOffset(int labelOffset) {
    this.labelOffset = labelOffset;
  }

  public EdgeIndexFunction<N, E> getParallelEdgeIndexFunction() {
    return parallelEdgeIndexFunction;
  }

  public void setParallelEdgeIndexFunction(EdgeIndexFunction<N, E> parallelEdgeIndexFunction) {
    this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
    // reset the edge shape Function, as the parallel edge index function
    // is used by it
    this.setEdgeShapeFunction(getEdgeShapeFunction());
  }

  public MutableSelectedState<E> getSelectedEdgeState() {
    return pickedEdgeState;
  }

  public void setSelectedEdgeState(MutableSelectedState<E> pickedEdgeState) {
    this.pickedEdgeState = pickedEdgeState;
  }

  public MutableSelectedState<N> getSelectedNodeState() {
    return pickedNodeState;
  }

  public void setSelectedNodeState(MutableSelectedState<N> pickedNodeState) {
    this.pickedNodeState = pickedNodeState;
  }

  public CellRendererPane getRendererPane() {
    return rendererPane;
  }

  public void setRendererPane(CellRendererPane rendererPane) {
    this.rendererPane = rendererPane;
  }

  public JComponent getScreenDevice() {
    return screenDevice;
  }

  public void setScreenDevice(JComponent screenDevice) {
    this.screenDevice = screenDevice;
    screenDevice.add(rendererPane);
  }

  public Function<N, Font> getNodeFontFunction() {
    return nodeFontFunction;
  }

  public void setNodeFontFunction(Function<N, Font> nodeFontFunction) {
    this.nodeFontFunction = nodeFontFunction;
  }

  public Function<N, Icon> getNodeIconFunction() {
    return nodeIconFunction;
  }

  public void setNodeIconFunction(Function<N, Icon> nodeIconFunction) {
    this.nodeIconFunction = nodeIconFunction;
  }

  public Predicate<N> getNodeIncludePredicate() {
    return nodeIncludePredicate;
  }

  public void setNodeIncludePredicate(Predicate<N> nodeIncludePredicate) {
    this.nodeIncludePredicate = nodeIncludePredicate;
  }

  public NodeLabelRenderer getNodeLabelRenderer() {
    return nodeLabelRenderer;
  }

  public void setNodeLabelRenderer(NodeLabelRenderer nodeLabelRenderer) {
    this.nodeLabelRenderer = nodeLabelRenderer;
  }

  public Function<N, Paint> getNodeFillPaintFunction() {
    return nodeFillPaintFunction;
  }

  public void setNodeFillPaintFunction(Function<N, Paint> nodeFillPaintFunction) {
    this.nodeFillPaintFunction = nodeFillPaintFunction;
  }

  public Function<N, Paint> getNodeDrawPaintFunction() {
    return nodeDrawPaintFunction;
  }

  public void setNodeDrawPaintFunction(Function<N, Paint> nodeDrawPaintFunction) {
    this.nodeDrawPaintFunction = nodeDrawPaintFunction;
  }

  public Function<N, String> getNodeLabelFunction() {
    return nodeLabelFunction;
  }

  public void setNodeLabelFunction(Function<N, String> nodeLabelFunction) {
    this.nodeLabelFunction = nodeLabelFunction;
  }

  public void setNodeLabelDrawPaintFunction(Function<N, Paint> nodeLabelDrawPaintFunction) {
    this.nodeLabelDrawPaintFunction = nodeLabelDrawPaintFunction;
  }

  public Function<N, Paint> getNodeLabelDrawPaintFunction() {
    return nodeLabelDrawPaintFunction;
  }

  public NetworkElementAccessor<N, E> getPickSupport() {
    return pickSupport;
  }

  public void setPickSupport(NetworkElementAccessor<N, E> pickSupport) {
    this.pickSupport = pickSupport;
  }

  public MultiLayerTransformer getMultiLayerTransformer() {
    return multiLayerTransformer;
  }

  public void setMultiLayerTransformer(MultiLayerTransformer basicTransformer) {
    this.multiLayerTransformer = basicTransformer;
  }

  public Function<E, Paint> getArrowDrawPaintFunction() {
    return arrowDrawPaintFunction;
  }

  public Function<E, Paint> getArrowFillPaintFunction() {
    return arrowFillPaintFunction;
  }

  public void setArrowDrawPaintFunction(Function<E, Paint> arrowDrawPaintFunction) {
    this.arrowDrawPaintFunction = arrowDrawPaintFunction;
  }

  public void setArrowFillPaintFunction(Function<E, Paint> arrowFillPaintFunction) {
    this.arrowFillPaintFunction = arrowFillPaintFunction;
  }

  private Shape getNodeShape(String shape, int size) {
    switch (shape) {
      case "SQUARE":
        return new Rectangle2D.Float(-size / 2.f, -size / 2.f, size, size);
      case "CIRCLE":
      default:
        return new Ellipse2D.Float(-size / 2.f, -size / 2.f, size, size);
    }
  }

  /**
   * parse out the node label position
   *
   * @param position
   * @return
   */
  private Renderer.NodeLabel.Position getPosition(String position) {
    try {
      return Renderer.NodeLabel.Position.valueOf(position);
    } catch (Exception e) {
    }
    return Renderer.NodeLabel.Position.SE;
  }

  /**
   * parse out the edge shape
   *
   * @param edgeShape
   */
  private void setEdgeShape(String edgeShape) {
    switch (edgeShape) {
      case "LINE":
        setEdgeShapeFunction(EdgeShape.line());
        break;
      case "CUBIC_CURVE":
        setEdgeShapeFunction(EdgeShape.cubicCurve());
        break;
      case "ORTHOGONAL":
        setEdgeShapeFunction(EdgeShape.orthogonal());
        break;
      case "QUAD_CURVE":
      default:
        setEdgeShapeFunction(EdgeShape.quadCurve());
        break;
    }
  }
}
