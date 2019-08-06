package org.jungrapht.visualization.renderers;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;

import java.awt.*;
import java.util.ConcurrentModificationException;
import org.jgrapht.Graph;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.spatial.Spatial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightweightRenderer<V, E> implements Renderer<V, E> {

  private static final Logger log = LoggerFactory.getLogger(LightweightRenderer.class);
  protected Vertex<V, E> vertexRenderer = new LightweightVertexRenderer<>();
  protected VertexLabel<V, E> vertexLabelRenderer = new DefaultVertexLabelRenderer<>();
  protected Renderer.Edge<V, E> edgeRenderer = new LightweightEdgeRenderer<>();

  @Override
  public void render(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      Spatial<V> vertexSpatial,
      Spatial<E> edgeSpatial) {

    renderContext.getGraphicsContext().setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);

    Iterable<V> visibleVertices;
    Iterable<E> visibleEdges;

    try {
      visibleVertices = visualizationModel.getGraph().vertexSet();
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
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  @Override
  public void render(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel) {
    renderContext.getGraphicsContext().setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);

    Graph<V, E> graph = visualizationModel.getGraph();
    // paint all the edges
    try {
      for (E e : graph.edgeSet()) {
        renderEdge(renderContext, visualizationModel, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (V v : graph.vertexSet()) {
        renderVertex(renderContext, visualizationModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  @Override
  public void renderVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    vertexRenderer.paintVertex(renderContext, visualizationModel, v);
  }

  @Override
  public void renderVertexLabel(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    vertexLabelRenderer.labelVertex(
        renderContext, visualizationModel, v, renderContext.getVertexLabelFunction().apply(v));
  }

  @Override
  public void renderEdge(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {
    edgeRenderer.paintEdge(renderContext, visualizationModel, e);
  }

  @Override
  public void renderEdgeLabel(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {}

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
    return null;
  }

  /** @param edgeLabelRenderer the edgeLabelRenderer to set */
  @Override
  public void setEdgeLabelRenderer(Renderer.EdgeLabel<V, E> edgeLabelRenderer) {}

  /** @return the vertexLabelRenderer */
  @Override
  public VertexLabel<V, E> getVertexLabelRenderer() {
    return null;
  }

  /** @param vertexLabelRenderer the vertexLabelRenderer to set */
  @Override
  public void setVertexLabelRenderer(VertexLabel<V, E> vertexLabelRenderer) {}

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
