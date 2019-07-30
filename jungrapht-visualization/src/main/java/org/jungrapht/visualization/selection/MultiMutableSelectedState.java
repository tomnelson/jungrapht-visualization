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
 * Maintains the state of what has been 'picked' in the graph. The <code>Sets</code> are constructed
 * so that their iterators will traverse them in the order in which they are picked.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class MultiMutableSelectedState<T> extends AbstractMutableSelectedState<T>
    implements MutableSelectedState<T> {
  /** the 'selected' items */
  protected Set<T> picked = new LinkedHashSet<>();

  @Override
  public boolean pick(T t, boolean state) {
    boolean priorState = this.picked.contains(t);
    if (state) {
      picked.add(t);
      if (!priorState) {
        fireItemStateChanged(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.SELECTED));
      }

    } else {
      picked.remove(t);
      if (priorState) {
        fireItemStateChanged(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.DESELECTED));
      }
    }
    return priorState;
  }

  @Override
  public boolean pick(Collection<T> t, boolean state) {
    boolean priorState = this.picked.containsAll(t);
    if (state) {
      picked.addAll(t);
      if (!priorState) {
        fireItemStateChanged(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.SELECTED));
      }

    } else {
      picked.remove(t);
      if (priorState) {
        fireItemStateChanged(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, t, ItemEvent.DESELECTED));
      }
    }
    return priorState;
  }

  @Override
  public void clear() {
    Collection<T> unpicks = new ArrayList<>(picked);
    pick(unpicks, false);
    picked.clear();
  }

  @Override
  public Set<T> getSelected() {
    return Collections.unmodifiableSet(picked);
  }

  @Override
  public boolean isSelected(T t) {
    return picked.contains(t);
  }

  /** for the ItemSelectable interface contract */
  @SuppressWarnings("unchecked")
  @Override
  public T[] getSelectedObjects() {
    List<T> list = new ArrayList<>(picked);
    return (T[]) list.toArray();
  }
}
