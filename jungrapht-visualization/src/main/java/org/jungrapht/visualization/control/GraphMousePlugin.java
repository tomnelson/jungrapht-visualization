/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jul 6, 2005
 */

package org.jungrapht.visualization.control;

import java.awt.event.MouseEvent;

/**
 * the interface for all plugins to the PluggableGraphMouse
 *
 * @author Tom Nelson
 */
public interface GraphMousePlugin {

  /** @return the mouse event modifiers that will activate this plugin */
  default int getModifiersEx() {
    return 0;
  }

  /** @param modifiers the mouse event modifiers that will activate this plugin */
  default void setModifiers(int modifiers) {}

  /**
   * compare the set modifiers against those of the supplied event
   *
   * @param e an event to compare to
   * @return whether the member modifiers match the event modifiers
   */
  default boolean checkModifiers(MouseEvent e) {
    return false;
  }
}
