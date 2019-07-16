package org.jungrapht.samples.util;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;

public class RadialLayoutRings<N, E> implements VisualizationServer.Paintable {

  Collection<Double> depths;
  LayoutModel<N> layoutModel;
  VisualizationServer<N, E> vv;
  RadialTreeLayoutAlgorithm<N> radialTreeLayoutAlgorithm;

  public RadialLayoutRings(
      VisualizationServer<N, E> vv, RadialTreeLayoutAlgorithm<N> radialTreeLayoutAlgorithm) {
    this.vv = vv;
    this.layoutModel = vv.getModel().getLayoutModel();
    this.radialTreeLayoutAlgorithm = radialTreeLayoutAlgorithm;
    depths = getDepths();
  }

  private Collection<Double> getDepths() {
    Set<Double> depths = new HashSet<>();
    Map<N, PolarPoint> polarLocations = radialTreeLayoutAlgorithm.getPolarLocations();
    for (N v : vv.getModel().getNetwork().vertexSet()) {
      PolarPoint pp = polarLocations.get(v);
      depths.add(pp.radius);
    }
    return depths;
  }

  public void paint(Graphics g) {
    g.setColor(Color.lightGray);

    Graphics2D g2d = (Graphics2D) g;
    Point center = radialTreeLayoutAlgorithm.getCenter(layoutModel);

    Ellipse2D ellipse = new Ellipse2D.Double();
    for (double d : depths) {
      ellipse.setFrameFromDiagonal(center.x - d, center.y - d, center.x + d, center.y + d);
      Shape shape =
          vv.getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
              .transform(ellipse);
      g2d.draw(shape);
    }
  }

  public boolean useTransform() {
    return true;
  }
}
