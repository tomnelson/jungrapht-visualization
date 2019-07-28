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
import org.jungrapht.visualization.util.VertexShapeFactory;

/** @author Joshua O'Madadhain */
public abstract class AbstractVertexShapeFunction<V> implements SettableVertexShapeFunction<V> {
  protected Function<V, Integer> vsf;
  protected Function<V, Float> varf;
  protected VertexShapeFactory<V> factory;
  public static final int DEFAULT_SIZE = 8;
  public static final float DEFAULT_ASPECT_RATIO = 1.0f;

  public AbstractVertexShapeFunction(Function<V, Integer> vsf, Function<V, Float> varf) {
    this.vsf = vsf;
    this.varf = varf;
    factory = new VertexShapeFactory<>(vsf, varf);
  }

  public AbstractVertexShapeFunction() {
    this(n -> DEFAULT_SIZE, n -> DEFAULT_ASPECT_RATIO);
  }

  public void setSizeTransformer(Function<V, Integer> vsf) {
    this.vsf = vsf;
    factory = new VertexShapeFactory<>(vsf, varf);
  }

  public void setAspectRatioTransformer(Function<V, Float> varf) {
    this.varf = varf;
    factory = new VertexShapeFactory<>(vsf, varf);
  }
}
