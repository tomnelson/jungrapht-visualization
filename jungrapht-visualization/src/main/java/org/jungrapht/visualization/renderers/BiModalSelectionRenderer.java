package org.jungrapht.visualization.renderers;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;
import javax.swing.*;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.BidirectionalTransformer;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensTransformer;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.transform.shape.TransformingGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Renderer will draw the selection highlights for a Paintable. In order to discern what the
 * current rendering mode is, it has a reference to the main BiModalRenderer
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class BiModalSelectionRenderer<V, E> extends BiModalRenderer<V, E> {

  private static final Logger log = LoggerFactory.getLogger(BiModalSelectionRenderer.class);

  public static class Builder<
          V, E, T extends BiModalSelectionRenderer<V, E>, B extends Builder<V, E, T, B>>
      extends BiModalRenderer.Builder<V, E, T, B> {
    protected BiModalRenderer<V, E> modeSourceRenderer;

    public B modeSourceRenderer(BiModalRenderer<V, E> modeSourceRenderer) {
      this.modeSourceRenderer = modeSourceRenderer;
      return self();
    }

    public T build() {
      return (T) new BiModalSelectionRenderer<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new BiModalSelectionRenderer.Builder();
  }

  /** another BoModalRenderer to get the current visualization Mode from */
  BiModalRenderer<V, E> other;

  protected BiModalSelectionRenderer(Builder<V, E, ?, ?> builder) {
    this(builder.component, builder.rendererMap, builder.modeSourceRenderer);
  }

  protected BiModalSelectionRenderer(
      JComponent component,
      Map<Mode, Renderer<V, E>> rendererMap,
      BiModalRenderer<V, E> modeSourceRenderer) {
    super(component, rendererMap);
    this.other = modeSourceRenderer;
  }

  /**
   * Render the highlights for the supplied vertex
   *
   * @param renderContext to supply properties
   * @param visualizationModel to supply the LayoutModel
   * @param v the vertex to render highlights for
   */
  @Override
  public void renderVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {

    GraphicsDecorator graphicsDecorator = renderContext.getGraphicsContext();
    /*
     * For the special case where there is a Magnifying lens active, this renderer will set the mode per vertex
     * depending on whether the vertex is in the lens, and according to what the magnification is for the lens at
     * that location (typically uniform accross the entire lens)
     */
    if (graphicsDecorator instanceof TransformingGraphics) {
      TransformingGraphics transformingGraphics = (TransformingGraphics) graphicsDecorator;
      BidirectionalTransformer bidirectionalTransformer = transformingGraphics.getTransformer();
      if (bidirectionalTransformer instanceof LensTransformer) {
        LensTransformer magnifyTransformer = (LensTransformer) bidirectionalTransformer;
        Lens lens = magnifyTransformer.getLens();
        // layoutLocation
        Point p = visualizationModel.getLayoutModel().apply(v);
        Point2D layoutPoint = new Point2D.Double(p.x, p.y);
        // transform to view
        Point2D viewPoint =
            renderContext
                .getMultiLayerTransformer()
                .transform(MultiLayerTransformer.Layer.LAYOUT, layoutPoint);
        Shape lensShape = lens.getLensShape();
        if (lensShape.contains(viewPoint)) {
          double magnification = magnifyTransformer.getLens().getMagnification();
          double product = magnification * magnifyTransformer.getScale();
          // override for the magnifier scale. This may set the mode to Heavyweight inside the lens
          Mode mode = other.getModeFor(() -> product);
          rendererMap.get(mode).renderVertex(renderContext, visualizationModel, v);
        } else {
          rendererMap.get(other.getMode()).renderVertex(renderContext, visualizationModel, v);
        }
      }
    } else {
      rendererMap.get(other.getMode()).renderVertex(renderContext, visualizationModel, v);
    }
  }
}
