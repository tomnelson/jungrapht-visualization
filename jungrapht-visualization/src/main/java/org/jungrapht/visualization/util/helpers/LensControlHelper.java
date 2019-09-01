package org.jungrapht.visualization.util.helpers;

import java.util.Collection;
import java.util.Map;
import javax.swing.*;
import org.jungrapht.visualization.transform.LensSupport;

public class LensControlHelper {

  public static LensControlHelper with(JComponent container, Map<String, LensSupport> map) {
    return new LensControlHelper(container, map);
  }

  private JComponent container;

  public JComponent container(String title) {
    return ControlHelpers.getCenteredContainer(title, container);
  }

  public JComponent container() {
    return container;
  }

  private LensControlHelper(JComponent container, Map<String, LensSupport> map) {
    this.container = container;
    JButton none = new JButton("None");
    none.addActionListener(e -> map.values().forEach(LensSupport::deactivate));

    int count = map.size();
    if (count < 4) {
      map.entrySet().forEach(entry -> addControls(entry, map.values(), this.container));
      container.add(none);
    } else {
      int i = 0;
      Box box = Box.createHorizontalBox();
      for (Map.Entry<String, LensSupport> entry : map.entrySet()) {
        if (i++ % 2 == 0) {
          box = Box.createHorizontalBox();
          container.add(box);
        }
        addControls(entry, map.values(), box);
      }
      box = Box.createHorizontalBox();
      container.add(box);
      box.add(none);
    }
  }

  private void addControls(
      Map.Entry<String, LensSupport> entry, Collection<LensSupport> lenses, JComponent container) {
    JButton button = new JButton(entry.getKey());
    button.addActionListener(
        e -> {
          lenses.forEach(LensSupport::deactivate);
          entry.getValue().activate();
        });
    container.add(Box.createGlue());
    container.add(button);
    container.add(Box.createGlue());
  }
}
