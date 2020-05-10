package org.jungrapht.samples.util;

import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;

public class LayeringConfiguration extends JPanel implements ItemSelectable {

  Layering layeringPreference;

  public LayeringConfiguration() {
    this(Layering.TOP_DOWN);
  }

  public LayeringConfiguration(Layering layeringPreference) {
    super(new GridLayout(0, 1));
    this.layeringPreference = layeringPreference;
    ButtonGroup radio = new ButtonGroup();
    JRadioButton topDownButton = new JRadioButton("Top Down");
    topDownButton.setSelected(true);
    add(topDownButton);
    radio.add(topDownButton);
    topDownButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            fireItemStateChanged(
                new ItemEvent(this, ItemEvent.SELECTED, Layering.TOP_DOWN, ItemEvent.SELECTED));
          }
        });
    JRadioButton longestPathButton = new JRadioButton("Longest Path");
    add(longestPathButton);
    radio.add(longestPathButton);
    longestPathButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            fireItemStateChanged(
                new ItemEvent(this, ItemEvent.SELECTED, Layering.LONGEST_PATH, ItemEvent.SELECTED));
          }
        });
    JRadioButton cofmanGrahamButton = new JRadioButton("Coffman Graham");
    add(cofmanGrahamButton);
    radio.add(cofmanGrahamButton);
    cofmanGrahamButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            fireItemStateChanged(
                new ItemEvent(
                    this, ItemEvent.SELECTED, Layering.COFFMAN_GRAHAM, ItemEvent.SELECTED));
          }
        });
    JRadioButton networkSimlexButton = new JRadioButton("Network Simplex");
    add(networkSimlexButton);
    radio.add(networkSimlexButton);
    networkSimlexButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            fireItemStateChanged(
                new ItemEvent(
                    this, ItemEvent.SELECTED, Layering.NETWORK_SIMPLEX, ItemEvent.SELECTED));
          }
        });
  }

  public Layering getLayeringPreference() {
    return this.layeringPreference;
  }

  @Override
  public Object[] getSelectedObjects() {
    return new Object[0];
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
    this.layeringPreference = (Layering) e.getItem();
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ItemListener.class) {
        ((ItemListener) listeners[i + 1]).itemStateChanged(e);
      }
    }
  }
}
