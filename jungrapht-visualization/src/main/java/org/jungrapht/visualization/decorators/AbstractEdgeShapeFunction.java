/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on March 10, 2005
 */
package org.jungrapht.visualization.decorators;

import java.awt.Shape;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.util.Context;

/**
 * An interface for decorators that return a <code>Shape</code> for a specified edge.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class AbstractEdgeShapeFunction<V, E>
    implements Function<Context<Graph<V, E>, E>, Shape> {

  /** Specifies how far apart to place the control points for edges being drawn in parallel. */
  protected float controlOffsetIncrement = 20.f;

  /** Sets the value of <code>control_offset_increment</code>. */
  public void setControlOffsetIncrement(float y) {
    controlOffsetIncrement = y;
  }
}
