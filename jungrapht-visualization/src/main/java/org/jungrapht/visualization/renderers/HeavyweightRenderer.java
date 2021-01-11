/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.renderers;

import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.spatial.Spatial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the Renderer used by the VisualizationViewer. Default Vertex and
 * Edge Renderers are supplied, or the user may set custom values. The Vertex and Edge renderers are
 * used in the renderVertex and renderEdge methods, which are called in the render loop of the
 * VisualizationViewer.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
class HeavyweightRenderer<V, E> implements Renderer<V, E> {

  private static final Logger log = LoggerFactory.getLogger(HeavyweightRenderer.class);
  protected Vertex<V, E> vertexRenderer = new HeavyweightVertexRenderer<>();
  protected VertexLabel<V, E> vertexLabelRenderer = new HeavyweightVertexLabelRenderer<>();
  protected Renderer.Edge<V, E> edgeRenderer = new HeavyweightEdgeRenderer<>();
  protected Renderer.EdgeLabel<V, E> edgeLabelRenderer = new HeayweightEdgeLabelRenderer<>();

  @Override
  public void render(
      RenderContext<V, E> renderContext,
      LayoutModel<V> visualizationModel,
      Spatial<V, V> vertexSpatial,
      Spatial<E, V> edgeSpatial) {
    // BiModalRenderer handles this
  }

  @Override
  public void render(RenderContext<V, E> renderContext, LayoutModel<V> visualizationModel) {
    // BiModalRenderer handles this
  }

  @Override
  public void renderVertex(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v) {
    vertexRenderer.paintVertex(renderContext, layoutModel, v);
  }

  @Override
  public void renderVertexLabel(
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v) {
    vertexLabelRenderer.labelVertex(
        renderContext, layoutModel, v, renderContext.getVertexLabelFunction().apply(v));
  }

  @Override
  public void renderEdge(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e) {
    edgeRenderer.paintEdge(renderContext, layoutModel, e);
  }

  @Override
  public void renderEdgeLabel(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e) {
    edgeLabelRenderer.labelEdge(
        renderContext, layoutModel, e, renderContext.getEdgeLabelFunction().apply(e));
  }

  @Override
  public void setVertexRenderer(Vertex<V, E> r) {
    this.vertexRenderer = r;
  }

  @Override
  public void setEdgeRenderer(Renderer.Edge<V, E> r) {
    this.edgeRenderer = r;
  }

  /** @return the edgeLabelRenderer */
  @Override
  public Renderer.EdgeLabel<V, E> getEdgeLabelRenderer() {
    return edgeLabelRenderer;
  }

  /** @param edgeLabelRenderer the edgeLabelRenderer to set */
  @Override
  public void setEdgeLabelRenderer(Renderer.EdgeLabel<V, E> edgeLabelRenderer) {
    this.edgeLabelRenderer = edgeLabelRenderer;
  }

  /** @return the vertexLabelRenderer */
  @Override
  public VertexLabel<V, E> getVertexLabelRenderer() {
    return vertexLabelRenderer;
  }

  /** @param vertexLabelRenderer the vertexLabelRenderer to set */
  @Override
  public void setVertexLabelRenderer(VertexLabel<V, E> vertexLabelRenderer) {
    this.vertexLabelRenderer = vertexLabelRenderer;
  }

  /** @return the edgeRenderer */
  @Override
  public Renderer.Edge<V, E> getEdgeRenderer() {
    return edgeRenderer;
  }

  /** @return the vertexRenderer */
  @Override
  public Vertex<V, E> getVertexRenderer() {
    return vertexRenderer;
  }
}
