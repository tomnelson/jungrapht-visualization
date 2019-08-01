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
    implements MutableSelectedState<T> {
  /** the 'selected' items */
  protected Set<T> selected = new LinkedHashSet<>();

  /**
   * Make element the only selected item, if it is not already selected if element is already
   * selected, clear all the selections (including element) if element is not already selected, make
   * element the only item that is selected
   *
   * @param element
   * @return
   */
  @Override
  public boolean select(T element) {
    // if element is already selected
    if (selected.contains(element)) {
      // this is a deselect
      clear();
      return true;
    }
    clear();
    this.selected.add(element);
    // added and mutated the set.
    fireItemStateChanged(
        new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, element, ItemEvent.SELECTED));
    return true; // mutated the collection and fired an event
  }

  /**
   * if t is was already selected, remove it
   *
   * @param t
   * @return
   */
  @Override
  public boolean deselect(T t) {
    if (this.selected.remove(t)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.DESELECTED));
      return true; // mutated the collection
    }
    return false;
  }

  @Override
  public boolean add(T t) {
    if (this.selected.add(t)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.SELECTED));
      return true; // mutated the collection and fired an event
    }
    if (this.selected.remove(t)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.DESELECTED));
      return true;
    }
    return false;
  }

  @Override
  public boolean remove(T element) {
    return false;
  }

  @Override
  public boolean select(Collection<T> elements) {

    // if element is already selected
    if (selected.containsAll(elements)) {
      // this is a deselect
      clear();
      return true;
    }
    this.selected.addAll(elements);
    // added and mutated the set.
    fireItemStateChanged(
        new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, elements, ItemEvent.SELECTED));
    return true; // mutated the collection and fired an event
  }

  @Override
  public boolean deselect(Collection<T> elements) {
    if (this.selected.removeAll(elements)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, elements, ItemEvent.DESELECTED));
      return true; // mutated the collection
    }
    return false;
  }

  @Override
  public boolean add(Collection<T> elements) {
    if (this.selected.addAll(elements)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, elements, ItemEvent.SELECTED));
      return true; // mutated the collection and fired an event
    }
    if (this.selected.removeAll(elements)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, elements, ItemEvent.DESELECTED));
      return true;
    }
    return false;
  }

  @Override
  public boolean remove(Collection<T> elements) {
    if (this.selected.remove(elements)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, elements, ItemEvent.DESELECTED));
      return true; // mutated the collection
    }
    return false;
  }

  @Override
  public void clear() {
    Collection<T> unpicks = new ArrayList<>(selected);
    deselect(unpicks);
    selected.clear();
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
  @SuppressWarnings("unchecked")
  @Override
  public T[] getSelectedObjects() {
    List<T> list = new ArrayList<>(selected);
    return (T[]) list.toArray();
  }
}
