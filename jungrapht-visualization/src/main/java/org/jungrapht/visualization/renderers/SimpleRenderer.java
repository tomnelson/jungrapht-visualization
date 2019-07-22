package org.jungrapht.visualization.renderers;

import java.util.ConcurrentModificationException;
import org.jgrapht.Graph;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.spatial.Spatial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleRenderer<N, E> implements Renderer<N, E> {

  private static final Logger log = LoggerFactory.getLogger(SimpleRenderer.class);
  protected Node<N, E> nodeRenderer = new SimpleNodeRenderer<>();
  protected NodeLabel<N, E> nodeLabelRenderer = new BasicNodeLabelRenderer<>();
  protected Renderer.Edge<N, E> edgeRenderer = new SimpleEdgeRenderer<>();
  //    protected Function<N, Shape> simpleNodeShapeFunction = new EllipseNodeShapeFunction<>();
  //    protected Renderer.EdgeLabel<N, E> edgeLabelRenderer = new BasicEdgeLabelRenderer<>();

  @Override
  public void render(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E> visualizationModel,
      Spatial<N> nodeSpatial,
      Spatial<E> edgeSpatial) {
    // simple rendering does not use spatial structures
    render(renderContext, visualizationModel);
    log.trace("simpleRendering ignoring {}", nodeSpatial);
    //        if (nodeSpatial == null) {
    //            render(renderContext, visualizationModel);
    //            return;
    //        }
    //        Iterable<N> visibleNodes;
    //        Iterable<E> visibleEdges;
    //
    //        try {
    //            visibleNodes =
    //                    nodeSpatial.getVisibleElements(
    //                            ((VisualizationServer) renderContext.getScreenDevice()).viewOnLayout());
    //
    //            if (edgeSpatial != null) {
    //                visibleEdges =
    //                        edgeSpatial.getVisibleElements(
    //                                ((VisualizationServer) renderContext.getScreenDevice()).viewOnLayout());
    //            } else {
    //                visibleEdges = visualizationModel.getNetwork().edgeSet();
    //            }
    //        } catch (ConcurrentModificationException ex) {
    //            // skip rendering until graph node index is stable,
    //            // this can happen if the layout relax thread is changing locations while the
    //            // visualization is rendering
    //            log.info("got {} so returning", ex.toString());
    //            log.info(
    //                    "layoutMode active: {}, edgeSpatial active {}, nodeSpatial active: {}",
    //                    visualizationModel.getLayoutModel().isRelaxing(),
    //                    edgeSpatial.isActive(),
    //                    nodeSpatial.isActive());
    //            return;
    //        }
    //
    //        try {
    //            Graph<N, E> network = visualizationModel.getNetwork();
    //            // paint all the edges
    //            log.trace("the visibleEdges are {}", visibleEdges);
    //            for (E e : visibleEdges) {
    //                if (network.edgeSet().contains(e)) {
    //                    renderEdge(renderContext, visualizationModel, e);
    //                    //            renderEdgeLabel(renderContext, visualizationModel, e);
    //                }
    //            }
    //        } catch (ConcurrentModificationException cme) {
    //            renderContext.getScreenDevice().repaint();
    //        }
    //
    //        // paint all the nodes
    //        try {
    //            log.trace("the visibleNodes are {}", visibleNodes);
    //
    //            for (N v : visibleNodes) {
    //                renderNode(renderContext, visualizationModel, v);
    //            }
    //        } catch (ConcurrentModificationException cme) {
    //            renderContext.getScreenDevice().repaint();
    //        }
  }

  @Override
  public void render(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel) {
    Graph<N, E> network = visualizationModel.getNetwork();
    //        renderContext.setNodeShapeFunction(simpleNodeShapeFunction);
    // paint all the edges
    try {
      for (E e : network.edgeSet()) {
        renderEdge(renderContext, visualizationModel, e);
        //          renderEdgeLabel(renderContext, visualizationModel, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the nodes
    try {
      for (N v : network.vertexSet()) {
        renderNode(renderContext, visualizationModel, v);
        //          renderNodeLabel(renderContext, visualizationModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  @Override
  public void renderNode(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v) {
    nodeRenderer.paintNode(renderContext, visualizationModel, v);
  }

  @Override
  public void renderNodeLabel(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v) {
    nodeLabelRenderer.labelNode(
        renderContext, visualizationModel, v, renderContext.getNodeLabelFunction().apply(v));
  }

  @Override
  public void renderEdge(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, E e) {
    edgeRenderer.paintEdge(renderContext, visualizationModel, e);
  }

  @Override
  public void renderEdgeLabel(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, E e) {
    //      edgeLabelRenderer.labelEdge(
    //              renderContext, visualizationModel, e, renderContext.getEdgeLabelFunction().apply(e));
  }

  @Override
  public void setNodeRenderer(Node<N, E> r) {
    this.nodeRenderer = r;
  }

  @Override
  public void setEdgeRenderer(Renderer.Edge<N, E> r) {
    this.edgeRenderer = r;
  }

  /** @return the edgeLabelRenderer */
  @Override
  public Renderer.EdgeLabel<N, E> getEdgeLabelRenderer() {
    return null;
  }

  /** @param edgeLabelRenderer the edgeLabelRenderer to set */
  @Override
  public void setEdgeLabelRenderer(Renderer.EdgeLabel<N, E> edgeLabelRenderer) {
    //      this.edgeLabelRenderer = edgeLabelRenderer;
  }

  /** @return the nodeLabelRenderer */
  @Override
  public NodeLabel<N, E> getNodeLabelRenderer() {
    return null;
  }

  /** @param nodeLabelRenderer the nodeLabelRenderer to set */
  @Override
  public void setNodeLabelRenderer(NodeLabel<N, E> nodeLabelRenderer) {
    //      this.nodeLabelRenderer = nodeLabelRenderer;
  }

  /** @return the edgeRenderer */
  @Override
  public Renderer.Edge<N, E> getEdgeRenderer() {
    return edgeRenderer;
  }

  /** @return the nodeRenderer */
  @Override
  public Node<N, E> getNodeRenderer() {
    return nodeRenderer;
  }
}
