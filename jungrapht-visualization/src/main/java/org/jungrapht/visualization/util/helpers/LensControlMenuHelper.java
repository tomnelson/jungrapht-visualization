package org.jungrapht.visualization.util.helpers;

import java.util.Collection;
import java.util.Map;
import javax.swing.*;
import org.jungrapht.visualization.transform.LensSupport;

public class LensControlMenuHelper {

  public static LensControlMenuHelper with(JMenu menu, Map<String, LensSupport> map) {
    return new LensControlMenuHelper(menu, map);
  }

  private JMenu menu;

  public JMenu menu(String title) {
    menu.setText(title);
    return menu;
  }

  public JMenu menu() {
    return menu;
  }

  private LensControlMenuHelper(JMenu menu, Map<String, LensSupport> map) {
    this.menu = menu;
    JMenuItem none = new JMenuItem("None");
    none.addActionListener(e -> map.values().forEach(LensSupport::deactivate));
    map.entrySet().forEach(entry -> addMenuControls(entry, map.values(), this.menu));
    menu.add(none);
  }

  private void addMenuControls(
      Map.Entry<String, LensSupport> entry, Collection<LensSupport> lenses, JComponent container) {
    JMenuItem button = new JMenuItem(entry.getKey());
    button.addActionListener(
        e -> {
          lenses.forEach(LensSupport::deactivate);
          entry.getValue().activate();
        });
    container.add(button);
  }
}
