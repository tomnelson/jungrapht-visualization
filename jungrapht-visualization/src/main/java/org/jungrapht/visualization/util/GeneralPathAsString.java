package org.jungrapht.visualization.util;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class GeneralPathAsString {

  public static String toString(Path2D newPath) {
    StringBuilder sb = new StringBuilder();
    float[] coords = new float[6];
    for (PathIterator iterator = newPath.getPathIterator(null);
        !iterator.isDone();
        iterator.next()) {
      int type = iterator.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          Point2D p = new Point2D.Double(coords[0], coords[1]);
          sb.append("moveTo ").append(p).append("--");
          break;

        case PathIterator.SEG_LINETO:
          p = new Point2D.Double(coords[0], coords[1]);
          sb.append("lineTo ").append(p).append("--");
          break;

        case PathIterator.SEG_QUADTO:
          p = new Point2D.Double(coords[0], coords[1]);
          Point2D q = new Point2D.Double(coords[2], coords[3]);
          sb.append("quadTo ").append(p).append(" controlled by ").append(q);
          break;

        case PathIterator.SEG_CUBICTO:
          p = new Point2D.Double(coords[0], coords[1]);
          q = new Point2D.Double(coords[2], coords[3]);
          Point2D r = new Point2D.Double(coords[4], coords[5]);
          sb.append("cubeTo ").append(p).append(" controlled by ").append(q).append(",").append(r);

          break;

        case PathIterator.SEG_CLOSE:
          newPath.closePath();
          sb.append("close");
          break;

        default:
      }
    }
    return sb.toString();
  }
}
