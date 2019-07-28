/*
 * Created on Jul 18, 2004
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

/** @author Joshua O'Madadhain */
public interface SettableVertexShapeFunction<V> extends Function<V, Shape> {
  void setSizeTransformer(Function<V, Integer> vsf);

  void setAspectRatioTransformer(Function<V, Float> varf);
}
