/*
 *Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.ParallelEdgeShapeFunction;
import org.jungrapht.visualization.layout.GraphElementAccessor;
import org.jungrapht.visualization.layout.event.RenderContextStateChange;
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

  /** event support for changes in the RenderContext state */
  private RenderContextStateChange.Support<V, E> renderContextStateChangeSupport =
      RenderContextStateChange.Support.create();

  /**
   * getter for event support. Caller can add itself as a listener to RenderContextStateChange
   * events
   *
   * @return theRenderContextStateChangeSupport that will fire events to listeners
   */
  @Override
  public RenderContextStateChange.Support getRenderContextStateChangeSupport() {
    return renderContextStateChangeSupport;
  }

  /**
   * Supplies Shapes for vertices by checking various properties
   *
   * @param <V>
   */
  public static class ShapeFunctionSupplier<V> implements Supplier<Function<V, Shape>> {
    /**
     * determine Shape and size from properties
     *
     * @return the same Shape for any vertex
     */
    @Override
    public Function<V, Shape> get() {
      int vertexSize = Integer.getInteger(VERTEX_SIZE, 20);
      String vertexShapeString = System.getProperty(VERTEX_SHAPE, "CIRCLE");
      Shape vertexShape = DefaultRenderContext.getVertexShape(vertexShapeString, vertexSize);
      return v -> vertexShape;
    }
  }
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
  public static final String EDGE_WIDTH = PREFIX + "edgeWidth";
  private static final String EDGE_STROKE = PREFIX + "edgeStroke";

  // edge label visual property symbols
  private static final String EDGE_LABEL_FONT = PREFIX + "edgeLabelFont";
  private static final String EDGE_LABEL_CLOSENESS = PREFIX + "edgeLabelCloseness";

  // edge arrow visual property symbols
  private static final String ARROW_STYLE = PREFIX + "arrowStyle";
  private static final String EDGE_ARROW_LENGTH = PREFIX + "edgeArrowLength";
  private static final String EDGE_ARROW_WIDTH = PREFIX + "edgeArrowWidth";
  private static final String EDGE_ARROW_NOTCH_DEPTH = PREFIX + "edgeArrowNotchDepth";
  private static final String EDGE_ARROW_STROKE = PREFIX + "edgeArrowStroke";
  private static final String ARROW_PLACEMENT_TOLERANCE = PREFIX + "arrowPlacementTolerance";
  private static final String ARROWS_ON_UNDIRECTED_EDGES = PREFIX + "arrowsOnUndirectedEdges";

  /** Holds a subset of vertices that are selected */
  protected MutableSelectedState<V> selectedVertexState;
  /** Holds asubset of edges that are selected */
  protected MutableSelectedState<E> selectedEdgeState;

  /** the {@link Stroke} used to draw edges */
  protected Stroke edgeStroke;

  protected float edgeStrokeWidth = Float.parseFloat(System.getProperty(EDGE_WIDTH, "1.0f"));

  // vertex properties
  private int vertexSize = Integer.getInteger(VERTEX_SIZE, 20);
  private String vertexShapeString = System.getProperty(VERTEX_SHAPE, "CIRCLE");
  private Paint vertexDrawPaint = Color.getColor(VERTEX_DRAW_COLOR, Color.BLACK);
  private Paint vertexFillPaint = Color.getColor(VERTEX_FILL_COLOR, Color.RED);
  private Paint selectedVertexFillPaint = Color.getColor(PICKED_VERTEX_COLOR, Color.YELLOW);
  private Shape vertexShape = getVertexShape(vertexShapeString, vertexSize);

  // vertex functions
  /** Implement to limit which vertices are rendered */
  protected Predicate<V> vertexIncludePredicate = n -> true;
  /** the {@link Stroke} used to draw (outline) vertex shapes */
  protected Function<V, Stroke> vertexStrokeFunction =
      n -> new BasicStroke(Float.parseFloat(System.getProperty(VERTEX_STROKE_WIDTH, "1.0")));
  /** implement to provide Shapes for vertices */
  protected Function<V, Shape> vertexShapeFunction = n -> vertexShape;

  /** implement to provide outline color for vertices */
  protected Function<V, Paint> vertexDrawPaintFunction = n -> vertexDrawPaint;
  /**
   * implement to provide fill color for vertices. Default version uses default fill paint and
   * selected vertex fill paing
   */
  protected Function<V, Paint> vertexFillPaintFunction =
      n ->
          selectedVertexState != null && selectedVertexState.isSelected(n)
              ? selectedVertexFillPaint
              : vertexFillPaint;

  // vertex label properties
  private Font vertexFont = Font.getFont(VERTEX_LABEL_FONT, new Font("Helvetica", Font.PLAIN, 12));
  private Paint vertexLabelDrawPaint = Color.getColor(VERTEX_LABEL_DRAW_COLOR, Color.BLACK);
  private Renderer.VertexLabel.Position vertexLabelPosition =
      getPosition(System.getProperty(VERTEX_LABEL_POSITION));

  // vertex label functions
  /** implement to provide {@code Font}s for vertices */
  protected Function<V, Font> vertexFontFunction = n -> vertexFont;
  /** implement to provide Colors for vertex labels */
  protected Function<V, Paint> vertexLabelDrawPaintFunction = n -> vertexLabelDrawPaint;

  // edge properties
  private float edgeWidth = Float.parseFloat(System.getProperty(EDGE_WIDTH, "1.0f"));
  private Color pickedEdgePaint = Color.getColor(PICKED_EDGE_COLOR, Color.CYAN);
  private Color edgePaint = Color.getColor(EDGE_COLOR, Color.BLACK);

  // edge functions
  /** implement to provide {@code Stroke}s for edges */
  protected Function<E, Stroke> edgeStrokeFunction = e -> edgeStroke;
  /** implement to provide fill color for edges (rarely useful) */
  protected Function<E, Paint> edgeFillPaintFunction = n -> null;
  /** implement to provide draw {@Paint}s for edges */
  protected Function<E, Paint> edgeDrawPaintFunction =
      e ->
          selectedEdgeState != null && selectedEdgeState.isSelected(e)
              ? pickedEdgePaint
              : edgePaint;

  // edge label properties
  private Font edgeLabelFont = Font.getFont(EDGE_LABEL_FONT, new Font("Helvetica", Font.PLAIN, 12));

  // edge label functions
  /** implement to provide {@Font}s for edge labels */
  protected Function<E, Font> edgeFontFunction = n -> edgeLabelFont;
  /** implement to provide edge labels */
  protected Function<E, String> edgeLabelFunction = e -> null;

  // edge arrow properties
  private int edgeArrowLength = Integer.getInteger(EDGE_ARROW_LENGTH, 10);
  private int edgeArrowWidth = Integer.getInteger(EDGE_ARROW_WIDTH, 8);
  private double edgeArrowNotchDepth =
      Double.parseDouble(System.getProperty(EDGE_ARROW_NOTCH_DEPTH, "0.4"));
  protected Shape edgeArrow;
  protected float arrowPlacementTolerance =
      Float.parseFloat(System.getProperty(ARROW_PLACEMENT_TOLERANCE, "1.0"));
  protected Stroke edgeArrowStroke =
      new BasicStroke(Float.parseFloat(System.getProperty(EDGE_ARROW_STROKE, "1.0")));

  // edge arrow functions
  /** implement to provide {@code Stroke}s for edge arrows */
  protected Function<E, Stroke> edgeArrowStrokeFunction = e -> edgeArrowStroke;
  /** implement to provide {@code Paint}s for edge arrows */
  protected Function<E, Paint> arrowFillPaintFunction =
      e ->
          selectedEdgeState != null && selectedEdgeState.isSelected(e)
              ? pickedEdgePaint
              : edgePaint;
  /** implement to provide {@code Paint}s for edge arrow outline */
  protected Function<E, Paint> arrowDrawPaintFunction =
      e ->
          selectedEdgeState != null && selectedEdgeState.isSelected(e)
              ? pickedEdgePaint
              : edgePaint;
  /**
   * when {@code true}, draws arrows at both ends of undirected edges default is {@code false} - no
   * arrows drawn for undirected edges
   */
  protected boolean arrowsOnUndirectedEdges =
      Boolean.parseBoolean(System.getProperty(ARROWS_ON_UNDIRECTED_EDGES, "false"));

  /** provide labels for vertices */
  protected Function<V, String> vertexLabelFunction = n -> null;

  protected Function<V, Icon> vertexIconFunction;

  protected boolean renderEdgeArrow;

  protected Predicate<E> edgeIncludePredicate = n -> true;

  private float edgeLabelCloseness =
      Float.parseFloat(System.getProperty(EDGE_LABEL_CLOSENESS, "0.65f"));

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

  DefaultRenderContext() {
    this.parallelEdgeIndexFunction = new ParallelEdgeIndexFunction<>();
    setEdgeShape(System.getProperty(EDGE_SHAPE, "QUAD_CURVE"));
    this.edgeWidth = Float.parseFloat(System.getProperty(EDGE_WIDTH, "1.0f"));
    setEdgeStroke(System.getProperty(EDGE_STROKE, "LINE"), edgeWidth);
  }

  /**
   * sets the arrow functions, depending on the graph type
   *
   * @param {@code Graph} if the {@link Graph} is a directed graph
   */
  @Override
  public void setupArrows(boolean directed) {
    if (directed) {
      this.edgeArrow =
          ArrowFactory.getNotchedArrow(
              edgeArrowWidth, edgeArrowLength, (int) (edgeArrowLength * edgeArrowNotchDepth));
      this.renderEdgeArrow = true;
    } else {
      this.edgeArrow = ArrowFactory.getWedgeArrow(edgeArrowWidth, edgeArrowLength);
      this.renderEdgeArrow = arrowsOnUndirectedEdges;
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
    this.renderContextStateChangeSupport.fireRenderContextStateChanged(this, true);
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
    return this.renderEdgeArrow || arrowsOnUndirectedEdges;
  }

  public void setRenderEdgeArrow(boolean render) {
    this.renderEdgeArrow = render;
  }

  @Override
  public void setEdgeArrowWidth(int edgeArrowWidth) {
    this.edgeArrowWidth = edgeArrowWidth;
  }

  @Override
  public int getEdgeArrowWidth() {
    return this.edgeArrowWidth;
  }

  @Override
  public void setEdgeArrowLength(int edgeArrowLength) {
    this.edgeArrowLength = edgeArrowLength;
  }

  @Override
  public int getEdgeArrowLength() {
    return this.edgeArrowLength;
  }

  @Override
  public void setArrowsOnUndirectedEdges(boolean arrowsOnUndirectedEdges) {
    this.arrowsOnUndirectedEdges = arrowsOnUndirectedEdges;
  }

  @Override
  public boolean getArrowsOnUndirectedEdges() {
    return this.arrowsOnUndirectedEdges;
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
    this.renderContextStateChangeSupport.fireRenderContextStateChanged(this, true);
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

  public void setEdgeWidth(float edgeWidth) {
    this.edgeWidth = edgeWidth;
  }

  public float getEdgeWidth() {
    return edgeWidth;
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
    return selectedEdgeState;
  }

  public void setSelectedEdgeState(MutableSelectedState<E> pickedEdgeState) {
    this.selectedEdgeState = pickedEdgeState;
  }

  public MutableSelectedState<V> getSelectedVertexState() {
    return selectedVertexState;
  }

  public void setSelectedVertexState(MutableSelectedState<V> pickedVertexState) {
    this.selectedVertexState = pickedVertexState;
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
    Objects.requireNonNull(pickSupport);
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

  private static Shape getVertexShape(String shape, int size) {
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

  /**
   * parse out the edge stroke (LINE, DASHED, DOTTED
   *
   * @param edgeStroke Stroke to set
   */
  private void setEdgeStroke(String edgeStroke, float width) {
    switch (edgeStroke) {
      case "DOTTED":
        this.edgeStroke =
            new BasicStroke(
                width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dotting, 0f);
        break;
      case "DASHED":
        this.edgeStroke =
            new BasicStroke(
                width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1.0f, dashing, 0f);
        break;
      case "LINE":
      default:
        this.edgeStroke = new BasicStroke(width);
    }
  }
}
