/*
 * Created on Jul 16, 2004
 *
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.decorators;

import java.awt.Shape;
import java.util.function.Function;

public class EllipseShapeFunction<T> extends AbstractShapeFunction<T>
    implements Function<T, Shape> {

  public EllipseShapeFunction() {}

  public EllipseShapeFunction(
      Function<T, Integer> sizeFunction, Function<T, Float> aspectRatioFunction) {
    super(sizeFunction, aspectRatioFunction);
  }

  @Override
  public Shape apply(T t) {
    return factory.getEllipse(t);
  }
}
