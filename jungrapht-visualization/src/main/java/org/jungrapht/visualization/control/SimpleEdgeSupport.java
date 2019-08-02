package org.jungrapht.visualization.control;

import com.google.common.base.Preconditions;
import java.awt.geom.Point2D;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jungrapht.visualization.DefaultVisualizationServer;

public class SimpleEdgeSupport<V, E> implements EdgeSupport<V, E> {

  protected Point2D down;
  protected EdgeEffects<V, E> edgeEffects;
  protected Supplier<E> edgeFactory;
  protected V startVertex;

  public SimpleEdgeSupport(Supplier<E> edgeFactory) {
    this.edgeFactory = edgeFactory;
    this.edgeEffects = new CubicCurveEdgeEffects<>();
  }

  @Override
  public void startEdgeCreate(
      DefaultVisualizationServer<V, E> vv, V startVertex, Point2D startPoint) {
    this.startVertex = startVertex;
    this.down = startPoint;
    this.edgeEffects.startEdgeEffects(vv, startPoint, startPoint);
    if (vv.getModel().getGraph().getType().isDirected()) {
      this.edgeEffects.startArrowEffects(vv, startPoint, startPoint);
    }
    vv.repaint();
  }

  @Override
  public void midEdgeCreate(DefaultVisualizationServer<V, E> vv, Point2D midPoint) {
    if (startVertex != null) {
      this.edgeEffects.midEdgeEffects(vv, down, midPoint);
      if (vv.getModel().getGraph().getType().isDirected()) {
        this.edgeEffects.midArrowEffects(vv, down, midPoint);
      }
      vv.repaint();
    }
  }

  @Override
  public void endEdgeCreate(DefaultVisualizationServer<V, E> vv, V endVertex) {
    Preconditions.checkState(
        vv.getModel().getGraph() instanceof Graph<?, ?>, "graph must be mutable");
    if (startVertex != null) {
      Graph<V, E> graph = vv.getModel().getGraph();
      graph.addEdge(startVertex, endVertex, edgeFactory.get());
      vv.getEdgeSpatial().recalculate();
      vv.repaint();
    }
    startVertex = null;
    edgeEffects.endEdgeEffects(vv);
    edgeEffects.endArrowEffects(vv);
  }

  @Override
  public void abort(DefaultVisualizationServer<V, E> vv) {
    startVertex = null;
    edgeEffects.endEdgeEffects(vv);
    edgeEffects.endArrowEffects(vv);
    vv.repaint();
  }

  public EdgeEffects<V, E> getEdgeEffects() {
    return edgeEffects;
  }

  public void setEdgeEffects(EdgeEffects<V, E> edgeEffects) {
    this.edgeEffects = edgeEffects;
  }

  public Supplier<E> getEdgeFactory() {
    return edgeFactory;
  }

  public void setEdgeFactory(Supplier<E> edgeFactory) {
    this.edgeFactory = edgeFactory;
  }
}
