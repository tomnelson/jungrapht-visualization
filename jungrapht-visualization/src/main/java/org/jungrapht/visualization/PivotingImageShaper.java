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

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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
public class PivotingImageShaper {

  /** the number of pixels to skip while sampling the images edges */
  static int sample = 1;
  /** the first x coordinate of the shape. Used to discern when we are done */
  static int firstx = 0;

  /**
   * Given an image, possibly with a transparent background, return the Shape of the opaque part of
   * the image
   *
   * @param image the image whose shape is being returned
   * @return the Shape
   */
  public static Shape getShape(BufferedImage image) {
    firstx = 0;
    return leftEdge(image, new Path2D.Double());
  }

  private static Point2D detectLine(Point2D p1, Point2D p2, Point2D p, Line2D line, Path2D path) {
    if (p2 == null) {
      p2 = p;
      line.setLine(p1, p2);
    }
    // check for line
    else if (line.ptLineDistSq(p) < 1) { // its on the line
      // make it p2
      p2.setLocation(p);
    } else { // its not on the current line
      p1.setLocation(p2);
      p2.setLocation(p);
      line.setLine(p1, p2);
      path.lineTo(p1.getX(), p1.getY());
    }
    return p2;
  }
  /**
   * trace the left side of the image
   *
   * @param image
   * @param path
   * @return
   */
  private static Shape leftEdge(BufferedImage image, Path2D path) {
    int lastj = 0;
    Point2D p1 = null;
    Point2D p2 = null;
    Line2D line = new Line2D.Double();
    for (int i = 0; i < image.getHeight(); i += sample) {
      boolean aPointExistsOnThisLine = false;
      // go until we reach an opaque point, then stop
      for (int j = 0; j < image.getWidth(); j += sample) {
        if ((image.getRGB(j, i) & 0xff000000) != 0) {
          // this is a point I want
          Point2D p = new Point2D.Double(j, i);
          aPointExistsOnThisLine = true;
          if (path.getCurrentPoint() != null) {
            // this is a continuation of a path
            p2 = detectLine(p1, p2, p, line, path);
          } else {
            // this is the first point in the path
            path.moveTo(j, i);
            firstx = j;
            p1 = p;
          }
          lastj = j;
          break;
        }
      }
      if (!aPointExistsOnThisLine) {
        break;
      }
    }
    return bottomEdge(image, path, lastj);
  }

  /**
   * trace the bottom of the image
   *
   * @param image
   * @param path
   * @param start
   * @return
   */
  private static Shape bottomEdge(BufferedImage image, Path2D path, int start) {
    int lastj = 0;
    Point2D p1 = path.getCurrentPoint();
    Point2D p2 = null;
    Line2D line = new Line2D.Double();
    for (int i = start; i < image.getWidth(); i += sample) {
      boolean aPointExistsOnThisLine = false;
      for (int j = image.getHeight() - 1; j >= 0; j -= sample) {
        if ((image.getRGB(i, j) & 0xff000000) != 0) {
          // this is a point I want
          Point2D p = new Point2D.Double(i, j);
          aPointExistsOnThisLine = true;
          p2 = detectLine(p1, p2, p, line, path);
          lastj = j;
          break;
        }
      }
      if (!aPointExistsOnThisLine) {
        break;
      }
    }
    return rightEdge(image, path, lastj);
  }

  /**
   * trace the right side of the image
   *
   * @param image
   * @param path
   * @param start
   * @return
   */
  private static Shape rightEdge(BufferedImage image, Path2D path, int start) {
    int lastj = 0;
    Point2D p1 = path.getCurrentPoint();
    Point2D p2 = null;
    Line2D line = new Line2D.Double();
    for (int i = start; i >= 0; i -= sample) {
      boolean aPointExistsOnThisLine = false;

      for (int j = image.getWidth() - 1; j >= 0; j -= sample) {
        if ((image.getRGB(j, i) & 0xff000000) != 0) {
          // this is a point I want
          Point2D p = new Point2D.Double(j, i);
          aPointExistsOnThisLine = true;
          p2 = detectLine(p1, p2, p, line, path);
          lastj = j;
          break;
        }
      }
      if (!aPointExistsOnThisLine) {
        break;
      }
    }
    return topEdge(image, path, lastj);
  }

  /**
   * trace the top of the image
   *
   * @param image
   * @param path
   * @param start
   * @return
   */
  private static Shape topEdge(BufferedImage image, Path2D path, int start) {
    Point2D p1 = path.getCurrentPoint();
    Point2D p2 = null;
    Line2D line = new Line2D.Double();
    for (int i = start; i >= firstx; i -= sample) {
      boolean aPointExistsOnThisLine = false;
      for (int j = 0; j < image.getHeight(); j += sample) {
        if ((image.getRGB(i, j) & 0xff000000) != 0) {
          // this is a point I want
          Point2D p = new Point2D.Double(i, j);
          aPointExistsOnThisLine = true;
          p2 = detectLine(p1, p2, p, line, path);
          break;
        }
      }
      if (!aPointExistsOnThisLine) {
        break;
      }
    }
    path.closePath();
    return path;
  }
}
