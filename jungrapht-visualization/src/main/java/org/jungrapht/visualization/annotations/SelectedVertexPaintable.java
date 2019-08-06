package org.jungrapht.visualization.annotations;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Set;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.util.ArrowFactory;

/**
 * Paints a shape at the location of all selected vertices. The shape does not change size as the
 * view is scaled (zoomed in or out)
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 */
public class SelectedVertexPaintable<V> implements VisualizationServer.Paintable {

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
    public SelectedVertexPaintable<V> build() {
      return new SelectedVertexPaintable<>(this);
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
  private SelectedVertexPaintable(Builder<V> builder) {
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
  private SelectedVertexPaintable(
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
    // set the transform to identity
    g2d.setTransform(new AffineTransform());
    // get the currently currently selected vertices
    Set<V> selectedVertices = visualizationServer.getSelectedVertexState().getSelected();
    LayoutModel<V> layoutModel = visualizationServer.getVisualizationModel().getLayoutModel();
    MultiLayerTransformer multiLayerTransformer =
        visualizationServer.getRenderContext().getMultiLayerTransformer();
    for (V vertex : selectedVertices) {
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
