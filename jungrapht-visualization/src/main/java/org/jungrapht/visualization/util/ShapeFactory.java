/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jul 20, 2004
 */
package org.jungrapht.visualization.util;

import java.awt.*;
import java.awt.geom.*;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for generating <code>Shape</code>s for drawing objects. The available shapes
 * include rectangles, rounded rectangles, ellipses, regular polygons, and regular stars. The
 * dimensions of the requested shapes are defined by the specified size function (specified by a
 * {@code Function<T, Integer>}) and aspect ratio function (specified by a {@code Function<T,
 * Float>}) implementations: the width of the bounding box of the shape is given by the size, and
 * the height is given by the size multiplied by the aspect ratio.
 */
public class ShapeFactory<T> {

  private static final Logger log = LoggerFactory.getLogger(ShapeFactory.class);
  protected Function<T, Integer> sizeFunction;
  protected Function<T, Float> aspectRatioFunction;

  /**
   * Creates an instance with the specified size and aspect ratio functions.
   *
   * @param sizeFunction provides a size (width) for each input object
   * @param aspectRatioFunction provides a height/width ratio for each input object
   */
  public ShapeFactory(Function<T, Integer> sizeFunction, Function<T, Float> aspectRatioFunction) {
    this.sizeFunction = sizeFunction;
    this.aspectRatioFunction = aspectRatioFunction;
  }

  /**
   * Creates a <code>ShapeFactory</code> with a constant size of 10 and a constant aspect ratio of
   * 1.
   */
  public ShapeFactory() {
    this(n -> 10, n -> 1.0f);
  }

  private static final Rectangle2D theRectangle = new Rectangle2D.Float();

  /**
   * Returns a <code>Rectangle2D</code> whose width and height are defined by this instance's size
   * and aspect ratio functions.
   *
   * @param t the object for which the shape will be drawn
   * @return a rectangle for this input T
   */
  public Shape getRectangle(T t, double rotation) {
    float width = sizeFunction.apply(t);
    float height = width * aspectRatioFunction.apply(t);
    theRectangle.setFrame(0, 0, width, height);

    AffineTransform at = new AffineTransform();
    if (rotation != 0) {
      at.rotate(rotation);
    }
    at.translate(-width / 2, -height / 2);
    return at.createTransformedShape(theRectangle);
  }

  /**
   * Returns a <code>Rectangle2D</code> whose width and height are defined by this instance's size
   * and aspect ratio functions.
   *
   * @param t the object for which the shape will be drawn
   * @return a rectangle for this input T
   */
  public Shape getRectangle(T t) {
    return getRectangle(t, 0.0);
  }

  private static final Ellipse2D theEllipse = new Ellipse2D.Float();

  /**
   * Returns a <code>Ellipse2D</code> whose width and height are defined by this instance's size and
   * aspect ratio functions.
   *
   * @param t the object for which the shape will be drawn
   * @return an ellipse for input T
   */
  public Shape getEllipse(T t) {
    theEllipse.setFrame(getRectangle(t).getBounds2D());
    return theEllipse;
  }

  private static final RoundRectangle2D theRoundRectangle = new RoundRectangle2D.Float();
  /**
   * Returns a <code>RoundRectangle2D</code> whose width and height are defined by this instance's
   * size and aspect ratio functions. The arc layoutSize is set to be half the minimum of the height
   * and width of the frame.
   *
   * @param t the object for which the shape will be drawn
   * @return an round rectangle for this input T
   */
  public RoundRectangle2D getRoundRectangle(T t) {
    Rectangle2D frame = getRectangle(t).getBounds2D();
    float arc_size = (float) Math.min(frame.getHeight(), frame.getWidth()) / 2;
    theRoundRectangle.setRoundRect(
        frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight(), arc_size, arc_size);
    return theRoundRectangle;
  }

  /**
   * Returns a regular <code>sides</code>-sided <code>Polygon</code> whose bounding box's width and
   * height are defined by this instance's size and aspect ratio functions.
   *
   * @param t the T for which the shape will be drawn
   * @param sides the number of sides of the polygon; must be &ge; 3.
   * @return a regular polygon for this t
   */
  public Shape getRegularPolygon(T t, int sides, double rotation) {
    GeneralPath thePolygon = new GeneralPath();
    assert sides >= 3 : "Number of sides must be >= 3";
    Rectangle2D frame = getRectangle(t).getBounds2D();
    float width = (float) frame.getWidth();
    float height = (float) frame.getHeight();

    // generate coordinates
    double angle = 0;
    thePolygon.reset();
    thePolygon.moveTo(0, 0);
    thePolygon.lineTo(width, 0);
    double theta = (2 * Math.PI) / sides;
    for (int i = 2; i < sides; i++) {
      angle -= theta;
      float delta_x = (float) (width * Math.cos(angle));
      float delta_y = (float) (width * Math.sin(angle));
      Point2D prev = thePolygon.getCurrentPoint();
      thePolygon.lineTo((float) prev.getX() + delta_x, (float) prev.getY() + delta_y);
    }
    thePolygon.closePath();

    // scale polygon to be right layoutSize, translate to center at (0,0)
    Rectangle2D r = thePolygon.getBounds2D();
    double scale_x = width / r.getWidth();
    double scale_y = height / r.getHeight();
    float translationX = (float) (r.getMinX() + r.getWidth() / 2);
    float translationY = (float) (r.getMinY() + r.getHeight() / 2);

    AffineTransform at = AffineTransform.getScaleInstance(scale_x, scale_y);
    if (rotation != 0) {
      at.rotate(Math.PI);
    }
    at.translate(-translationX, -translationY);

    return at.createTransformedShape(thePolygon);
  }
  /**
   * Returns a regular <code>sides</code>-sided <code>Polygon</code> whose bounding box's width and
   * height are defined by this instance's size and aspect ratio functions.
   *
   * @param t the T for which the shape will be drawn
   * @param sides the number of sides of the polygon; must be &ge; 3.
   * @return a regular polygon for this t
   */
  public Shape getRegularPolygon(T t, int sides) {
    return getRegularPolygon(t, sides, 0.0);
  }

  /**
   * Returns a regular <code>Polygon</code> of <code>points</code> points whose bounding box's width
   * and height are defined by this instance's layoutSize and aspect ratio functions.
   *
   * @param t the input T for which the shape will be drawn
   * @param points the number of points of the polygon; must be &ge; 5.
   * @return an star shape for this t
   */
  public Shape getRegularStar(T t, int points) {
    GeneralPath thePolygon = new GeneralPath();
    assert points >= 5 : "Number of points must be >= 5";
    Rectangle2D frame = getRectangle(t).getBounds2D();
    float width = (float) frame.getWidth();
    float height = (float) frame.getHeight();

    // generate coordinates
    double theta = (2 * Math.PI) / points;
    double angle = -theta / 2;
    thePolygon.reset();
    thePolygon.moveTo(0, 0);
    float deltaX = width * (float) Math.cos(angle);
    float deltaY = width * (float) Math.sin(angle);
    Point2D currentPoint = thePolygon.getCurrentPoint();
    thePolygon.lineTo((float) currentPoint.getX() + deltaX, (float) currentPoint.getY() + deltaY);
    for (int i = 1; i < points; i++) {
      angle += theta;
      deltaX = width * (float) Math.cos(angle);
      deltaY = width * (float) Math.sin(angle);
      currentPoint = thePolygon.getCurrentPoint();
      thePolygon.lineTo((float) currentPoint.getX() + deltaX, (float) currentPoint.getY() + deltaY);
      angle -= theta * 2;
      deltaX = width * (float) Math.cos(angle);
      deltaY = width * (float) Math.sin(angle);
      currentPoint = thePolygon.getCurrentPoint();
      if (currentPoint != null) {
        thePolygon.lineTo(
            (float) currentPoint.getX() + deltaX, (float) currentPoint.getY() + deltaY);
      } else {
        log.error("somehow, currentPoint is null");
      }
    }
    thePolygon.closePath();

    // scale polygon to be right layoutSize, translate to center at (0,0)
    Rectangle2D r = thePolygon.getBounds2D();
    double scaleX = width / r.getWidth();
    double scaleY = height / r.getHeight();

    float translationX = (float) (r.getMinX() + r.getWidth() / 2);
    float translationY = (float) (r.getMinY() + r.getHeight() / 2);

    AffineTransform at = AffineTransform.getScaleInstance(scaleX, scaleY);
    at.translate(-translationX, -translationY);

    return at.createTransformedShape(thePolygon);
  }
}
