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

import java.awt.*;
import java.util.Collection;

/**
 * An interface for classes that keep track of the selected state of T objects.
 *
 * @author Tom Nelson
 */
public interface MutableSelectedStateSink<T> extends MutableSelectedState<T>, ItemSelectable {

  /**
   * select one element.
   *
   * @param element the element to select
   * @param fireEvents if false, act as a sink, if true, do not fire events
   * @return true if the collection of selected elements was changed
   */
  boolean select(T element, boolean fireEvents);

  /**
   * deselect one element
   *
   * @param element the element to deselect
   * @param fireEvents act as a sink, if true, do not fire events
   * @return true is the collection of selected elements was changed
   */
  boolean deselect(T element, boolean fireEvents);

  /**
   * select a collection of elements to be the only selected elements
   *
   * @param elements
   * @param fireEvents if false, act as a sink, if true, do not fire events
   * @return true if the collection of selected elements was changed
   */
  boolean select(Collection<T> elements, boolean fireEvents);

  /**
   * deselect a collection of elements
   *
   * @param elements the elements to deselect
   * @param fireEvents if false, act as a sink, if true, do not fire events
   * @return true if the collection of selected elements was changed
   */
  boolean deselect(Collection<T> elements, boolean fireEvents);

  /**
   * Clears the "selected" state from all elements.
   *
   * @param fireEvents if false, act as a sink, if true, do not fire events
   */
  void clear(boolean fireEvents);
}
