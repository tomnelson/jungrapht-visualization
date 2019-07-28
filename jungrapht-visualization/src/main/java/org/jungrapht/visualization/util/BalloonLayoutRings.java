package org.jungrapht.visualization.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.Point;

public class BalloonLayoutRings<V, E> implements VisualizationServer.Paintable {

  BalloonLayoutAlgorithm<V> layoutAlgorithm;
  VisualizationServer<V, E> vv;

  public BalloonLayoutRings(
      VisualizationServer<V, E> vv, BalloonLayoutAlgorithm<V> layoutAlgorithm) {
    this.vv = vv;
    this.layoutAlgorithm = layoutAlgorithm;
  }

  public void paint(Graphics g) {
    g.setColor(Color.gray);

    Graphics2D g2d = (Graphics2D) g;

    Ellipse2D ellipse = new Ellipse2D.Double();
    for (V v : vv.getModel().getGraph().vertexSet()) {
      Double radius = layoutAlgorithm.getRadii().get(v);
      if (radius == null) {
        continue;
      }
      Point p = vv.getModel().getLayoutModel().apply(v);
      ellipse.setFrame(-radius, -radius, 2 * radius, 2 * radius);
      AffineTransform at = AffineTransform.getTranslateInstance(p.x, p.y);
      Shape shape = at.createTransformedShape(ellipse);
      shape = vv.getTransformSupport().transform(vv, shape);
      g2d.draw(shape);
    }
  }

  public boolean useTransform() {
    return false;
  }
}
