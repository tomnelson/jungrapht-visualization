/*
 * Copyright (c) 2015, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Nov 7, 2015
 */

package org.jungrapht.visualization.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.jungrapht.visualization.FourPassImageShaper;

public class ImageShapeUtils {

  /**
   * Given the fileName of an image, possibly with a transparent background, return the Shape of the
   * opaque part of the image
   *
   * @param fileName name of the image, loaded from the classpath
   * @return the Shape
   */
  public static Shape getShape(String fileName) {
    return getShape(fileName, Integer.MAX_VALUE);
  }

  /**
   * Given the fileName of an image, possibly with a transparent background, return the Shape of the
   * opaque part of the image
   *
   * @param fileName name of the image, loaded from the classpath
   * @param max the maximum dimension of the traced shape
   * @return the Shape
   * @see #getShape(Image, int)
   */
  public static Shape getShape(String fileName, int max) {
    BufferedImage image = null;
    try {
      image = ImageIO.read(ImageShapeUtils.class.getResource(fileName));
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return getShape(Objects.requireNonNull(image), max);
  }

  /**
   * Given an image, possibly with a transparent background, return the Shape of the opaque part of
   * the image
   *
   * @param image the image whose shape is to be returned
   * @return the Shape
   */
  public static Shape getShape(Image image) {
    return getShape(image, Integer.MAX_VALUE);
  }

  public static Shape getShape(Image image, int max) {
    BufferedImage bi =
        new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    Graphics g = bi.createGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    return getShape(bi, max);
  }

  /**
   * Given an image, possibly with a transparent background, return the Shape of the opaque part of
   * the image
   *
   * <p>If the image is larger than max in either direction, scale the image down to max-by-max, do
   * the trace (on fewer points) then scale the resulting shape back up to the layoutSize of the
   * original image.
   *
   * @param image the image to trace
   * @param max used to restrict number of points in the resulting shape
   * @return the Shape
   */
  public static Shape getShape(BufferedImage image, int max) {
    double width = image.getWidth();
    double height = image.getHeight();
    if (width > max || height > max) {
      BufferedImage smaller = new BufferedImage(max, max, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = smaller.createGraphics();
      AffineTransform at = AffineTransform.getScaleInstance(max / width, max / height);
      AffineTransform back = AffineTransform.getScaleInstance(width / max, height / max);
      g.drawImage(image, at, null);
      g.dispose();
      return back.createTransformedShape(getShape(smaller));
    } else {
      return FourPassImageShaper.getShape(image);
    }
  }
}
