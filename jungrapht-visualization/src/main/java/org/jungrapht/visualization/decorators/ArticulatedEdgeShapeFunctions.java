package org.jungrapht.visualization.decorators;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * functions to convert a list of layout coordinate system points into a unit shape from 0,0 to 1,0.
 * All edge shapes in jungrapht-visualization are transformed from the unit shape to the visualized
 * shape during the visualization process
 */
public class ArticulatedEdgeShapeFunctions {

  private static Logger log = LoggerFactory.getLogger(ArticulatedEdgeShapeFunctions.class);

  /**
   * This is for articulated edge shapes that have intermediate articulation points. From the
   * supplied list of points, make the 'unit' edge shape (0,0 to 1,0) by translating and scaling the
   * incoming points to a Path2D from 0,0 to 1,0; This 'unit' shape will be transformed to the graph
   * location during the visualization process
   *
   * @param list supplied points in layout coordinate system
   * @return a Shape (Path2D) extending from 0,0 to 1,0
   */
  public static Shape makeUnitShape(List<Point> list) {
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
    // path is a shape from the start to end in the layout coords
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
    // theShape starts at 0,0 and extends to 1,0
    return theShape;
  }

  /**
   * This is for articulated edge shapes that have intermediate articulation points. All feedback
   * arcs (edges that were reversed in direction during the remove-cycles process) need to have
   * their intermediate points modified so that when the edges are transformed into position, the
   * articulation points are 'reversed' in cartesian space. From the supplied list of points, make
   * the edge shape (0,0 to 1,0) as above, but if there are points between 0,0 and 1,0 flip those
   * points vertically (y = -y) and move them horizontally (x = 1-x) so that the resulting shape
   * articulation points are correct for the reversed edge direction.
   *
   * @param list supplied points in layout coordinate system
   * @return a Shape (Path2D) extending from 0,0 to 1,0
   */
  public static Shape makeReverseUnitShape(List<Point> list) {
    if (list.size() > 2) { // only for edges with articulation points
      Shape shape =
          makeUnitShape(list); // create the basic shape (0,0 to 1,0) for the 'reversed' edge
      if (shape instanceof Path2D) { // it is
        // gather the points from the path
        List<Point> pathPoints = new ArrayList<>();
        float[] coords = new float[6];
        Path2D path = (Path2D) shape;
        for (PathIterator iterator = path.getPathIterator(null, 1);
            !iterator.isDone();
            iterator.next()) {
          int type = iterator.currentSegment(coords);
          switch (type) {
            case PathIterator.SEG_MOVETO:
              pathPoints.add(Point.of(coords[0], coords[1]));
              break;
            case PathIterator.SEG_LINETO:
              pathPoints.add(Point.of(coords[0], coords[1]));
              break;
          }
        }
        // change the middle points (not end points) of the pathPoints list
        for (int i = 1; i < pathPoints.size() - 1; i++) {
          Point p = pathPoints.get(i);
          double y = -p.y;
          double x = 1.0 - p.x;
          pathPoints.set(i, Point.of(x, y));
        }
        // make a new Path2D from newList
        Path2D newPath = new GeneralPath();
        Point p = pathPoints.get(0);
        newPath.moveTo(p.x, p.y);
        for (int i = 1; i < pathPoints.size() - 1; i++) {
          p = pathPoints.get(i);
          newPath.lineTo(p.x, p.y);
        }
        p = pathPoints.get(pathPoints.size() - 1);
        newPath.lineTo(p.x, p.y);
        return newPath;
      }
    }
    return makeUnitShape(list);
  }
}
