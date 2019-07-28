package org.jungrapht.visualization.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;

public class IconFunction<V> implements Function<V, Icon> {

  Map<V, Icon> iconMap = new HashMap<>();
  Function<V, String> vertexLabelFunction;
  JLabel stamp = new JLabel();
  Map<RenderingHints.Key, Object> renderingHints = new HashMap<>();

  public IconFunction(Function<V, String> vertexLabelFunction) {
    this.vertexLabelFunction = vertexLabelFunction;
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  @Override
  public Icon apply(V n) {
    if (!iconMap.containsKey(n)) {
      cacheIconFor(n);
    }
    return iconMap.get(n);
  }

  public Map<V, Icon> getIconMap() {
    return iconMap;
  }

  private void cacheIconFor(V vertex) {
    stamp.setText(vertexLabelFunction.apply(vertex));
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
    iconMap.put(vertex, icon);
  }
}
