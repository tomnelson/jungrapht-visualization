/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 5, 2005
 */

package org.jungrapht.visualization.transform;

import org.jungrapht.visualization.control.ModalGraphMouse;

/**
 * basic API for implementing lens projection support
 *
 * @author Tom Nelson
 */
public interface LensSupport {

  void activate();

  void deactivate();

  void activate(boolean state);

  LensTransformer getLensTransformer();

  ModalGraphMouse getGraphMouse();
}
