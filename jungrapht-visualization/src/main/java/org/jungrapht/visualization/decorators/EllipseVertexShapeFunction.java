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

/** @author Joshua O'Madadhain */
public class EllipseVertexShapeFunction<V> extends AbstractVertexShapeFunction<V>
    implements Function<V, Shape> {
  public EllipseVertexShapeFunction() {}

  public EllipseVertexShapeFunction(Function<V, Integer> vsf, Function<V, Float> varf) {
    super(vsf, varf);
  }

  public Shape apply(V v) {
    return factory.getEllipse(v);
  }
}
