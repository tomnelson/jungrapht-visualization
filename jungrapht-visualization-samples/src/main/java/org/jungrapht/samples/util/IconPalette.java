package org.jungrapht.samples.util;

import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import javax.swing.*;
import org.jungrapht.visualization.LayeredIcon;
import org.jungrapht.visualization.control.dnd.VertexImageDragGestureListener;

public class IconPalette extends JPanel {

  public IconPalette() {
    setLayout(new GridLayout(4, 3));
    String[] iconNames = {
      "apple",
      "os",
      "x",
      "linux",
      "inputdevices",
      "wireless",
      "graphics3",
      "gamespcgames",
      "humor",
      "music",
      "privacy"
    };

    var listener = new VertexImageDragGestureListener();
    for (int i = 0; i < iconNames.length; i++) {
      String name = "/images/topic" + iconNames[i] + ".gif";
      try {
        Icon icon = new LayeredIcon(new ImageIcon(IconPalette.class.getResource(name)).getImage());
        var label = new JLabel(icon);
        DragSource ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_COPY, listener);
        label.setTransferHandler(new TransferHandler("icon"));
        add(label);
      } catch (Exception ex) {
        System.err.println("You need slashdoticons.jar in your classpath to see the image " + name);
      }
    }
  }
}
