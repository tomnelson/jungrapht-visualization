/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 *
 * Created on Mar 28, 2005
 */
package org.jungrapht.visualization.selection;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Maintains the state of what has been 'selected' in the graph. The <code>Sets</code> are
 * constructed so that their iterators will traverse them in the order in which they are selected.
 *
 * @author Tom Nelson
 */
public class MultiMutableSelectedState<T> extends AbstractMutableSelectedState<T>
    implements MutableSelectedStateSink<T> {
  /** the 'selected' items */
  protected Set<T> selected = new LinkedHashSet<>();

  /**
   * Select an element
   *
   * @param element the element to select
   * @return true if the collection of selected changed
   */
  @Override
  public boolean select(T element) {
    return select(element, true);
  }

  @Override
  public boolean select(T element, boolean fireEvents) {
    if (selected.add(element)) {
      if (fireEvents) {
        fireItemStateChanged(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, element, ItemEvent.SELECTED));
      }
      return true;
    }
    return false;
  }

  /**
   * if t is was already selected, remove it
   *
   * @param t item to de-select
   * @return true if the collection of selected things was modified
   */
  @Override
  public boolean deselect(T t, boolean fireEvents) {
    if (this.selected.remove(t)) {
      if (fireEvents) {
        fireItemStateChanged(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.DESELECTED));
      }
      return true; // mutated the collection
    }
    return false;
  }

  @Override
  public boolean deselect(T element) {
    return deselect(element, true);
  }

  @Override
  public boolean select(Collection<T> elements, boolean fireEvents) {

    if (selected.addAll(elements)) {
      // added and mutated the set.
      if (fireEvents) {
        fireItemStateChanged(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, elements, ItemEvent.SELECTED));
      }
      return true; // mutated the collection and fired an event
    }
    return false;
  }

  @Override
  public boolean select(Collection<T> elements) {
    return select(elements, true);
  }

  @Override
  public boolean deselect(Collection<T> elements, boolean fireEvents) {
    if (this.selected.removeAll(elements)) {
      if (fireEvents) {
        fireItemStateChanged(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, elements, ItemEvent.DESELECTED));
      }
      return true; // mutated the collection
    }
    return false;
  }

  @Override
  public boolean deselect(Collection<T> elements) {
    return deselect(elements, true);
  }

  @Override
  public void clear(boolean fireEvents) {
    Collection<T> unpicks = new ArrayList<>(selected);
    deselect(unpicks, fireEvents);
    selected.clear();
  }

  @Override
  public void clear() {
    clear(true);
  }

  @Override
  public Set<T> getSelected() {
    return Collections.unmodifiableSet(selected);
  }

  @Override
  public boolean isSelected(T t) {
    return selected.contains(t);
  }

  /** for the ItemSelectable interface contract */
  @Override
  public T[] getSelectedObjects() {
    List<T> list = new ArrayList<>(selected);
    return (T[]) list.toArray();
  }
}
