package org.jungrapht.visualization.control;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.function.Function;
import javax.swing.*;
import org.jungrapht.visualization.LayeredIcon;
import org.jungrapht.visualization.renderers.Checkmark;

public class SelectionIconListener<V> implements ItemListener {
  Function<V, Icon> imager;
  Icon checked;

  public SelectionIconListener(Function<V, Icon> imager) {
    this.imager = imager;
    checked = new Checkmark(Color.red);
  }

  public void itemStateChanged(ItemEvent e) {
    if (e.getItem() instanceof Collection) {
      ((Collection<V>) e.getItem()).forEach(n -> updatePickIcon(n, e.getStateChange()));
    } else {
      updatePickIcon((V) e.getItem(), e.getStateChange());
    }
  }

  private void updatePickIcon(V n, int stateChange) {
    Icon icon = imager.apply(n);
    if (icon instanceof LayeredIcon layered) {
      if (stateChange == ItemEvent.SELECTED) {
        layered.add(checked);
      } else {
        layered.remove(checked);
      }
    }
  }
}
