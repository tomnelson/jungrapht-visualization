package org.jungrapht.visualization.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Set;
import javax.swing.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.renderers.LightweightVertexRenderer;
import org.jungrapht.visualization.renderers.ModalRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.ArrowFactory;

/**
 * Paints a shape at the location of all selected vertices. The shape does not change size as the
 * view is scaled (zoomed in or out)
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 */
public class MultiSelectedVertexPaintable<V> implements VisualizationServer.Paintable {

  /**
   * builder for the {@code SelectedVertexPaintable}
   *
   * @param <V> the vertex type
   */
  public static class Builder<V> {

    private final VisualizationServer<V, ?> visualizationServer;
    private Shape selectionShape = ArrowFactory.getNotchedArrow(20, 24, 8);
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
    public MultiSelectedVertexPaintable<V> build() {
      return new MultiSelectedVertexPaintable<>(this);
    }

    /** @param visualizationServer the (required) {@code VisualizationServer} parameter */
    private Builder(VisualizationServer<V, ?> visualizationServer) {
      this.visualizationServer = visualizationServer;
    }
  }

  /**
   * @param visualizationServer the (required) {@code VisualizationServer} parameter
   * @param <V> the vertex type
   * @return the {@code Builder} used to create the instance of a {@code SelectedVertexPaintable}
   */
  public static <V> Builder<V> builder(VisualizationServer<V, ?> visualizationServer) {
    return new Builder<>(visualizationServer);
  }

  /** the (required) {@code VisualizationServer} */
  private final VisualizationServer<V, ?> visualizationServer;
  /** the {@code Shape} to paint to indicate selected vertices */
  private Shape selectionShape;
  /** the {@code Paint} to use to draw the selected vertex indicating {@code Shape} */
  private Paint selectionPaint;

  /**
   * Create an instance of a {@code SelectedVertexPaintable}
   *
   * @param builder the {@code Builder} to provide parameters to the {@code SelectedVertexPaintable}
   */
  private MultiSelectedVertexPaintable(Builder<V> builder) {
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
      VisualizationServer<V, ?> visualizationServer, Shape shape, Paint selectionPaint) {
    this.visualizationServer = visualizationServer;
    this.selectionShape = shape;
    this.selectionPaint = selectionPaint;
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
    LayoutModel<V> layoutModel = visualizationServer.getModel().getLayoutModel();
    MultiLayerTransformer multiLayerTransformer =
        visualizationServer.getRenderContext().getMultiLayerTransformer();
    // if there is only one selected vertex, make a big arrow pointing to it
    if (selectedVertices.size() == 1) {
      // set the transform to identity
      g2d.setTransform(new AffineTransform());
      V vertex = selectedVertices.stream().findFirst().get();
      // find the layout coords
      Point location = layoutModel.apply(vertex);
      // translate to view coords
      Point2D viewLocation = multiLayerTransformer.transform(location.x, location.y);
      // get a 45 degree rotation (later)
      AffineTransform rotationTransform = AffineTransform.getRotateInstance(3 * Math.PI / 4);
      // move the shape to the right place in the view
      AffineTransform translateTransform =
          AffineTransform.getTranslateInstance(viewLocation.getX(), viewLocation.getY());
      AffineTransform transform = new AffineTransform();
      transform.concatenate(translateTransform);
      transform.concatenate(rotationTransform);
      Shape shape = transform.createTransformedShape(selectionShape);
      g2d.draw(shape);
      g2d.fill(shape);
    } else {
      // if there are a bunch that are selected, highlight them with a drawn border
      ((JComponent) visualizationServer).revalidate();
      for (V vertex : selectedVertices) {
        paintIconForVertex(
            visualizationServer.getRenderContext(), visualizationServer.getModel(), vertex);
      }
    }
    // put back the old values
    g2d.setPaint(oldPaint);
    g2d.setTransform(oldTransform);
  }

  protected Shape prepareFinalVertexShape(
      RenderContext<V, ?> renderContext,
      VisualizationModel<V, ?> visualizationModel,
      V v,
      int[] coords) {

    // get the shape to be rendered
    Shape shape;
    Renderer<V, ?> renderer = visualizationServer.getRenderer();
    if (renderer instanceof ModalRenderer) {
      ModalRenderer modalRenderer = (ModalRenderer) renderer;
      Renderer.Vertex vertexRenderer = modalRenderer.getVertexRenderer();
      if (vertexRenderer instanceof LightweightVertexRenderer) {
        LightweightVertexRenderer<V, ?> lightweightVertexRenderer =
            (LightweightVertexRenderer) vertexRenderer;
        shape = lightweightVertexRenderer.getVertexShapeFunction().apply(v);
      } else {
        shape = renderContext.getVertexShapeFunction().apply(v);
      }

    } else {
      shape = renderContext.getVertexShapeFunction().apply(v);
    }
    Point p = visualizationModel.getLayoutModel().apply(v);
    // p is the vertex location in layout coordinates

    Point2D p2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(MultiLayerTransformer.Layer.LAYOUT, new Point2D.Double(p.x, p.y));
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
    Paint oldPaint = g.getPaint();

    AffineTransform viewTransform =
        renderContext
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.VIEW)
            .getTransform();
    g.setTransform(viewTransform);
    Paint drawPaint = Color.red;
    if (drawPaint != null) {
      g.setPaint(drawPaint);
      Stroke oldStroke = g.getStroke();
      Stroke stroke = new BasicStroke(4.f);
      if (stroke != null) {
        g.setStroke(stroke);
      }
      g.draw(shape);
      g.setPaint(oldPaint);
      g.setStroke(oldStroke);
    }
  }

  protected void paintIconForVertex(
      RenderContext<V, ?> renderContext, VisualizationModel<V, ?> visualizationModel, V v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    int[] coords = new int[2];
    Shape shape = prepareFinalVertexShape(renderContext, visualizationModel, v, coords);

    paintShapeForVertex(renderContext, v, shape);
  }

  @Override
  public boolean useTransform() {
    return false;
  }
}
