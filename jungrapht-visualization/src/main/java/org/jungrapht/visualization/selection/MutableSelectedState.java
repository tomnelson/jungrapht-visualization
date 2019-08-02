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
 * An interface for classes that keep track of the selected state of T objects.
 *
 * @author Tom Nelson
 */
public interface MutableSelectedState<T> extends SelectedState<T>, ItemSelectable {

  /**
   * select one element.
   *
   * @param element the element to select
   * @return true if the collection of selected elements was changed
   */
  boolean select(T element);

  /**
   * deselect one element
   *
   * @param element the element to deselect
   * @return true is the collection of selected elements was changed
   */
  boolean deselect(T element);

  /**
   * select a collection of elements to be the only selected elements
   *
   * @param elements
   * @return true if the collection of selected elements was changed
   */
  boolean select(Collection<T> elements);

  /**
   * deselect a collection of elements
   *
   * @param elements the elements to deselect
   * @return true if the collection of selected elements was changed
   */
  boolean deselect(Collection<T> elements);

  /** Clears the "selected" state from all elements. */
  void clear();
}
