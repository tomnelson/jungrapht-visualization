/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.renderers;

import java.util.ConcurrentModificationException;
import org.jgrapht.Graph;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
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
 */
class DefaultRenderer<V, E> implements Renderer<V, E> {

  private static final Logger log = LoggerFactory.getLogger(DefaultRenderer.class);
  protected Vertex<V, E> vertexRenderer = new DefaultVertexRenderer<>();
  protected VertexLabel<V, E> vertexLabelRenderer = new DefaultVertexLabelRenderer<>();
  protected Renderer.Edge<V, E> edgeRenderer = new DefaultEdgeRenderer<>();
  protected Renderer.EdgeLabel<V, E> edgeLabelRenderer = new DefaultEdgeLabelRenderer<>();

  public void render(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      Spatial<V> vertexSpatial,
      Spatial<E> edgeSpatial) {
    if (vertexSpatial == null) {
      render(renderContext, visualizationModel);
      return;
    }
    Iterable<V> visibleVertices = null;
    Iterable<E> visibleEdges = null;

    try {
      visibleVertices =
          vertexSpatial.getVisibleElements(
              ((VisualizationServer) renderContext.getScreenDevice()).viewOnLayout());

      if (edgeSpatial != null) {
        visibleEdges =
            edgeSpatial.getVisibleElements(
                ((VisualizationServer) renderContext.getScreenDevice()).viewOnLayout());
      } else {
        visibleEdges = visualizationModel.getGraph().edgeSet();
      }
    } catch (ConcurrentModificationException ex) {
      // skip rendering until graph vertex index is stable,
      // this can happen if the layout relax thread is changing locations while the
      // visualization is rendering
      log.info("got {} so returning", ex.toString());
      log.info(
          "layoutMode active: {}, edgeSpatial active {}, vertexSpatial active: {}",
          visualizationModel.getLayoutModel().isRelaxing(),
          edgeSpatial != null && edgeSpatial.isActive(),
          vertexSpatial != null && vertexSpatial.isActive());
      return;
    }

    try {
      Graph<V, E> graph = visualizationModel.getGraph();
      // paint all the edges
      log.trace("the visibleEdges are {}", visibleEdges);
      for (E e : visibleEdges) {
        if (graph.edgeSet().contains(e)) {
          renderEdge(renderContext, visualizationModel, e);
          renderEdgeLabel(renderContext, visualizationModel, e);
        }
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      log.trace("the visibleVertices are {}", visibleVertices);

      for (V v : visibleVertices) {
        renderVertex(renderContext, visualizationModel, v);
        renderVertexLabel(renderContext, visualizationModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  @Override
  public void render(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel) {
    Graph<V, E> graph = visualizationModel.getGraph();
    // paint all the edges
    try {
      for (E e : graph.edgeSet()) {
        renderEdge(renderContext, visualizationModel, e);
        renderEdgeLabel(renderContext, visualizationModel, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (V v : graph.vertexSet()) {
        renderVertex(renderContext, visualizationModel, v);
        renderVertexLabel(renderContext, visualizationModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  public void renderVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    vertexRenderer.paintVertex(renderContext, visualizationModel, v);
  }

  public void renderVertexLabel(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    vertexLabelRenderer.labelVertex(
        renderContext, visualizationModel, v, renderContext.getVertexLabelFunction().apply(v));
  }

  public void renderEdge(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {
    edgeRenderer.paintEdge(renderContext, visualizationModel, e);
  }

  public void renderEdgeLabel(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {
    edgeLabelRenderer.labelEdge(
        renderContext, visualizationModel, e, renderContext.getEdgeLabelFunction().apply(e));
  }

  public void setVertexRenderer(Vertex<V, E> r) {
    this.vertexRenderer = r;
  }

  public void setEdgeRenderer(Renderer.Edge<V, E> r) {
    this.edgeRenderer = r;
  }

  /** @return the edgeLabelRenderer */
  public Renderer.EdgeLabel<V, E> getEdgeLabelRenderer() {
    return edgeLabelRenderer;
  }

  /** @param edgeLabelRenderer the edgeLabelRenderer to set */
  public void setEdgeLabelRenderer(Renderer.EdgeLabel<V, E> edgeLabelRenderer) {
    this.edgeLabelRenderer = edgeLabelRenderer;
  }

  /** @return the vertexLabelRenderer */
  public VertexLabel<V, E> getVertexLabelRenderer() {
    return vertexLabelRenderer;
  }

  /** @param vertexLabelRenderer the vertexLabelRenderer to set */
  public void setVertexLabelRenderer(VertexLabel<V, E> vertexLabelRenderer) {
    this.vertexLabelRenderer = vertexLabelRenderer;
  }

  /** @return the edgeRenderer */
  public Renderer.Edge<V, E> getEdgeRenderer() {
    return edgeRenderer;
  }

  /** @return the vertexRenderer */
  public Vertex<V, E> getVertexRenderer() {
    return vertexRenderer;
  }
}
