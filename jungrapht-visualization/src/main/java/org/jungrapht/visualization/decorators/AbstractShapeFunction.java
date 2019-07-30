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

import java.util.function.Function;
import org.jungrapht.visualization.util.ShapeFactory;

public abstract class AbstractShapeFunction<T> implements SettableShapeFunction<T> {
  protected Function<T, Integer> sizeFunction;
  protected Function<T, Float> aspectRatioFunction;
  protected ShapeFactory<T> factory;
  public static final int DEFAULT_SIZE = 8;
  public static final float DEFAULT_ASPECT_RATIO = 1.0f;

  public AbstractShapeFunction(
      Function<T, Integer> sizeFunction, Function<T, Float> aspectRatioFunction) {
    this.sizeFunction = sizeFunction;
    this.aspectRatioFunction = aspectRatioFunction;
    factory = new ShapeFactory<>(sizeFunction, aspectRatioFunction);
  }

  public AbstractShapeFunction() {
    this(n -> DEFAULT_SIZE, n -> DEFAULT_ASPECT_RATIO);
  }

  @Override
  public void setSizeFunction(Function<T, Integer> sizeFunction) {
    this.sizeFunction = sizeFunction;
    factory = new ShapeFactory<>(sizeFunction, aspectRatioFunction);
  }

  @Override
  public void setAspectRatioFunction(Function<T, Float> aspectRatioFunction) {
    this.aspectRatioFunction = aspectRatioFunction;
    factory = new ShapeFactory<>(sizeFunction, aspectRatioFunction);
  }
}
