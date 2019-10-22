package org.jungrapht.visualization.decorators;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.List;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapeFunctions {

  private static Logger log = LoggerFactory.getLogger(ShapeFunctions.class);

  public static Shape makeShape(List<Point> list) {
    GeneralPath path = new GeneralPath();
    for (int i = 0; i < list.size(); i++) {
      Point pt = list.get(i);
      if (i == 0) {
        path.moveTo(pt.x, pt.y);
        log.trace("moveTo({}, {})", pt.x, pt.y);
      } else {
        path.lineTo(pt.x, pt.y);
        log.trace("lineTo({}, {})", pt.x, pt.y);
      }
    }
    // i have a shape from the start to end on the real coords
    Point firstPoint = list.get(0);
    Point lastPoint = list.get(list.size() - 1);
    Point sum = lastPoint.add(-firstPoint.x, -firstPoint.y);
    PolarPoint diffPolar = PolarPoint.cartesianToPolar(sum);
    double theta = diffPolar.theta;
    log.trace("theta is {}", theta);
    AffineTransform at =
        AffineTransform.getScaleInstance(1.0 / diffPolar.radius, 1.0 / diffPolar.radius);
    at.rotate(-theta);
    at.translate(-list.get(0).x, -list.get(0).y);
    Shape theShape = at.createTransformedShape(path);
    return theShape;
  }

  public static Shape placeEdgeShape(Shape shape, Point sourcePoint, Point targetPoint) {
    AffineTransform xform = AffineTransform.getTranslateInstance(sourcePoint.x, sourcePoint.y);
    double dx = targetPoint.x - sourcePoint.x;
    double dy = targetPoint.y - sourcePoint.y;
    // angle of edge at desired location
    double thetaRadians = Math.atan2(dy, dx);
    xform.rotate(thetaRadians);
    // length of edge at desired location
    double dist = Math.sqrt(dx * dx + dy * dy);
    xform.scale(dist, dist);
    Shape edgeShape = xform.createTransformedShape(shape);
    return edgeShape;
  }
}
