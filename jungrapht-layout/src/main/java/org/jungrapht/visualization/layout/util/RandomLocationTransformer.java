/*
 * Created on Jul 19, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.layout.util;

import static org.jungrapht.visualization.layout.util.RandomLocationTransformer.Origin.NE;

import java.util.Date;
import java.util.Random;
import java.util.function.Function;
import org.jungrapht.visualization.layout.model.Point;

/**
 * Provides a random vertex location within the bounds of the width and height. This provides a
 * random location for unmapped vertices the first time they are accessed.
 *
 * <p><b>Note</b>: the generated values are not cached, so animate() will generate a new random
 * location for the passed vertex every time it is called. If you want a consistent value, wrap this
 * // * layout's generated values in a instance.
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 */
public class RandomLocationTransformer<V> implements Function<V, Point> {
  protected double width;
  protected double height;
  protected Random random;
  protected Origin origin;

  public enum Origin {
    NE,
    CENTER
  }

  /**
   * Creates an instance with the specified layoutSize which uses the current time as the random
   * seed.
   *
   * @param width, height the layoutSize of the layout area
   */
  public RandomLocationTransformer(double width, double height) {
    this(NE, width, height, new Date().getTime());
  }

  /**
   * Creates an instance with the specified layoutSize which uses the current time as the random
   * seed.
   *
   * @param width, height the layoutSize of the layout area
   */
  public RandomLocationTransformer(Origin origin, double width, double height) {
    this(origin, width, height, new Date().getTime());
  }

  /**
   * Creates an instance with the specified dimension and random seed.
   *
   * @param
   * @param seed the seed for the internal random number generator
   */
  public RandomLocationTransformer(double width, double height, long seed) {
    this(NE, width, height, seed);
  }

  /**
   * Creates an instance with the specified dimension and random seed.
   *
   * @param
   * @param seed the seed for the internal random number generator
   */
  public RandomLocationTransformer(Origin origin, double width, double height, long seed) {
    this.origin = origin;
    this.width = width;
    this.height = height;
    this.random = new Random(seed);
  }

  private Point applyNE(V vertex) {
    return Point.of(random.nextDouble() * width, random.nextDouble() * height);
  }

  private Point applyCenter(V vertex) {
    double radiusX = width / 2;
    double radiusY = height / 2;
    return Point.of(random.nextDouble() * width - radiusX, random.nextDouble() * height - radiusY);
  }

  @Override
  public Point apply(V vertex) {
    if (this.origin == NE) {
      return applyNE(vertex);
    } else {
      return applyCenter(vertex);
    }
  }
}
