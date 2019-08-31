package org.jungrapht.visualization.renderers;

import static org.jungrapht.visualization.renderers.BiModalRenderer.HEAVYWEIGHT;
import static org.jungrapht.visualization.renderers.BiModalRenderer.LIGHTWEIGHT;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.event.ChangeEvent;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.transform.BidirectionalTransformer;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.transform.shape.MagnifyIconGraphics;

// unused
public class BiModalRendererDelegate<V, E> implements Renderer<V, E> {

  private BiModalRenderer<V, E> delegate;
  protected Map<BiModalRenderer.Mode, Renderer<V, E>> rendererMap;

  public BiModalRendererDelegate(
      BiModalRenderer<V, E> delegate,
      Renderer<V, E> heavyweightRenderer,
      Renderer<V, E> lightweightRenderer) {
    this.delegate = delegate;
    this.rendererMap = new HashMap<>();
    rendererMap.put(HEAVYWEIGHT, heavyweightRenderer);
    rendererMap.put(LIGHTWEIGHT, lightweightRenderer);
  }

  public Supplier<Double> getScaleSupplier() {
    return delegate.getScaleSupplier();
  }

  protected BiModalRenderer.Mode getMode() {
    return delegate.getMode();
  }

  public void setScaleSupplier(Supplier<Double> scaleSupplier) {
    delegate.setScaleSupplier(scaleSupplier);
  }

  public Supplier<Integer> getCountSupplier() {
    return delegate.getCountSupplier();
  }

  public void setCountSupplier(Supplier<Integer> countSupplier) {
    delegate.setCountSupplier(countSupplier);
  }

  public void setMode(BiModalRenderer.Mode mode) {
    delegate.setMode(mode);
  }

  public void setVertexRenderer(BiModalRenderer.Mode mode, Vertex<V, E> r) {
    delegate.setVertexRenderer(mode, r);
  }

  public void setEdgeRenderer(BiModalRenderer.Mode mode, Edge<V, E> r) {
    delegate.setEdgeRenderer(mode, r);
  }

  public void setVertexLabelRenderer(BiModalRenderer.Mode mode, VertexLabel<V, E> r) {
    delegate.setVertexLabelRenderer(mode, r);
  }

  public void setEdgeLabelRenderer(BiModalRenderer.Mode mode, EdgeLabel<V, E> r) {
    delegate.setEdgeLabelRenderer(mode, r);
  }

  public VertexLabel<V, E> getVertexLabelRenderer(BiModalRenderer.Mode mode) {
    return delegate.getVertexLabelRenderer(mode);
  }

  public Vertex<V, E> getVertexRenderer(BiModalRenderer.Mode mode) {
    return delegate.getVertexRenderer(mode);
  }

  public Edge<V, E> getEdgeRenderer(BiModalRenderer.Mode mode) {
    return delegate.getEdgeRenderer(mode);
  }

  public EdgeLabel<V, E> getEdgeLabelRenderer(BiModalRenderer.Mode mode) {
    return delegate.getEdgeLabelRenderer(mode);
  }

  public BiModalRenderer.Mode getModeFor(Supplier<Double> scaleSupplier) {
    return delegate.getModeFor(scaleSupplier);
  }

  public void manageMode() {
    delegate.manageMode();
  }

  public void stateChanged(ChangeEvent e) {
    delegate.stateChanged(e);
  }

  @Override
  public void render(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      Spatial<V> vertexSpatial,
      Spatial<E> edgeSpatial) {}

  @Override
  public void render(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel) {}

  @Override
  public void renderVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {

    GraphicsDecorator graphicsDecorator = renderContext.getGraphicsContext();
    if (graphicsDecorator instanceof MagnifyIconGraphics) {
      MagnifyIconGraphics magnifyIconGraphics = (MagnifyIconGraphics) graphicsDecorator;
      BidirectionalTransformer bidirectionalTransformer = magnifyIconGraphics.getTransformer();
      if (bidirectionalTransformer instanceof MagnifyTransformer) {
        MagnifyTransformer magnifyTransformer = (MagnifyTransformer) bidirectionalTransformer;
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
          // override for the magnifier scale
          BiModalRenderer.Mode mode = getModeFor(() -> product);
          rendererMap.get(mode).renderVertex(renderContext, visualizationModel, v);
        } else {
          rendererMap.get(delegate.getMode()).renderVertex(renderContext, visualizationModel, v);
        }
      }
    } else {
      rendererMap.get(delegate.getMode()).renderVertex(renderContext, visualizationModel, v);
    }
  }

  @Override
  public void renderVertexLabel(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {}

  @Override
  public void renderEdge(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {}

  @Override
  public void renderEdgeLabel(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {}

  @Override
  public void setVertexRenderer(Vertex<V, E> r) {}

  @Override
  public void setEdgeRenderer(Edge<V, E> r) {}

  @Override
  public void setVertexLabelRenderer(VertexLabel<V, E> r) {}

  @Override
  public void setEdgeLabelRenderer(EdgeLabel<V, E> r) {}

  @Override
  public VertexLabel<V, E> getVertexLabelRenderer() {
    return null;
  }

  @Override
  public Vertex<V, E> getVertexRenderer() {
    return null;
  }

  @Override
  public Edge<V, E> getEdgeRenderer() {
    return null;
  }

  @Override
  public EdgeLabel<V, E> getEdgeLabelRenderer() {
    return null;
  }
}
