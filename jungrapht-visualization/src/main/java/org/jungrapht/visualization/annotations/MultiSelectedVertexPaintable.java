package org.jungrapht.visualization.annotations;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;
import static org.jungrapht.visualization.renderers.BiModalRenderer.LIGHTWEIGHT;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.renderers.*;
import org.jungrapht.visualization.renderers.Renderer;
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
    private Paint selectionPaint = Color.red;
    private float selectionStrokeMin =
        Float.parseFloat(System.getProperty(PREFIX + "selectionStrokeMin", "2.f"));
    private boolean useBounds = true;

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

    public B useBounds(boolean useBounds) {
      this.useBounds = useBounds;
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

  private float selectionStrokeMin;

  private BiModalSelectionRenderer<V, E> biModalRenderer;

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
        builder.selectionPaint,
        builder.selectionIcon,
        builder.selectionStrokeMin);
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
      Paint selectionPaint,
      Icon selectionIcon,
      float selectionStrokeMin) {
    this.visualizationServer = visualizationServer;
    this.selectionShape = shape;
    this.useBounds = useBounds;
    this.selectionPaint = selectionPaint;
    this.selectionIcon = selectionIcon;
    this.selectionStrokeMin = selectionStrokeMin;
    this.biModalRenderer =
        BiModalSelectionRenderer.<V, E>builder()
            .component(visualizationServer.getComponent())
            .lightweightRenderer(
                new SelectionRenderer<>(new LightweightVertexSelectionRenderer<>()))
            .heavyweightRenderer(
                (new SelectionRenderer<>(
                    new HeavyweightVertexSelectionRenderer<>(visualizationServer))))
            .modeSourceRenderer((BiModalRenderer<V, E>) visualizationServer.getRenderer())
            .build();
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

    Collection<V> selectedVertices =
        visualizationServer
            .getSelectedVertexState()
            .getSelected()
            .stream()
            .filter(visualizationServer.getVisualizationModel().getGraph().vertexSet()::contains)
            .collect(Collectors.toList());

    GraphicsDecorator graphicsDecorator =
        visualizationServer.getRenderContext().getGraphicsContext();

    if (graphicsDecorator instanceof TransformingGraphics) {
      // get a copy of the current transform used by g2d
      AffineTransform savedTransform = g2d.getTransform();
      AffineTransform graphicsTransformCopy = new AffineTransform(g2d.getTransform());

      AffineTransform viewTransform =
          visualizationServer
              .getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.VIEW)
              .getTransform();

      // don't mutate the viewTransform!
      graphicsTransformCopy.concatenate(viewTransform);
      g2d.setTransform(graphicsTransformCopy);
      Stroke savedStroke = g2d.getStroke();
      float strokeWidth =
          Math.max(selectionStrokeMin, (int) (selectionStrokeMin / g2d.getTransform().getScaleX()));
      g2d.setStroke(new BasicStroke(strokeWidth));
      for (V vertex : selectedVertices) {
        paintTransformed(vertex);
      }
      g2d.setStroke(savedStroke);

    } else {
      for (V vertex : selectedVertices) {
        paintIconForVertex(
            visualizationServer.getRenderContext(),
            visualizationServer.getVisualizationModel(),
            vertex);
      }
    }
    // put back the old values
    g2d.setPaint(oldPaint);
    g2d.setTransform(oldTransform);
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
        visualizationServer.getVisualizationModel(),
        vertex);

    visualizationServer.getRenderContext().setVertexShapeFunction(oldShapeFunction);
    ((LightweightVertexSelectionRenderer) biModalRenderer.getVertexRenderer(LIGHTWEIGHT))
        .setVertexShapeFunction(oldLightweightShapeFunction);
  }

  protected void paintTransformed(V vertex) {
    biModalRenderer.renderVertex(
        visualizationServer.getRenderContext(),
        visualizationServer.getVisualizationModel(),
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
    log.info("graphics transform is {}", graphicsTransform);
    // move the shape to the right place in the view
    Shape shape =
        AffineTransform.getTranslateInstance(viewLocation.getX(), viewLocation.getY())
            .createTransformedShape(selectionShape);
    g2d.draw(shape);
    g2d.fill(shape);
  }

  protected Shape prepareFinalVertexShape(
      RenderContext<V, ?> renderContext,
      VisualizationModel<V, ?> visualizationModel,
      V v,
      int[] coords) {

    // get the shape to be rendered
    Shape shape;
    if (visualizationServer.getRenderer() instanceof BiModalRenderer) {
      BiModalRenderer<V, ?> biModalRenderer = (BiModalRenderer) visualizationServer.getRenderer();
      Renderer.Vertex<V, ?> vertexRenderer = biModalRenderer.getVertexRenderer();
      if (vertexRenderer instanceof LightweightVertexRenderer) {
        LightweightVertexRenderer<V, ?> lightweightVertexRenderer =
            (LightweightVertexRenderer) vertexRenderer;
        shape = lightweightVertexRenderer.getVertexShapeFunction().apply(v);
      } else {
        // heavyweight
        shape = visualizationServer.getRenderContext().getVertexShapeFunction().apply(v);
        if (useBounds) {
          shape = shape.getBounds();
        }
      }
    } else {
      shape = renderContext.getVertexShapeFunction().apply(v);
    }
    Point p = visualizationModel.getLayoutModel().apply(v);
    // p is the vertex location in layout coordinates

    Point2D p2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, p.x, p.y);
    // now p is in view coordinates, ready to be further transformed by any transform in the
    // graphics context
    float x = (float) p2d.getX();
    float y = (float) p2d.getY();
    coords[0] = (int) x;
    coords[1] = (int) y;
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
    Stroke oldStroke = g.getStroke();
    float strokeWidth =
        Math.max(selectionStrokeMin, (int) (selectionStrokeMin / g2d.getTransform().getScaleX()));
    Stroke stroke = new BasicStroke(strokeWidth);
    g.setStroke(stroke);
    g.draw(shape);
    g.setPaint(oldPaint);
    g.setStroke(oldStroke);
    g2d.setTransform(savedTransform);
  }

  protected void paintIconForVertex(
      RenderContext<V, ?> renderContext, VisualizationModel<V, ?> visualizationModel, V v) {
    int[] coords = new int[2];
    Shape shape = prepareFinalVertexShape(renderContext, visualizationModel, v, coords);
    paintShapeForVertex(renderContext, v, shape);
  }

  @Override
  public boolean useTransform() {
    return false;
  }
}
