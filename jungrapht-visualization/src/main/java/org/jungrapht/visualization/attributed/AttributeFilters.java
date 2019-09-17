package org.jungrapht.visualization.attributed;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.EventListenerList;

public class AttributeFilters<K, V, T extends Attributed<K, V>, B extends AbstractButton>
    implements ItemSelectable {

  public static class Builder<K, V, T extends Attributed<K, V>, B extends AbstractButton> {
    private Collection<K> losers = Collections.emptyList();
    private Set<T> elements;
    private Supplier<B> buttonSupplier = () -> (B) new JRadioButton();

    public Builder losers(Collection<K> losers) {
      this.losers = losers;
      return this;
    }

    public Builder elements(Set<T> elements) {
      this.elements = elements;
      return this;
    }

    public Builder buttonSupplier(Supplier<B> buttonSupplier) {
      this.buttonSupplier = buttonSupplier;
      return this;
    }

    public AttributeFilters<K, V, T, B> build() {
      return new AttributeFilters<>(this);
    }
  }

  private AttributeFilters(Builder builder) {
    this(builder.losers, builder.elements, builder.buttonSupplier);
  }

  List<B> radioButtons = new ArrayList<>();
  Multiset<V> multiset = HashMultiset.create();

  Set<String> selectedTexts = new HashSet<>();

  protected EventListenerList listenerList = new EventListenerList();

  private AttributeFilters(Collection<String> losers, Set<T> elements, Supplier<B> buttonSupplier) {

    // count up the unique attribute values (skipping the 'losers' we know we don't want)
    elements.forEach(
        element -> {
          Map<K, V> attributeMap = new HashMap<>(element.getAttributeMap());
          for (Map.Entry<K, V> entry : attributeMap.entrySet()) {
            if (!losers.contains(entry.getKey())) {
              multiset.add(entry.getValue());
            }
          }
        });
    // accept the values with cardinality above the max of 2 and 5% of the number elements.
    multiset.removeIf(s -> multiset.count(s) < Math.max(2, elements.size() * .05));

    // create a radio button for every element that was retained
    multiset.elementSet();
    for (V key : multiset.elementSet()) {
      B button = buttonSupplier.get();
      button.setText(key.toString());
      button.addItemListener(
          item -> {
            if (item.getStateChange() == ItemEvent.SELECTED) {
              selectedTexts.add(button.getText());
              fireItemStateChanged(
                  new ItemEvent(
                      this, ItemEvent.ITEM_STATE_CHANGED, this.selectedTexts, ItemEvent.SELECTED));

            } else if (item.getStateChange() == ItemEvent.DESELECTED) {
              selectedTexts.remove(button.getText());
              fireItemStateChanged(
                  new ItemEvent(
                      this,
                      ItemEvent.ITEM_STATE_CHANGED,
                      this.selectedTexts,
                      ItemEvent.DESELECTED));
            }
          });
      radioButtons.add(button);
    }
  }

  public List<B> getRadioButtons() {
    return radioButtons;
  }

  // event support:
  @Override
  public Object[] getSelectedObjects() {
    return selectedTexts.toArray();
  }

  @Override
  public void addItemListener(ItemListener l) {
    listenerList.add(ItemListener.class, l);
  }

  @Override
  public void removeItemListener(ItemListener l) {
    listenerList.remove(ItemListener.class, l);
  }

  protected void fireItemStateChanged(ItemEvent e) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ItemListener.class) {
        ((ItemListener) listeners[i + 1]).itemStateChanged(e);
      }
    }
  }
}
