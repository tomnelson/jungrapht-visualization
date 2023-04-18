/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Apr 16, 2005
 */

package org.jungrapht.visualization.transform.shape;

import java.awt.Shape;

/**
 * Provides methods to map points from one coordinate system to another: graph to screen and screen
 * to graph. The flatness parameter is used to break a curved shape into smaller segments in order
 * to perform a more detailed transformation.
 *
 * @author Tom Nelson
 */
public interface ShapeFlatnessTransformer extends ShapeTransformer {

  /**
   * map a shape from graph coordinate system to the screen coordinate system
   *
   * @param shape the shape to be transformed
   * @param flatness used to break the supplied shape into segments
   * @return a Path2D(Shape) representing the screen points of the shape
   */
  Shape transform(Shape shape, double flatness);
}
