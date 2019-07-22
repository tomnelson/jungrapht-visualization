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
 * The default implementation of the Renderer used by the VisualizationViewer. Default Node and Edge
 * Renderers are supplied, or the user may set custom values. The Node and Edge renderers are used
 * in the renderNode and renderEdge methods, which are called in the render loop of the
 * VisualizationViewer.
 *
 * @author Tom Nelson
 */
public class BasicRenderer<N, E> implements Renderer<N, E> {

  private static final Logger log = LoggerFactory.getLogger(BasicRenderer.class);
  protected Node<N, E> nodeRenderer = new BasicNodeRenderer<>();
  protected NodeLabel<N, E> nodeLabelRenderer = new BasicNodeLabelRenderer<>();
  protected Renderer.Edge<N, E> edgeRenderer = new BasicEdgeRenderer<>();
  protected Renderer.EdgeLabel<N, E> edgeLabelRenderer = new BasicEdgeLabelRenderer<>();

  public void render(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E> visualizationModel,
      Spatial<N> nodeSpatial,
      Spatial<E> edgeSpatial) {
    if (nodeSpatial == null) {
      render(renderContext, visualizationModel);
      return;
    }
    Iterable<N> visibleNodes = null;
    Iterable<E> visibleEdges = null;

    try {
      visibleNodes =
          nodeSpatial.getVisibleElements(
              ((VisualizationServer) renderContext.getScreenDevice()).viewOnLayout());

      if (edgeSpatial != null) {
        visibleEdges =
            edgeSpatial.getVisibleElements(
                ((VisualizationServer) renderContext.getScreenDevice()).viewOnLayout());
      } else {
        visibleEdges = visualizationModel.getNetwork().edgeSet();
      }
    } catch (ConcurrentModificationException ex) {
      // skip rendering until graph node index is stable,
      // this can happen if the layout relax thread is changing locations while the
      // visualization is rendering
      log.info("got {} so returning", ex.toString());
      log.info(
          "layoutMode active: {}, edgeSpatial active {}, nodeSpatial active: {}",
          visualizationModel.getLayoutModel().isRelaxing(),
          edgeSpatial != null && edgeSpatial.isActive(),
          nodeSpatial != null && nodeSpatial.isActive());
      return;
    }

    try {
      Graph<N, E> network = visualizationModel.getNetwork();
      // paint all the edges
      log.trace("the visibleEdges are {}", visibleEdges);
      for (E e : visibleEdges) {
        if (network.edgeSet().contains(e)) {
          renderEdge(renderContext, visualizationModel, e);
          renderEdgeLabel(renderContext, visualizationModel, e);
        }
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the nodes
    try {
      log.trace("the visibleNodes are {}", visibleNodes);

      for (N v : visibleNodes) {
        renderNode(renderContext, visualizationModel, v);
        renderNodeLabel(renderContext, visualizationModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  @Override
  public void render(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel) {
    Graph<N, E> network = visualizationModel.getNetwork();
    // paint all the edges
    try {
      for (E e : network.edgeSet()) {
        renderEdge(renderContext, visualizationModel, e);
        renderEdgeLabel(renderContext, visualizationModel, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the nodes
    try {
      for (N v : network.vertexSet()) {
        renderNode(renderContext, visualizationModel, v);
        renderNodeLabel(renderContext, visualizationModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  public void renderNode(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v) {
    nodeRenderer.paintNode(renderContext, visualizationModel, v);
  }

  public void renderNodeLabel(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v) {
    nodeLabelRenderer.labelNode(
        renderContext, visualizationModel, v, renderContext.getNodeLabelFunction().apply(v));
  }

  public void renderEdge(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, E e) {
    edgeRenderer.paintEdge(renderContext, visualizationModel, e);
  }

  public void renderEdgeLabel(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, E e) {
    edgeLabelRenderer.labelEdge(
        renderContext, visualizationModel, e, renderContext.getEdgeLabelFunction().apply(e));
  }

  public void setNodeRenderer(Node<N, E> r) {
    this.nodeRenderer = r;
  }

  public void setEdgeRenderer(Renderer.Edge<N, E> r) {
    this.edgeRenderer = r;
  }

  /** @return the edgeLabelRenderer */
  public Renderer.EdgeLabel<N, E> getEdgeLabelRenderer() {
    return edgeLabelRenderer;
  }

  /** @param edgeLabelRenderer the edgeLabelRenderer to set */
  public void setEdgeLabelRenderer(Renderer.EdgeLabel<N, E> edgeLabelRenderer) {
    this.edgeLabelRenderer = edgeLabelRenderer;
  }

  /** @return the nodeLabelRenderer */
  public NodeLabel<N, E> getNodeLabelRenderer() {
    return nodeLabelRenderer;
  }

  /** @param nodeLabelRenderer the nodeLabelRenderer to set */
  public void setNodeLabelRenderer(NodeLabel<N, E> nodeLabelRenderer) {
    this.nodeLabelRenderer = nodeLabelRenderer;
  }

  /** @return the edgeRenderer */
  public Renderer.Edge<N, E> getEdgeRenderer() {
    return edgeRenderer;
  }

  /** @return the nodeRenderer */
  public Node<N, E> getNodeRenderer() {
    return nodeRenderer;
  }
}
