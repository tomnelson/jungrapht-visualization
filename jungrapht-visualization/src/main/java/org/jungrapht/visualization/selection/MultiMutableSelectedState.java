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

  private boolean select(T t) {
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

  private boolean deselect(T t) {
    if (this.selected.remove(t)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.DESELECTED));
      return true; // mutated the collection
    }
    return false;
  }

  /**
   * select or deselect passed t, based on state
   *
   * @param t the item to select
   * @param state true to select, false to deselect
   * @return true if the selected state was changed
   */
  @Override
  public boolean pick(T t, boolean state) {
    return state ? select(t) : deselect(t);
  }

  private boolean select(Collection<T> t) {
    if (this.selected.addAll(t)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.SELECTED));
      return true; // mutated the collection and fired an event
    }
    if (this.selected.removeAll(t)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.DESELECTED));
      return true;
    }
    return false;
  }

  private boolean deselect(Collection<T> t) {
    if (this.selected.remove(t)) {
      fireItemStateChanged(
          new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.DESELECTED));
      return true; // mutated the collection
    }
    return false;
  }

  @Override
  public boolean pick(Collection<T> t, boolean state) {
    return state ? select(t) : deselect(t);
  }

  @Override
  public void clear() {
    Collection<T> unpicks = new ArrayList<>(selected);
    pick(unpicks, false);
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
