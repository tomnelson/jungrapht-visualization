package org.jungrapht.visualization.util;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.EventListenerList;

public abstract class ItemSupport<T> implements ItemSelectable {

  protected EventListenerList listenerList = new EventListenerList();

  protected boolean selected;

  /**
   * Returns the selected items or {@code null} if no items are selected.
   *
   * @return the list with the class name of what is selected
   */
  public Object[] getSelectedObjects() {
    if (!selected) {
      return null;
    }
    Object[] selectedObjects = new Object[1];
    selectedObjects[0] = getClass().getSimpleName();
    return selectedObjects;
  }

  public void addItemListener(ItemListener l) {
    listenerList.add(ItemListener.class, l);
  }

  public void removeItemListener(ItemListener l) {
    listenerList.remove(ItemListener.class, l);
  }

  protected void fireItemStateChanged(ItemEvent e) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ItemListener.class) {
        ((ItemListener) listeners[i + 1]).itemStateChanged(e);
      }
    }
  }
}
