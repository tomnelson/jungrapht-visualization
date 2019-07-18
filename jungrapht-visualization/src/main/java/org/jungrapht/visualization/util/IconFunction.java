package org.jungrapht.visualization.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;

public class IconFunction<N> implements Function<N, Icon> {

  Map<N, Icon> iconMap = new HashMap<>();
  Function<N, String> nodeLabelFunction;
  JLabel stamp = new JLabel();
  Map<RenderingHints.Key, Object> renderingHints = new HashMap<>();

  public IconFunction(Function<N, String> nodeLabelFunction) {
    this.nodeLabelFunction = nodeLabelFunction;
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  @Override
  public Icon apply(N n) {
    if (!iconMap.containsKey(n)) {
      cacheIconFor(n);
    }
    return iconMap.get(n);
  }

  public Map<N, Icon> getIconMap() {
    return iconMap;
  }

  private void cacheIconFor(N node) {
    stamp.setText(nodeLabelFunction.apply(node));
    stamp.setForeground(Color.black);
    stamp.setBackground(Color.white);
    stamp.setOpaque(true);
    stamp.setSize(stamp.getPreferredSize());
    stamp.addNotify();
    BufferedImage bi =
        new BufferedImage(stamp.getWidth(), stamp.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = bi.createGraphics();
    graphics.setRenderingHints(renderingHints);
    stamp.paint(graphics);
    graphics.setPaint(Color.black);
    graphics.drawRect(0, 0, stamp.getWidth() - 1, stamp.getHeight() - 1);
    graphics.dispose();
    Icon icon = new ImageIcon(bi);
    iconMap.put(node, icon);
  }
}
