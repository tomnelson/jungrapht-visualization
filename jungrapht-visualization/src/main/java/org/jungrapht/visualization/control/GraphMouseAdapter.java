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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Simple extension of MouseAdapter that supplies modifier checking
 *
 * @author Tom Nelson
 */
public class GraphMouseAdapter extends MouseAdapter {

  protected int modifiers;

  public GraphMouseAdapter(int modifiers) {
    this.modifiers = modifiers;
  }

  public int getModifiersEx() {
    return modifiers;
  }

  public void setModifiers(int modifiers) {
    this.modifiers = modifiers;
  }

  protected boolean checkModifiers(MouseEvent e) {
    return e.getModifiersEx() == modifiers;
  }
}
