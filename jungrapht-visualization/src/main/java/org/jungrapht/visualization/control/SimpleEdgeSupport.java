package org.jungrapht.visualization.control;

import com.google.common.base.Preconditions;
import java.awt.geom.Point2D;
import java.util.function.Supplier;
import org.jgrapht.Graph;
import org.jungrapht.visualization.BasicVisualizationServer;

public class SimpleEdgeSupport<N, E> implements EdgeSupport<N, E> {

  protected Point2D down;
  protected EdgeEffects<N, E> edgeEffects;
  protected Supplier<E> edgeFactory;
  protected N startNode;

  public SimpleEdgeSupport(Supplier<E> edgeFactory) {
    this.edgeFactory = edgeFactory;
    this.edgeEffects = new CubicCurveEdgeEffects<>();
  }

  @Override
  public void startEdgeCreate(BasicVisualizationServer<N, E> vv, N startNode, Point2D startPoint) {
    this.startNode = startNode;
    this.down = startPoint;
    this.edgeEffects.startEdgeEffects(vv, startPoint, startPoint);
    if (vv.getModel().getNetwork().getType().isDirected()) {
      this.edgeEffects.startArrowEffects(vv, startPoint, startPoint);
    }
    vv.repaint();
  }

  @Override
  public void midEdgeCreate(BasicVisualizationServer<N, E> vv, Point2D midPoint) {
    if (startNode != null) {
      this.edgeEffects.midEdgeEffects(vv, down, midPoint);
      if (vv.getModel().getNetwork().getType().isDirected()) {
        this.edgeEffects.midArrowEffects(vv, down, midPoint);
      }
      vv.repaint();
    }
  }

  @Override
  public void endEdgeCreate(BasicVisualizationServer<N, E> vv, N endNode) {
    Preconditions.checkState(
        vv.getModel().getNetwork() instanceof Graph<?, ?>, "graph must be mutable");
    if (startNode != null) {
      Graph<N, E> graph = vv.getModel().getNetwork();
      graph.addEdge(startNode, endNode, edgeFactory.get());
      vv.getEdgeSpatial().recalculate();
      vv.repaint();
    }
    startNode = null;
    edgeEffects.endEdgeEffects(vv);
    edgeEffects.endArrowEffects(vv);
  }

  @Override
  public void abort(BasicVisualizationServer<N, E> vv) {
    startNode = null;
    edgeEffects.endEdgeEffects(vv);
    edgeEffects.endArrowEffects(vv);
    vv.repaint();
  }

  public EdgeEffects<N, E> getEdgeEffects() {
    return edgeEffects;
  }

  public void setEdgeEffects(EdgeEffects<N, E> edgeEffects) {
    this.edgeEffects = edgeEffects;
  }

  public Supplier<E> getEdgeFactory() {
    return edgeFactory;
  }

  public void setEdgeFactory(Supplier<E> edgeFactory) {
    this.edgeFactory = edgeFactory;
  }
}
