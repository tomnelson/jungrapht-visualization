package org.jungrapht.visualization.annotations;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;
import static org.jungrapht.visualization.renderers.BiModalRenderer.LIGHTWEIGHT;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.PropertyLoader;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.renderers.*;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.transform.shape.TransformingGraphics;
import org.jungrapht.visualization.util.ArrowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Paints a highlight at the location of all selected vertices.
 *
 * @param <V> the vertex type
 * @author Tom Nelson
 */
public class MultiSelectedVertexPaintable<V, E> implements VisualizationServer.Paintable {

  private static final Logger log = LoggerFactory.getLogger(MultiSelectedVertexPaintable.class);

  static {
    PropertyLoader.load();
  }
  /**
   * builder for the {@code SelectedVertexPaintable}
   *
   * @param <V> the vertex type
   */
  public static class Builder<V, E, B extends Builder<V, E, B>> {

    private final VisualizationServer<V, E> visualizationServer;
    private Shape selectionShape =
        AffineTransform.getRotateInstance(3 * Math.PI / 4)
            .createTransformedShape(ArrowFactory.getNotchedArrow(20, 24, 8));
    private Icon selectionIcon;
    private Paint selectionPaint = Color.getColor(PREFIX + "selectionColor", Color.red);
    private float selectionStrokeMin =
        Float.parseFloat(System.getProperty(PREFIX + "selectionStrokeMin", "10.f"));
    private boolean useBounds =
        Boolean.parseBoolean(System.getProperty(PREFIX + "selectionShapeUseBounds", "true"));
    private boolean useOval =
        Boolean.parseBoolean(System.getProperty(PREFIX + "selectionShapeUseOval", "false"));
    private double highlightScale =
        Double.parseDouble(System.getProperty(PREFIX + "selectionHighlightScale", "1.12"));
    private boolean fillHighlight =
        Boolean.parseBoolean(System.getProperty(PREFIX + "selectionShapeFill", "true"));
    private Function<VisualizationServer<V, E>, Collection<V>> selectedVertexFunction;

    protected B self() {
      return (B) this;
    }
    /**
     * @param selectionShape the shape to draw as an indicator for selected vertices
     * @return this builder
     */
    public B selectionShape(Shape selectionShape) {
      this.selectionShape = selectionShape;
      return self();
    }

    public B selectionIcon(Icon selectionIcon) {
      this.selectionIcon = selectionIcon;
      return self();
    }
    /**
     * @param selectionPaint the color to draw the selected vertex indicator
     * @return this builder
     */
    public B selectionPaint(Paint selectionPaint) {
      this.selectionPaint = selectionPaint;
      return self();
    }

    public B selectionStrokeMin(float selectionStrokeMin) {
      this.selectionStrokeMin = selectionStrokeMin;
      return self();
    }

    public B highlightScale(double highlightScale) {
      this.highlightScale = highlightScale;
      return self();
    }

    public B useBounds(boolean useBounds) {
      this.useBounds = useBounds;
      return self();
    }

    public B useOval(boolean useOval) {
      this.useOval = useOval;
      return self();
    }

    public B fillHighlight(boolean fillHighlight) {
      this.fillHighlight = fillHighlight;
      return self();
    }

    public B selectedVertexFunction(
        Function<VisualizationServer<V, E>, Collection<V>> selectedVertexFunction) {
      this.selectedVertexFunction = selectedVertexFunction;
      return self();
    }

    /** @return a new instance of a {@code SelectedVertexPaintable} */
    public MultiSelectedVertexPaintable<V, E> build() {
      return new MultiSelectedVertexPaintable<>(this);
    }

    /** @param visualizationServer the (required) {@code VisualizationServer} parameter */
    private Builder(VisualizationServer<V, E> visualizationServer) {
      this.visualizationServer = visualizationServer;
    }
  }

  /**
   * @param visualizationServer the (required) {@code VisualizationServer} parameter
   * @param <V> the vertex type
   * @return the {@code Builder} used to create the instance of a {@code SelectedVertexPaintable}
   */
  public static <V, E> Builder<V, E, ?> builder(VisualizationServer<V, E> visualizationServer) {
    return new Builder<>(visualizationServer);
  }

  /** the (required) {@code VisualizationServer} */
  private final VisualizationServer<V, E> visualizationServer;
  /** the {@code Shape} to paint to indicate selected vertices */
  private Shape selectionShape;
  /** the {@code Paint} to use to draw the selected vertex indicating {@code Shape} */
  private Paint selectionPaint;

  private Icon selectionIcon;

  private boolean useBounds;

  private boolean useOval;

  private double highlightScale;

  private boolean fillHighlight;

  private float selectionStrokeMin;

  private BiModalSelectionRenderer<V, E> biModalRenderer;

  protected Function<VisualizationServer<V, E>, Collection<V>> selectedVertexFunction;

  /**
   * Create an instance of a {@code SelectedVertexPaintable}
   *
   * @param builder the {@code Builder} to provide parameters to the {@code SelectedVertexPaintable}
   */
  private MultiSelectedVertexPaintable(Builder<V, E, ?> builder) {
    this(
        builder.visualizationServer,
        builder.selectionShape,
        builder.useBounds,
        builder.useOval,
        builder.highlightScale,
        builder.fillHighlight,
        builder.selectionPaint,
        builder.selectionIcon,
        builder.selectionStrokeMin,
        builder.selectedVertexFunction);
  }

  /**
   * Create an instance of a {@code SelectedVertexPaintable}
   *
   * @param visualizationServer the (required) {@code VisualizationServer}
   * @param shape the {@code Shape} to paint to indicate selected vertices
   * @param selectionPaint the {@code Paint} to use to draw the selected vertex indicating {@code
   *     Shape}
   */
  private MultiSelectedVertexPaintable(
      VisualizationServer<V, E> visualizationServer,
      Shape shape,
      boolean useBounds,
      boolean useOval,
      double highlightScale,
      boolean fillHighlight,
      Paint selectionPaint,
      Icon selectionIcon,
      float selectionStrokeMin,
      Function<VisualizationServer<V, E>, Collection<V>> selectedVertexFunction) {
    this.visualizationServer = visualizationServer;
    this.selectionShape = shape;
    this.useBounds = useBounds;
    this.useOval = useOval;
    this.highlightScale = highlightScale;
    this.fillHighlight = fillHighlight;
    this.selectionPaint = selectionPaint;
    this.selectionIcon = selectionIcon;
    this.selectionStrokeMin = selectionStrokeMin;
    this.biModalRenderer =
        BiModalSelectionRenderer.<V, E>builder()
            .component(visualizationServer.getComponent())
            .lightweightRenderer(
                new SelectionRenderer<>(new LightweightVertexSelectionRenderer<>()))
            .heavyweightRenderer(
                (new SelectionRenderer<>(new HeavyweightVertexSelectionRenderer<>())))
            .modeSourceRenderer((BiModalRenderer<V, E>) visualizationServer.getRenderer())
            .build();
    this.selectedVertexFunction =
        selectedVertexFunction != null ? selectedVertexFunction : vs -> getSelectedVertices(vs);
  }

  protected Collection<V> getSelectedVertices(VisualizationServer<V, E> visualizationServer) {
    return visualizationServer
        .getSelectedVertices()
        .stream()
        .filter(v -> visualizationServer.getRenderContext().getVertexIncludePredicate().test(v))
        .collect(Collectors.toList());
  }

  /**
   * Draw shapes to indicate selected vertices
   *
   * @param g the {@code Graphics} to draw with
   */
  @Override
  public void paint(Graphics g) {
    // get the g2d
    Graphics2D g2d = (Graphics2D) g;
    // save off old Paint and AffineTransform
    Paint oldPaint = g2d.getPaint();
    AffineTransform oldTransform = g2d.getTransform();
    // set the new color
    g2d.setPaint(selectionPaint);

    Collection<V> selectedVertices = selectedVertexFunction.apply(visualizationServer);

    if (!selectedVertices.isEmpty()) {

      GraphicsDecorator graphicsDecorator =
          visualizationServer.getRenderContext().getGraphicsContext();

      Stroke savedStroke = g2d.getStroke();
      float strokeWidth =
          (float) Math.max(selectionStrokeMin, selectionStrokeMin / g2d.getTransform().getScaleX());
      g2d.setStroke(new BasicStroke(strokeWidth));
      if (graphicsDecorator instanceof TransformingGraphics) {
        // get a copy of the current transform used by g2d
        AffineTransform graphicsTransformCopy = new AffineTransform(oldTransform);

        AffineTransform viewTransform =
            visualizationServer
                .getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.VIEW)
                .getTransform();

        // don't mutate the viewTransform!
        graphicsTransformCopy.concatenate(viewTransform);
        g2d.setTransform(graphicsTransformCopy);
        selectedVertices
            .stream()
            .filter(visualizationServer.getRenderContext().getVertexIncludePredicate()::test)
            .forEach(vertex -> paintTransformed(vertex));

      } else {
        selectedVertices
            .stream()
            .filter(visualizationServer.getRenderContext().getVertexIncludePredicate()::test)
            .forEach(
                vertex ->
                    paintIconForVertex(
                        visualizationServer.getRenderContext(),
                        visualizationServer.getVisualizationModel(),
                        vertex));
      }
      // put back the old values
      g2d.setPaint(oldPaint);
      g2d.setStroke(savedStroke);
      g2d.setTransform(oldTransform);
    }
  }

  protected void paintSingleTransformed(V vertex) {
    Function<V, Shape> oldShapeFunction =
        visualizationServer.getRenderContext().getVertexShapeFunction();
    visualizationServer.getRenderContext().setVertexShapeFunction(v -> selectionShape);
    Function<V, Shape> oldLightweightShapeFunction =
        ((LightweightVertexSelectionRenderer) biModalRenderer.getVertexRenderer(LIGHTWEIGHT))
            .getVertexShapeFunction();
    ((LightweightVertexSelectionRenderer) biModalRenderer.getVertexRenderer(LIGHTWEIGHT))
        .setVertexShapeFunction(v -> selectionShape);

    biModalRenderer.renderVertex(
        visualizationServer.getRenderContext(),
        visualizationServer.getVisualizationModel().getLayoutModel(),
        vertex);

    visualizationServer.getRenderContext().setVertexShapeFunction(oldShapeFunction);
    ((LightweightVertexSelectionRenderer) biModalRenderer.getVertexRenderer(LIGHTWEIGHT))
        .setVertexShapeFunction(oldLightweightShapeFunction);
  }

  protected void paintTransformed(V vertex) {
    biModalRenderer.renderVertex(
        visualizationServer.getRenderContext(),
        visualizationServer.getVisualizationModel().getLayoutModel(),
        vertex);
  }

  protected void paintSingleNormal(Graphics2D g2d, V vertex) {
    LayoutModel<V> layoutModel = visualizationServer.getVisualizationModel().getLayoutModel();
    MultiLayerTransformer multiLayerTransformer =
        visualizationServer.getRenderContext().getMultiLayerTransformer();

    Point location = layoutModel.apply(vertex);
    // translate to view coords
    Point2D viewLocation = multiLayerTransformer.transform(location.x, location.y);
    AffineTransform graphicsTransform = g2d.getTransform();
    log.trace("graphics transform is {}", graphicsTransform);
    // move the shape to the right place in the view
    Shape shape =
        AffineTransform.getTranslateInstance(viewLocation.getX(), viewLocation.getY())
            .createTransformedShape(selectionShape);
    g2d.draw(shape);
    g2d.fill(shape);
  }

  protected Shape prepareFinalVertexShape(
      RenderContext<V, ?> renderContext, VisualizationModel<V, ?> visualizationModel, V v) {

    // get the shape to be rendered
    Shape shape;
    if (visualizationServer.getRenderer() instanceof BiModalRenderer) {
      shape = visualizationServer.getRenderContext().getVertexShapeFunction().apply(v);
      if (useBounds || useOval) {
        shape = shape.getBounds();
        if (useOval) {
          Rectangle2D bounds = (Rectangle2D) shape;
          shape =
              new Ellipse2D.Double(
                  bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
        }
      }
    } else {
      shape = renderContext.getVertexShapeFunction().apply(v);
    }

    // increase is 10% larger

    double scalex = highlightScale;
    double scaley = highlightScale;
    Rectangle2D bounds = shape.getBounds2D();
    double width = bounds.getWidth();
    double height = bounds.getHeight();
    if (width > height) {
      // 10% of width
      double dwidth = width * (highlightScale - 1);
      scaley = (height + dwidth) / height;
    } else if (height > width) {
      double dheight = height * (highlightScale - 1);
      scalex = (width + dheight) / width;
    }
    // make the shape a little larger
    AffineTransform scaled = AffineTransform.getScaleInstance(scalex, scaley);
    shape = scaled.createTransformedShape(shape);
    Point p = visualizationModel.getLayoutModel().apply(v);
    // p is the vertex location in layout coordinates

    Point2D p2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, p.x, p.y);
    // now p is in view coordinates, ready to be further transformed by any transform in the
    // graphics context
    double x = p2d.getX();
    double y = p2d.getY();
    // create a transform that translates to the location of
    // the vertex to be rendered
    AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
    // transform the vertex shape with xtransform
    shape = xform.createTransformedShape(shape);
    return shape;
  }

  protected void paintShapeForVertex(RenderContext<V, ?> renderContext, V v, Shape shape) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Graphics2D g2d = g.getDelegate();
    AffineTransform savedTransform = g2d.getTransform();
    AffineTransform graphicsTransformCopy = new AffineTransform(g2d.getTransform());

    Paint oldPaint = g.getPaint();

    AffineTransform viewTransform =
        renderContext
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.VIEW)
            .getTransform();

    // don't mutate the viewTransform!
    graphicsTransformCopy.concatenate(viewTransform);
    g2d.setTransform(graphicsTransformCopy);

    //    g.setTransform(viewTransform);
    Paint drawPaint = selectionPaint;
    g.setPaint(drawPaint);
    // anti-alias here??
    Object hint = g.getRenderingHint(KEY_ANTIALIASING);
    g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
    if (fillHighlight) {
      g.fill(shape);
    } else {
      g.draw(shape);
    }
    g.setRenderingHint(KEY_ANTIALIASING, hint);
    g.setPaint(oldPaint);
    g2d.setTransform(savedTransform);
  }

  protected void paintIconForVertex(
      RenderContext<V, ?> renderContext, VisualizationModel<V, ?> visualizationModel, V v) {
    Shape shape = prepareFinalVertexShape(renderContext, visualizationModel, v);
    paintShapeForVertex(renderContext, v, shape);
  }

  @Override
  public boolean useTransform() {
    return false;
  }
}
