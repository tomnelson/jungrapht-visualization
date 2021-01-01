/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.renderers;

import java.awt.*;
import java.util.ConcurrentModificationException;
import org.jgrapht.Graph;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationServer;
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
class DefaultRenderer<V, E> implements Renderer<V, E> {

  private static final Logger log = LoggerFactory.getLogger(DefaultRenderer.class);
  protected Vertex<V, E> vertexRenderer = new HeavyweightVertexRenderer<>();
  protected VertexLabel<V, E> vertexLabelRenderer = new HeavyweightVertexLabelRenderer<>();
  protected Edge<V, E> edgeRenderer = new HeavyweightEdgeRenderer<>();
  protected EdgeLabel<V, E> edgeLabelRenderer = new HeayweightEdgeLabelRenderer<>();

  public void render(
      RenderContext<V, E> renderContext,
      LayoutModel<V> layoutModel,
      Spatial<V> vertexSpatial,
      Spatial<E> edgeSpatial) {
    if (vertexSpatial == null) {
      render(renderContext, layoutModel);
      return;
    }
    renderContext
        .getGraphicsContext()
        .getRenderingHints()
        .put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        Graph<V, E> graph = layoutModel.getGraph();
        visibleEdges = graph.edgeSet();
      }
    } catch (ConcurrentModificationException ex) {
      // skip rendering until graph vertex index is stable,
      // this can happen if the layout relax thread is changing locations while the
      // visualization is rendering
      log.debug("got {} so returning", ex.toString());
      log.debug(
          "layoutMode active: {}, edgeSpatial active {}, vertexSpatial active: {}",
          layoutModel.isRelaxing(),
          edgeSpatial != null && edgeSpatial.isActive(),
          vertexSpatial.isActive());
      return;
    }

    try {
      Graph<V, E> graph = layoutModel.getGraph();
      // paint all the edges
      log.trace("the visibleEdges are {}", visibleEdges);
      for (E e : visibleEdges) {
        if (graph.containsEdge(e)) {
          renderEdge(renderContext, layoutModel, e);
          renderEdgeLabel(renderContext, layoutModel, e);
        }
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      log.trace("the visibleVertices are {}", visibleVertices);

      for (V v : visibleVertices) {
        renderVertex(renderContext, layoutModel, v);
        renderVertexLabel(renderContext, layoutModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  @Override
  public void render(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel) {
    renderContext
        .getGraphicsContext()
        .getRenderingHints()
        .put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Graph<V, E> graph = layoutModel.getGraph();
    // paint all the edges
    try {
      for (E e : graph.edgeSet()) {
        renderEdge(renderContext, layoutModel, e);
        renderEdgeLabel(renderContext, layoutModel, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (V v : graph.vertexSet()) {
        renderVertex(renderContext, layoutModel, v);
        renderVertexLabel(renderContext, layoutModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  public void renderVertex(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v) {
    vertexRenderer.paintVertex(renderContext, layoutModel, v);
  }

  public void renderVertexLabel(
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v) {
    vertexLabelRenderer.labelVertex(
        renderContext, layoutModel, v, renderContext.getVertexLabelFunction().apply(v));
  }

  public void renderEdge(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e) {
    edgeRenderer.paintEdge(renderContext, layoutModel, e);
  }

  public void renderEdgeLabel(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e) {
    edgeLabelRenderer.labelEdge(
        renderContext, layoutModel, e, renderContext.getEdgeLabelFunction().apply(e));
  }

  public void setVertexRenderer(Vertex<V, E> r) {
    this.vertexRenderer = r;
  }

  public void setEdgeRenderer(Edge<V, E> r) {
    this.edgeRenderer = r;
  }

  /** @return the edgeLabelRenderer */
  public EdgeLabel<V, E> getEdgeLabelRenderer() {
    return edgeLabelRenderer;
  }

  /** @param edgeLabelRenderer the edgeLabelRenderer to set */
  public void setEdgeLabelRenderer(EdgeLabel<V, E> edgeLabelRenderer) {
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
  public Edge<V, E> getEdgeRenderer() {
    return edgeRenderer;
  }

  /** @return the vertexRenderer */
  public Vertex<V, E> getVertexRenderer() {
    return vertexRenderer;
  }
}
