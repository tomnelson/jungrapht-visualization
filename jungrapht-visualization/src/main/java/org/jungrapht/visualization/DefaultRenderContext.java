/*
 *Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import com.google.common.base.Preconditions;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.ParallelEdgeShapeFunction;
import org.jungrapht.visualization.layout.GraphElementAccessor;
import org.jungrapht.visualization.renderers.EdgeLabelRenderer;
import org.jungrapht.visualization.renderers.JLabelEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.JLabelVertexLabelRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.renderers.VertexLabelRenderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.ArrowFactory;
import org.jungrapht.visualization.util.Context;
import org.jungrapht.visualization.util.EdgeIndexFunction;
import org.jungrapht.visualization.util.ParallelEdgeIndexFunction;

/**
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class DefaultRenderContext<V, E> implements RenderContext<V, E> {

  // vertex visual property symbols
  private static final String VERTEX_SHAPE = PREFIX + "vertexShape";
  private static final String VERTEX_SIZE = PREFIX + "vertexSize";
  private static final String VERTEX_DRAW_COLOR = PREFIX + "vertexDrawColor";
  private static final String VERTEX_FILL_COLOR = PREFIX + "vertexFillColor";
  private static final String PICKED_VERTEX_COLOR = PREFIX + "pickedVertexFillColor";
  private static final String VERTEX_STROKE_WIDTH = PREFIX + "vertexStrokeWidth";

  // vertex label visual property symbols
  private static final String VERTEX_LABEL_FONT = PREFIX + "vertexLabelFont";
  private static final String VERTEX_LABEL_POSITION = PREFIX + "vertexLabelPosition";
  private static final String VERTEX_LABEL_DRAW_COLOR = PREFIX + "vertexLabelDrawColor";

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

  protected MutableSelectedState<V> pickedVertexState;
  protected MutableSelectedState<E> pickedEdgeState;

  // vertex properties
  private int vertexSize = Integer.getInteger(VERTEX_SIZE, 20);
  private String vertexShapeString = System.getProperty(VERTEX_SHAPE, "CIRCLE");
  private Paint vertexDrawPaint = Color.getColor(VERTEX_DRAW_COLOR, Color.BLACK);
  private Paint vertexFillPaint = Color.getColor(VERTEX_FILL_COLOR, Color.RED);
  private Paint pickedVertexFillPaint = Color.getColor(PICKED_VERTEX_COLOR, Color.YELLOW);
  private Shape vertexShape = getVertexShape(vertexShapeString, vertexSize);

  // vertex functions
  protected Predicate<V> vertexIncludePredicate = n -> true;
  protected Function<V, Stroke> vertexStrokeFunction =
      n -> new BasicStroke(Float.parseFloat(System.getProperty(VERTEX_STROKE_WIDTH, "1.0")));
  protected Function<V, Shape> vertexShapeFunction = n -> vertexShape;

  protected Function<V, Paint> vertexDrawPaintFunction = n -> vertexDrawPaint;
  protected Function<V, Paint> vertexFillPaintFunction =
      n ->
          pickedVertexState != null && pickedVertexState.isSelected(n)
              ? pickedVertexFillPaint
              : vertexFillPaint;

  // vertex label properties
  private Font vertexFont = Font.getFont(VERTEX_LABEL_FONT, new Font("Helvetica", Font.PLAIN, 12));
  private Paint vertexLabelDrawPaint = Color.getColor(VERTEX_LABEL_DRAW_COLOR, Color.BLACK);
  private Renderer.VertexLabel.Position vertexLabelPosition =
      getPosition(System.getProperty(VERTEX_LABEL_POSITION));

  // vertex label functions
  protected Function<V, Font> vertexFontFunction = n -> vertexFont;
  protected Function<V, Paint> vertexLabelDrawPaintFunction = n -> vertexLabelDrawPaint;

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

  protected Function<V, String> vertexLabelFunction = n -> null;
  protected Function<V, Icon> vertexIconFunction;

  protected boolean renderEdgeArrow;

  protected Predicate<E> edgeIncludePredicate = n -> true;

  private static final float directedEdgeLabelCloseness =
      Float.parseFloat(System.getProperty(DIRECTED_EDGE_LABEL_CLOSENESS, "0.65f"));
  private static final float undirectedEdgeLabelCloseness =
      Float.parseFloat(System.getProperty(UNDIRECTED_EDGE_LABEL_CLOSENESS, "0.65f"));
  protected float edgeLabelCloseness;

  protected Function<Context<Graph<V, E>, E>, Shape> edgeShapeFunction;

  protected EdgeIndexFunction<V, E> parallelEdgeIndexFunction;

  protected MultiLayerTransformer multiLayerTransformer = new DefaultTransformer();

  /** pluggable support for picking graph elements by finding them based on their coordinates. */
  protected GraphElementAccessor<V, E> pickSupport;

  protected int labelOffset = LABEL_OFFSET;

  /** the JComponent that this Renderer will display the graph on */
  protected JComponent screenDevice;

  /**
   * The CellRendererPane is used here just as it is in JTree and JTable, to allow a pluggable
   * JLabel-based renderer for Vertex and Edge label strings and icons.
   */
  protected CellRendererPane rendererPane = new CellRendererPane();

  /**
   * A default GraphLabelRenderer - selected Vertex labels are blue, selected edge labels are cyan
   */
  protected VertexLabelRenderer vertexLabelRenderer = new JLabelVertexLabelRenderer(Color.blue);

  protected EdgeLabelRenderer edgeLabelRenderer = new JLabelEdgeLabelRenderer(Color.cyan);

  protected GraphicsDecorator graphicsContext;

  //  private EdgeShape edgeShape;

  DefaultRenderContext(Builder<V, E, ?, ?> builder) {
    this(builder.graph);
  }

  private DefaultRenderContext(Graph<V, E> graph) {
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

  /** @return the vertexShapeFunction */
  public Function<V, Shape> getVertexShapeFunction() {
    return vertexShapeFunction;
  }

  /** @param vertexShapeFunction the vertexShapeFunction to set */
  @Override
  public void setVertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
    this.vertexShapeFunction = vertexShapeFunction;
  }

  /** @return the vertexStrokeFunction */
  public Function<V, Stroke> getVertexStrokeFunction() {
    return vertexStrokeFunction;
  }

  /** @param vertexStrokeFunction the vertexStrokeFunction to set */
  public void setVertexStrokeFunction(Function<V, Stroke> vertexStrokeFunction) {
    this.vertexStrokeFunction = vertexStrokeFunction;
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

  public Function<Context<Graph<V, E>, E>, Shape> getEdgeShapeFunction() {
    return edgeShapeFunction;
  }

  public void setEdgeShapeFunction(Function<Context<Graph<V, E>, E>, Shape> edgeShapeFunction) {
    this.edgeShapeFunction = edgeShapeFunction;
    if (edgeShapeFunction instanceof ParallelEdgeShapeFunction) {
      ParallelEdgeShapeFunction<V, E> function =
          (ParallelEdgeShapeFunction<V, E>) edgeShapeFunction;
      function.setEdgeIndexFunction(this.parallelEdgeIndexFunction);
    }
  }

  public Renderer.VertexLabel.Position getVertexLabelPosition() {
    return vertexLabelPosition;
  }

  public void setVertexLabelPosition(Renderer.VertexLabel.Position vertexLabelPosition) {
    this.vertexLabelPosition = vertexLabelPosition;
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

  public EdgeIndexFunction<V, E> getParallelEdgeIndexFunction() {
    return parallelEdgeIndexFunction;
  }

  public void setParallelEdgeIndexFunction(EdgeIndexFunction<V, E> parallelEdgeIndexFunction) {
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

  public MutableSelectedState<V> getSelectedVertexState() {
    return pickedVertexState;
  }

  public void setSelectedVertexState(MutableSelectedState<V> pickedVertexState) {
    this.pickedVertexState = pickedVertexState;
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

  public Function<V, Font> getVertexFontFunction() {
    return vertexFontFunction;
  }

  public void setVertexFontFunction(Function<V, Font> vertexFontFunction) {
    this.vertexFontFunction = vertexFontFunction;
  }

  public Function<V, Icon> getVertexIconFunction() {
    return vertexIconFunction;
  }

  public void setVertexIconFunction(Function<V, Icon> vertexIconFunction) {
    this.vertexIconFunction = vertexIconFunction;
  }

  public Predicate<V> getVertexIncludePredicate() {
    return vertexIncludePredicate;
  }

  public void setVertexIncludePredicate(Predicate<V> vertexIncludePredicate) {
    this.vertexIncludePredicate = vertexIncludePredicate;
  }

  public VertexLabelRenderer getVertexLabelRenderer() {
    return vertexLabelRenderer;
  }

  public void setVertexLabelRenderer(VertexLabelRenderer vertexLabelRenderer) {
    this.vertexLabelRenderer = vertexLabelRenderer;
  }

  public Function<V, Paint> getVertexFillPaintFunction() {
    return vertexFillPaintFunction;
  }

  public void setVertexFillPaintFunction(Function<V, Paint> vertexFillPaintFunction) {
    this.vertexFillPaintFunction = vertexFillPaintFunction;
  }

  public Function<V, Paint> getVertexDrawPaintFunction() {
    return vertexDrawPaintFunction;
  }

  public void setVertexDrawPaintFunction(Function<V, Paint> vertexDrawPaintFunction) {
    this.vertexDrawPaintFunction = vertexDrawPaintFunction;
  }

  public Function<V, String> getVertexLabelFunction() {
    return vertexLabelFunction;
  }

  public void setVertexLabelFunction(Function<V, String> vertexLabelFunction) {
    this.vertexLabelFunction = vertexLabelFunction;
  }

  public void setVertexLabelDrawPaintFunction(Function<V, Paint> vertexLabelDrawPaintFunction) {
    this.vertexLabelDrawPaintFunction = vertexLabelDrawPaintFunction;
  }

  public Function<V, Paint> getVertexLabelDrawPaintFunction() {
    return vertexLabelDrawPaintFunction;
  }

  public GraphElementAccessor<V, E> getPickSupport() {
    return pickSupport;
  }

  public void setPickSupport(GraphElementAccessor<V, E> pickSupport) {
    Preconditions.checkNotNull(pickSupport);
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

  private Shape getVertexShape(String shape, int size) {
    switch (shape) {
      case "SQUARE":
        return new Rectangle2D.Float(-size / 2.f, -size / 2.f, size, size);
      case "CIRCLE":
      default:
        return new Ellipse2D.Float(-size / 2.f, -size / 2.f, size, size);
    }
  }

  /**
   * parse out the vertex label position
   *
   * @param position position as a String
   * @return the {@link Renderer.VertexLabel.Position}
   */
  private Renderer.VertexLabel.Position getPosition(String position) {
    try {
      return Renderer.VertexLabel.Position.valueOf(position);
    } catch (Exception e) {
    }
    return Renderer.VertexLabel.Position.SE;
  }

  /**
   * parse out the edge shape
   *
   * @param edgeShape edge shape as a String
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
