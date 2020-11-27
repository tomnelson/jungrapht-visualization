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

import java.awt.Cursor;
import java.awt.Point;

/**
 * a base class for GraphMousePlugin instances. Holds some members common to all GraphMousePlugins
 *
 * @author Tom Nelson
 */
public abstract class AbstractGraphMousePlugin implements GraphMousePlugin {

  /** the location in the View where the mouse was pressed */
  protected Point down;

  /** the special cursor that plugins may display */
  protected Cursor cursor;

  /** @return Returns the cursor. */
  public Cursor getCursor() {
    return cursor;
  }

  /** @param cursor The cursor to set. */
  public void setCursor(Cursor cursor) {
    this.cursor = cursor;
  }
}
