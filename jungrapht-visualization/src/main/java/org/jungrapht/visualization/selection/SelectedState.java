/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.selection;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;
import java.util.function.Consumer;

/** @author Tom Nelson */
public interface SelectedState<T> {

  boolean isSelected(T t);

  /** @return all selected elements. */
  Set<T> getSelected();

  class StateChangeListener<T> implements ItemListener {

    private Consumer<T> selectedFunction;
    private Consumer<T> deselectedFunction;

    public StateChangeListener(Consumer<T> selectedFunction, Consumer<T> deselectedFunction) {
      this.selectedFunction = selectedFunction;
      this.deselectedFunction = deselectedFunction;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        selectedFunction.accept((T) e.getItem());
      } else if (e.getStateChange() == ItemEvent.DESELECTED) {
        deselectedFunction.accept((T) e.getItem());
      }
    }
  }
}
