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
import java.util.Collection;

/**
 * An interface for classes that keep track of the "selected" state of edges or vertices.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public interface MutableSelectedState<T> extends SelectedState<T>, ItemSelectable {
  /**
   * Marks <code>element</code> as "selected" if <code>b == true</code>, and unmarks <code>v</code>
   * as selected if <code>b == false</code>.
   *
   * @param element the element to be selected/unpicked
   * @param state true if {@code v} is to be marked as selected, false if to be marked as unpicked
   * @return the "selected" state of <code>element</code> prior to this call
   */
  boolean pick(T element, boolean state);

  /**
   * Marks <code>element</code> as "selected" if <code>b == true</code>, and unmarks <code>v</code>
   * as selected if <code>b == false</code>.
   *
   * @param element the element to be selected/unpicked
   * @param state true if {@code v} is to be marked as selected, false if to be marked as unpicked
   * @return the "selected" state of <code>element</code> prior to this call
   */
  boolean pick(Collection<T> element, boolean state);

  /** Clears the "selected" state from all elements. */
  void clear();
}
