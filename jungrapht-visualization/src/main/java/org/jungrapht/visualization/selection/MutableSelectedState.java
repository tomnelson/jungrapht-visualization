/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 *
 * Created on Apr 2, 2005
 */
package org.jungrapht.visualization.selection;

import java.awt.ItemSelectable;

/**
 * An interface for classes that keep track of the "picked" state of edges or vertices.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public interface MutableSelectedState<T> extends SelectedState<T>, ItemSelectable {
  /**
   * Marks <code>element</code> as "picked" if <code>b == true</code>, and unmarks <code>v</code> as
   * picked if <code>b == false</code>.
   *
   * @param element the element to be picked/unpicked
   * @param state true if {@code v} is to be marked as picked, false if to be marked as unpicked
   * @return the "picked" state of <code>element</code> prior to this call
   */
  boolean pick(T element, boolean state);

  /** Clears the "picked" state from all elements. */
  void clear();
}
