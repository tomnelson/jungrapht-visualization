package org.jungrapht.visualization.annotations;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.PropertyLoader;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.decorators.ExpandXY;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;

/** Paints a highlight under selected edges */
public class SelectedEdgePaintable<V, E> implements VisualizationServer.Paintable {

  static {
    PropertyLoader.load();
  }

  /**
   * builder for the {@code SelectedEdgePaintable}
   *
   * @param <V> the vertex type
   */
  public static class Builder<V, E, B extends Builder<V, E, B>> {

    private final VisualizationServer<V, E> visualizationServer;
    private Function<E, Paint> selectionPaintFunction = e -> Color.red;
    private float selectionStrokeMultiplier = 2.f;
    private BasicStroke selectionStroke =
        new BasicStroke(
            1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {20, 5, 10, 5}, 0);
    private Function<VisualizationServer<V, E>, Collection<E>> selectedEdgeFunction;
    private double minScale = 0.2;

    protected B self() {
      return (B) this;
    }
    /**
     * @param selectionPaintFunction provides the color to draw the selected edge indicator
     * @return this builder
     */
    public B selectionPaintFunction(Function<E, Paint> selectionPaintFunction) {
      this.selectionPaintFunction = selectionPaintFunction;
      return self();
    }

    /**
     * makes the edge highlight wider by this factor
     *
     * @param selectionStrokeMultiplier
     * @return this builder
     */
    public B selectionStrokeMultiplier(float selectionStrokeMultiplier) {
      this.selectionStrokeMultiplier = selectionStrokeMultiplier;
      return self();
    }

    /**
     * configures the {@code Stroke} to use to highlight selected edges
     *
     * @param selectionStroke the {@code Stroke} to use
     * @return this builder
     */
    public B selectionStroke(BasicStroke selectionStroke) {
      this.selectionStroke = selectionStroke;
      return self();
    }

    /**
     * Provides the currently selected edges
     *
     * @param selectedEdgeFunction
     * @return this builder
     */
    public B selectedEdgeFunction(
        Function<VisualizationServer<V, E>, Collection<E>> selectedEdgeFunction) {
      this.selectedEdgeFunction = selectedEdgeFunction;
      return self();
    }

    public B minScale(double minScale) {
      this.minScale = minScale;
      return self();
    }
    /** @return a new instance of a {@code SelectedEdgePaintable} */
    public SelectedEdgePaintable<V, E> build() {
      return new SelectedEdgePaintable<>(this);
    }

    /** @param visualizationServer the (required) {@code VisualizationServer} parameter */
    private Builder(VisualizationServer<V, E> visualizationServer) {
      this.visualizationServer = visualizationServer;
    }
  }

  /**
   * @param visualizationServer the (required) {@code VisualizationServer} parameter
   * @param <V> the vertex type
   * @param <E> the edge type
   * @return the {@code Builder} used to create the instance of a {@code SelectedEdgePaintable}
   */
  public static <V, E> Builder<V, E, ?> builder(VisualizationServer<V, E> visualizationServer) {
    return new Builder<>(visualizationServer);
  }

  /** the (required) {@code VisualizationServer} */
  private final VisualizationServer<V, E> visualizationServer;

  private Function<E, Paint> selectionPaintFunction;

  private float selectionStrokeMultiplier;

  private BasicStroke selectionStroke;

  protected Function<VisualizationServer<V, E>, Collection<E>> selectedEdgeFunction;

  private boolean useTransform = true;

  private double minScale;

  /**
   * Create an instance of a {@code SelectedEdgePaintable}
   *
   * @param builder the {@code Builder} to provide parameters to the {@code SelectedEdgePaintable}
   */
  private SelectedEdgePaintable(Builder<V, E, ?> builder) {
    this(
        builder.visualizationServer,
        builder.selectionPaintFunction,
        builder.selectionStrokeMultiplier,
        builder.selectionStroke,
        builder.minScale,
        builder.selectedEdgeFunction);
  }

  /**
   * Create an instance of a {@code SelectedEdgePaintable}
   *
   * @param visualizationServer provides access to visualization elements
   * @param selectionPaintFunction supplies the color for selected edge highlighting
   * @param selectionStrokeMultiplier supplies a multiplier to make selection highlight wider
   * @param selectionStroke provides a custom stroke for highlighted edge
   * @param selectedEdgeFunction supplies the edges that are selected
   */
  private SelectedEdgePaintable(
      VisualizationServer<V, E> visualizationServer,
      Function<E, Paint> selectionPaintFunction,
      float selectionStrokeMultiplier,
      BasicStroke selectionStroke,
      double minScale,
      Function<VisualizationServer<V, E>, Collection<E>> selectedEdgeFunction) {
    this.visualizationServer = visualizationServer;
    this.selectionPaintFunction = selectionPaintFunction;
    this.selectionStrokeMultiplier = selectionStrokeMultiplier;
    this.selectionStroke = selectionStroke;
    this.minScale = minScale;
    this.selectedEdgeFunction =
        selectedEdgeFunction != null ? selectedEdgeFunction : vs -> getSelectedEdges(vs);
  }

  /**
   * supplies the selected edges that are not hidden
   *
   * @param visualizationServer provides the edges and predicate for edge inclusion
   * @return
   */
  protected Collection<E> getSelectedEdges(VisualizationServer<V, E> visualizationServer) {
    LayoutModel<V> layoutModel = visualizationServer.getVisualizationModel().getLayoutModel();
    RenderContext<V, E> renderContext = visualizationServer.getRenderContext();
    return visualizationServer
        .getSelectedEdges()
        .stream()
        .filter(
            e -> {
              // don't highlight edge if either incident vertex is not drawn
              V u = layoutModel.getGraph().getEdgeSource(e);
              V v = layoutModel.getGraph().getEdgeTarget(e);
              Predicate<V> vertexIncludePredicate = renderContext.getVertexIncludePredicate();
              Predicate<E> edgeIncludePredicate = renderContext.getEdgeIncludePredicate();
              return edgeIncludePredicate.test(e)
                  && vertexIncludePredicate.test(u)
                  && vertexIncludePredicate.test(v);
            })
        .collect(Collectors.toList());
  }

  /**
   * Draw shapes to indicate selected vertices
   *
   * @param g the {@code Graphics} to draw with
   */
  @Override
  public void paint(Graphics g) {

    getSelectedEdges(visualizationServer)
        .stream()
        .forEach(
            edge ->
                paintEdgeHighlight(
                    visualizationServer.getRenderContext(),
                    visualizationServer.getVisualizationModel().getLayoutModel(),
                    edge));
  }

  protected void paintEdgeHighlight(
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e) {
    Graphics2D g2d = renderContext.getGraphicsContext().getDelegate();
    Stroke savedStroke = visualizationServer.getRenderContext().getEdgeStrokeFunction().apply(e);
    if (savedStroke instanceof BasicStroke) {
      BasicStroke savedBasicStroke = (BasicStroke) savedStroke;
      // if the viewScale is small, the highlight will be difficult to see
      // use the viewScale below to enhance the highlight
      float viewScale =
          (float)
              renderContext
                  .getMultiLayerTransformer()
                  .getTransformer(MultiLayerTransformer.Layer.VIEW)
                  .getScale();

      Stroke stroke =
          new BasicStroke(
              savedBasicStroke.getLineWidth() * selectionStrokeMultiplier / viewScale,
              selectionStroke.getEndCap(),
              selectionStroke.getLineJoin(),
              selectionStroke.getMiterLimit(),
              selectionStroke.getDashArray(),
              selectionStroke.getDashPhase());
      g2d.setStroke(stroke);
    }

    int[] coords = new int[4];
    boolean[] loop = new boolean[1];
    Shape edgeShape = prepareFinalEdgeShape(renderContext, layoutModel, e, coords, loop);

    GraphicsDecorator g = renderContext.getGraphicsContext();

    Paint oldPaint = g.getPaint();

    g.setPaint(selectionPaintFunction.apply(e));

    g.draw(edgeShape);

    // restore old paint and stroke
    g.setPaint(oldPaint);
    g2d.setStroke(savedStroke);
  }

  protected Shape prepareFinalEdgeShape(
      RenderContext<V, E> renderContext,
      LayoutModel<V> layoutModel,
      E e,
      int[] coords,
      boolean[] loop) {
    V source = layoutModel.getGraph().getEdgeSource(e);
    V target = layoutModel.getGraph().getEdgeTarget(e);

    Point sourcePoint = layoutModel.apply(source);
    Point targetPoint = layoutModel.apply(target);
    Point2D sourcePoint2D =
        renderContext
            .getMultiLayerTransformer()
            .transform(
                MultiLayerTransformer.Layer.LAYOUT,
                new Point2D.Double(sourcePoint.x, sourcePoint.y));
    Point2D targetPoint2D =
        renderContext
            .getMultiLayerTransformer()
            .transform(
                MultiLayerTransformer.Layer.LAYOUT,
                new Point2D.Double(targetPoint.x, targetPoint.y));
    float sourcePoint2DX = (float) sourcePoint2D.getX();
    float sourcePoint2DY = (float) sourcePoint2D.getY();
    float targetPoint2DX = (float) targetPoint2D.getX();
    float targetPoint2DY = (float) targetPoint2D.getY();
    coords[0] = (int) sourcePoint2DX;
    coords[1] = (int) sourcePoint2DY;
    coords[2] = (int) targetPoint2DX;
    coords[3] = (int) targetPoint2DY;

    boolean isLoop = loop[0] = source.equals(target);
    Shape targetShape = renderContext.getVertexShapeFunction().apply(target);
    Shape edgeShape = getEdgeShape(renderContext.getEdgeShapeFunction(), e, layoutModel.getGraph());

    AffineTransform xform = AffineTransform.getTranslateInstance(sourcePoint2DX, sourcePoint2DY);

    if (isLoop) {
      // this is a self-loop. scale it is larger than the vertex
      // it decorates and translate it so that its nadir is
      // at the center of the vertex.
      Rectangle2D targetShapeBounds2D = targetShape.getBounds2D();
      xform.scale(targetShapeBounds2D.getWidth(), targetShapeBounds2D.getHeight());
      xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
    } else {
      // this is a normal edge. Rotate it to the angle between
      // vertex endpoints, then scale it to the distance between
      // the vertices
      float dx = targetPoint2DX - sourcePoint2DX;
      float dy = targetPoint2DY - sourcePoint2DY;
      float thetaRadians = (float) Math.atan2(dy, dx);
      xform.rotate(thetaRadians);
      double dist = Math.sqrt(dx * dx + dy * dy);

      if (edgeShape instanceof ExpandXY) {
        // this is for the Articulated edges in the min cross layouts
        // and (future) orthogonal layout edges
        MutableTransformer layoutTransformer =
            renderContext
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
        double scaleX = layoutTransformer.getScaleX();
        double scaleY = layoutTransformer.getScaleY();
        // account for any single axis scaling by mutating the 'Y' coords of the articulated edge
        AffineTransform singleAxisScalingTransform =
            AffineTransform.getScaleInstance(1, scaleX / scaleY);
        edgeShape = singleAxisScalingTransform.createTransformedShape(edgeShape);
        xform.scale(dist, dist);
      } else {
        // all other edges are scaled only in the x axis (to span from vertex to vertex)
        xform.scale(dist, 1.0);
      }
    }
    edgeShape = xform.createTransformedShape(edgeShape);

    return edgeShape;
  }

  protected Shape getEdgeShape(
      BiFunction<Graph<V, E>, E, Shape> edgeShapeFunction, E edge, Graph<V, E> graph) {
    if (edgeShapeFunction instanceof EdgeShape.ArticulatedLine) {
      return edgeShapeFunction.apply(graph, edge);
    } else {
      return EdgeShape.<V, E>line().apply(graph, edge);
    }
  }

  @Override
  public boolean useTransform() {
    return useTransform;
  }
}
