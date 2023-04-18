package org.jungrapht.visualization.control;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.util.ArrowFactory;

public class CubicCurveEdgeEffects<V, E> implements EdgeEffects<V, E> {

  protected CubicCurve2D rawEdge = new CubicCurve2D.Double();
  protected Shape edgeShape;
  protected Shape rawArrowShape;
  protected Shape arrowShape;
  protected VisualizationServer.Paintable edgePaintable;
  protected VisualizationServer.Paintable arrowPaintable;

  public CubicCurveEdgeEffects() {
    this.rawEdge.setCurve(0.0f, 0.0f, 0.33f, 100, 0.66f, -50, 1.0f, 0.0f);
    rawArrowShape = ArrowFactory.getNotchedArrow(20, 16, 8);
    this.edgePaintable = new EdgePaintable();
    this.arrowPaintable = new ArrowPaintable();
  }

  @Override
  public void startEdgeEffects(VisualizationServer<V, E> vv, Point2D down, Point2D out) {
    transformEdgeShape(down, out);
    vv.addPostRenderPaintable(edgePaintable);
  }

  @Override
  public void midEdgeEffects(VisualizationServer<V, E> vv, Point2D down, Point2D out) {
    transformEdgeShape(down, out);
  }

  @Override
  public void endEdgeEffects(VisualizationServer<V, E> vv) {
    vv.removePostRenderPaintable(edgePaintable);
  }

  @Override
  public void startArrowEffects(VisualizationServer<V, E> vv, Point2D down, Point2D out) {
    transformArrowShape(down, out);
    vv.addPostRenderPaintable(arrowPaintable);
  }

  @Override
  public void midArrowEffects(VisualizationServer<V, E> vv, Point2D down, Point2D out) {
    transformArrowShape(down, out);
  }

  @Override
  public void endArrowEffects(VisualizationServer<V, E> vv) {
    vv.removePostRenderPaintable(arrowPaintable);
  }

  /** code lifted from PluggableRenderer to move an edge shape into an arbitrary position */
  private void transformEdgeShape(Point2D down, Point2D out) {
    double x1 = down.getX();
    double y1 = down.getY();
    double x2 = out.getX();
    double y2 = out.getY();

    AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

    double dx = x2 - x1;
    double dy = y2 - y1;
    double thetaRadians = Math.atan2(dy, dx);
    xform.rotate(thetaRadians);
    double dist = Math.sqrt(dx * dx + dy * dy);
    xform.scale(dist / rawEdge.getBounds().getWidth(), 1.0);
    edgeShape = xform.createTransformedShape(rawEdge);
  }

  private void transformArrowShape(Point2D down, Point2D out) {
    double x1 = down.getX();
    double y1 = down.getY();
    double x2 = out.getX();
    double y2 = out.getY();

    AffineTransform xform = AffineTransform.getTranslateInstance(x2, y2);

    double dx = x2 - x1;
    double dy = y2 - y1;
    double thetaRadians = Math.atan2(dy, dx);
    xform.rotate(thetaRadians);
    arrowShape = xform.createTransformedShape(rawArrowShape);
  }
  /** Used for the edge creation visual effect during mouse drag */
  class EdgePaintable implements VisualizationServer.Paintable {

    public void paint(Graphics g) {
      if (edgeShape != null) {
        Color oldColor = g.getColor();
        g.setColor(Color.black);
        ((Graphics2D) g).draw(edgeShape);
        g.setColor(oldColor);
      }
    }

    public boolean useTransform() {
      return false;
    }
  }

  /** Used for the directed edge creation visual effect during mouse drag */
  class ArrowPaintable implements VisualizationServer.Paintable {

    public void paint(Graphics g) {
      if (arrowShape != null) {
        Color oldColor = g.getColor();
        g.setColor(Color.black);
        ((Graphics2D) g).fill(arrowShape);
        g.setColor(oldColor);
      }
    }

    public boolean useTransform() {
      return false;
    }
  }
}
