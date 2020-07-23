/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 5, 2005
 */

package org.jungrapht.visualization.transform;

import java.awt.event.ItemListener;
import org.jungrapht.visualization.control.LensGraphMouse;

/**
 * basic API for implementing lens projection support
 *
 * @author Tom Nelson
 * @parm <M> LensGraphMouse type
 */
public interface LensSupport<M extends LensGraphMouse> {

  interface Builder<M extends LensGraphMouse, B extends Builder<M, B>> {}

  void activate();

  void deactivate();

  void activate(boolean state);

  boolean isActive();

  void setManager(Runnable manager);

  LensTransformer getLensTransformer();

  M getGraphMouse();

  void addItemListener(ItemListener itemListener);
}
