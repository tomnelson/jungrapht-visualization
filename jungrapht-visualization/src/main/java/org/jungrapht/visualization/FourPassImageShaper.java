/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jun 17, 2005
 */

package org.jungrapht.visualization;

import java.awt.Shape;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Provides Supplier methods that, given a BufferedImage, an Image, or the fileName of an image,
 * will return a java.awt.Shape that is the contiguous traced outline of the opaque part of the
 * image. This could be used to define an image for use in a Vertex, where the shape used for
 * picking and edge-arrow placement follows the opaque part of an image that has a transparent
 * background. The methods try to detect lines in order to minimize points in the path
 *
 * @author Tom Nelson
 */
public class FourPassImageShaper {

  public static Shape getShape(BufferedImage image) {
    Area area = new Area(leftEdge(image));
    area.intersect(new Area(bottomEdge(image)));
    area.intersect(new Area(rightEdge(image)));
    area.intersect(new Area(topEdge(image)));
    return area;
  }
  /**
   * Checks to see if point p is on a line that passes thru points p1 and p2. If p is on the line,
   * extend the line segment so that it is from p1 to the location of p. If the point p is not on
   * the line, update my shape with a line extending to the old p2 location, make the old p2 the new
   * p1, and make p2 the old p
   *
   * @param p1 first end point
   * @param p2 last end point
   * @param p point on the line
   * @param line line so far
   * @param path path to search for the point p
   * @return the last point
   */
  private static Point2D detectLine(Point2D p1, Point2D p2, Point2D p, Line2D line, Path2D path) {

    // check for line
    // if p is on the line that extends thru p1 and p2
    if (line.ptLineDistSq(p) == 0) { // p is on the line p1,p2
      // extend line so that p2 is at p
      p2.setLocation(p);
    } else { // its not on the current line
      // start a new line from p2 to p
      p1.setLocation(p2);
      p2.setLocation(p);
      line.setLine(p1, p2);
      // end the ongoing path line at the new p1 (the old p2)
      path.lineTo(p1.getX(), p1.getY());
    }
    return p2;
  }
  /**
   * trace the left side of the image
   *
   * @param image image to find boundary of
   * @return the Shape of the left edge of the image
   */
  private static Shape leftEdge(BufferedImage image) {
    Path2D path = new Path2D.Double();
    Point2D p1 = null;
    Point2D p2 = null;
    Line2D line = new Line2D.Double();
    Point2D p = new Point2D.Double();
    int foundPointY = -1;
    for (int i = 0; i < image.getHeight(); i++) {
      // go until we reach an opaque point, then stop
      for (int j = 0; j < image.getWidth(); j++) {
        if ((image.getRGB(j, i) & 0xff000000) != 0) {
          // this is a point I want
          p = new Point2D.Double(j, i);
          foundPointY = i;
          break;
        }
      }
      if (foundPointY >= 0) {
        if (p2 == null) {
          // this is the first point found. project line to right edge
          p1 = new Point2D.Double(image.getWidth() - 1, foundPointY);
          path.moveTo(p1.getX(), p1.getY());
          p2 = new Point2D.Double();
          p2.setLocation(p);
        } else {
          p2 = detectLine(p1, p2, p, line, path);
        }
      }
    }
    path.lineTo(p.getX(), p.getY());
    if (foundPointY >= 0) {
      path.lineTo(image.getWidth() - 1, foundPointY);
    }
    path.closePath();
    return path;
  }

  /**
   * trace the bottom of the image
   *
   * @param image image to fimd the boundary of
   * @return the Shape of the bottom edge of the image
   */
  private static Shape bottomEdge(BufferedImage image) {
    Path2D path = new Path2D.Double();
    Point2D p1 = null;
    Point2D p2 = null;
    Line2D line = new Line2D.Double();
    Point2D p = new Point2D.Double();
    int foundPointX = -1;
    for (int i = 0; i < image.getWidth(); i++) {
      for (int j = image.getHeight() - 1; j >= 0; j--) {
        if ((image.getRGB(i, j) & 0xff000000) != 0) {
          // this is a point I want
          p.setLocation(i, j);
          foundPointX = i;
          break;
        }
      }
      if (foundPointX >= 0) {
        if (p2 == null) {
          // this is the first point found. project line to top edge
          p1 = new Point2D.Double(foundPointX, 0);
          // path starts here
          path.moveTo(p1.getX(), p1.getY());
          p2 = new Point2D.Double();
          p2.setLocation(p);
        } else {
          p2 = detectLine(p1, p2, p, line, path);
        }
      }
    }
    path.lineTo(p.getX(), p.getY());
    if (foundPointX >= 0) {
      path.lineTo(foundPointX, 0);
    }
    path.closePath();
    return path;
  }

  /**
   * trace the right side of the image
   *
   * @param image the input image
   * @return the Shape of the right edge
   */
  private static Shape rightEdge(BufferedImage image) {
    Path2D path = new Path2D.Double();
    Point2D p1 = null;
    Point2D p2 = null;
    Line2D line = new Line2D.Double();
    Point2D p = new Point2D.Double();
    int foundPointY = -1;

    for (int i = image.getHeight() - 1; i >= 0; i--) {
      for (int j = image.getWidth() - 1; j >= 0; j--) {
        if ((image.getRGB(j, i) & 0xff000000) != 0) {
          // this is a point I want
          p.setLocation(j, i);
          foundPointY = i;
          break;
        }
      }
      if (foundPointY >= 0) {
        if (p2 == null) {
          // this is the first point found. project line to top edge
          p1 = new Point2D.Double(0, foundPointY);
          // path starts here
          path.moveTo(p1.getX(), p1.getY());
          p2 = new Point2D.Double();
          p2.setLocation(p);
        } else {
          p2 = detectLine(p1, p2, p, line, path);
        }
      }
    }
    path.lineTo(p.getX(), p.getY());
    if (foundPointY >= 0) {
      path.lineTo(0, foundPointY);
    }
    path.closePath();
    return path;
  }

  /**
   * trace the top of the image
   *
   * @param image image to find the boundary of
   * @return the Shape of the top boundary
   */
  private static Shape topEdge(BufferedImage image) {
    Path2D path = new Path2D.Double();
    Point2D p1 = null;
    Point2D p2 = null;
    Line2D line = new Line2D.Double();
    Point2D p = new Point2D.Double();
    int foundPointX = -1;

    for (int i = image.getWidth() - 1; i >= 0; i--) {
      for (int j = 0; j < image.getHeight(); j++) {
        if ((image.getRGB(i, j) & 0xff000000) != 0) {
          // this is a point I want
          p.setLocation(i, j);
          foundPointX = i;
          break;
        }
      }
      if (foundPointX >= 0) {
        if (p2 == null) {
          // this is the first point found. project line to top edge
          p1 = new Point2D.Double(foundPointX, image.getHeight() - 1);
          // path starts here
          path.moveTo(p1.getX(), p1.getY());
          p2 = new Point2D.Double();
          p2.setLocation(p);
        } else {
          p2 = detectLine(p1, p2, p, line, path);
        }
      }
    }
    path.lineTo(p.getX(), p.getY());
    if (foundPointX >= 0) {
      path.lineTo(foundPointX, image.getHeight() - 1);
    }
    path.closePath();
    return path;
  }
}
