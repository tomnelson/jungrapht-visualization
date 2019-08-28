package org.jungrapht.visualization.annotations;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Set;
import java.util.function.Function;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.renderers.BiModalRenderer;
import org.jungrapht.visualization.renderers.BiModalSelectionRenderer;
import org.jungrapht.visualization.renderers.HeavyweightVertexSelectionRenderer;
import org.jungrapht.visualization.renderers.LightweightVertexSelectionRenderer;
import org.jungrapht.visualization.renderers.SelectionRenderer;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.transform.shape.MagnifyIconGraphics;
import org.jungrapht.visualization.util.ArrowFactory;

/**
 * Paints a shape at the location of all selected vertices. The shape does not change size as the
 * view is scaled (zoomed in or out)
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 */
public class MultiSelectedVertexPaintable<V, E> implements VisualizationServer.Paintable {

  /**
   * builder for the {@code SelectedVertexPaintable}
   *
   * @param <V> the vertex type
   */
  public static class Builder<V, E> {

    private final VisualizationServer<V, E> visualizationServer;
    private Shape selectionShape =
        AffineTransform.getRotateInstance(3 * Math.PI / 4)
            .createTransformedShape(ArrowFactory.getNotchedArrow(20, 24, 8));
    private Paint selectionPaint = Color.red;

    /**
     * @param selectionShape the shape to draw as an indicator for selected vertices
     * @return this builder
     */
    public Builder selectionShape(Shape selectionShape) {
      this.selectionShape = selectionShape;
      return this;
    }

    /**
     * @param selectionPaint the color to draw the selected vertex indicator
     * @return this builder
     */
    public Builder selectionPaint(Paint selectionPaint) {
      this.selectionPaint = selectionPaint;
      return this;
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
  public static <V, E> Builder<V, E> builder(VisualizationServer<V, E> visualizationServer) {
    return new Builder<>(visualizationServer);
  }

  /** the (required) {@code VisualizationServer} */
  private final VisualizationServer<V, E> visualizationServer;
  /** the {@code Shape} to paint to indicate selected vertices */
  private Shape selectionShape;
  /** the {@code Paint} to use to draw the selected vertex indicating {@code Shape} */
  private Paint selectionPaint;

  private BiModalSelectionRenderer<V, E> biModalRenderer;

  /**
   * Create an instance of a {@code SelectedVertexPaintable}
   *
   * @param builder the {@code Builder} to provide parameters to the {@code SelectedVertexPaintable}
   */
  private MultiSelectedVertexPaintable(Builder<V, E> builder) {
    this(builder.visualizationServer, builder.selectionShape, builder.selectionPaint);
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
      VisualizationServer<V, E> visualizationServer, Shape shape, Paint selectionPaint) {
    this.visualizationServer = visualizationServer;
    this.selectionShape = shape;
    this.selectionPaint = selectionPaint;
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
    // get the currently currently selected vertices
    Set<V> selectedVertices = visualizationServer.getSelectedVertexState().getSelected();
    LayoutModel<V> layoutModel = visualizationServer.getVisualizationModel().getLayoutModel();
    MultiLayerTransformer multiLayerTransformer =
        visualizationServer.getRenderContext().getMultiLayerTransformer();
    // if there is only one selected vertex, make a big arrow pointing to it
    //    if (selectedVertices.size() == 1) {
    for (V vertex : selectedVertices) {
      //      V vertex = selectedVertices.stream().findFirst().get();
      // find the layout coords
      Point location = layoutModel.apply(vertex);
      // translate to view coords
      Point2D viewLocation = multiLayerTransformer.transform(location.x, location.y);

      GraphicsDecorator graphicsDecorator =
          visualizationServer.getRenderContext().getGraphicsContext();
      if (graphicsDecorator instanceof MagnifyIconGraphics) {

        // get a copy of the current transform used by g2d
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

        Function<V, Shape> oldShapeFunction =
            visualizationServer.getRenderContext().getVertexShapeFunction();
        visualizationServer.getRenderContext().setVertexShapeFunction(v -> selectionShape);
        Function<V, Shape> oldLightweightShapeFunction =
            ((LightweightVertexSelectionRenderer)
                    biModalRenderer.getVertexRenderer(BiModalRenderer.Mode.LIGHTWEIGHT))
                .getVertexShapeFunction();
        ((LightweightVertexSelectionRenderer)
                biModalRenderer.getVertexRenderer(BiModalRenderer.Mode.LIGHTWEIGHT))
            .setVertexShapeFunction(v -> selectionShape);

        biModalRenderer.renderVertex(
            visualizationServer.getRenderContext(),
            visualizationServer.getVisualizationModel(),
            vertex);
        visualizationServer.getRenderContext().setVertexShapeFunction(oldShapeFunction);
        ((LightweightVertexSelectionRenderer)
                biModalRenderer.getVertexRenderer(BiModalRenderer.Mode.LIGHTWEIGHT))
            .setVertexShapeFunction(oldLightweightShapeFunction);
      } else {
        // move the shape to the right place in the view
        Shape shape =
            AffineTransform.getTranslateInstance(viewLocation.getX(), viewLocation.getY())
                .createTransformedShape(selectionShape);
        g2d.draw(shape);
        g2d.fill(shape);
      }
      g2d.setTransform(oldTransform);
    }

    // get a copy of the current transform used by g2d
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

    // highlight all selected vertices with a drawn border
    for (V vertex : selectedVertices) {
      biModalRenderer.renderVertex(
          visualizationServer.getRenderContext(),
          visualizationServer.getVisualizationModel(),
          vertex);
    }
    // put back the old values
    g2d.setPaint(oldPaint);
    g2d.setTransform(oldTransform);
  }

  @Override
  public boolean useTransform() {
    return false;
  }
}
