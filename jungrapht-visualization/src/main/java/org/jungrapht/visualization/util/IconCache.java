package org.jungrapht.visualization.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * a lazy-map backed {@code Function} that creates an image icon for each vertex
 *
 * @param <V> the vertex type
 */
public class IconCache<V> extends HashMap<V, Icon> implements Function<V, Icon> {

  protected Function<V, String> vertexLabelFunction;
  protected JLabel stamp = new JLabel();
  protected Map<RenderingHints.Key, Object> renderingHints = new HashMap<>();
  protected Function<V, Paint> colorFunction;

  public IconCache(Function<V, String> vertexLabelFunction) {
    this(vertexLabelFunction, n -> Color.black);
  }

  public IconCache(Function<V, String> vertexLabelFunction, Function<V, Paint> colorFunction) {
    this.vertexLabelFunction = vertexLabelFunction;
    this.colorFunction = colorFunction;
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Border border = stamp.getBorder();
    Border margin = new EmptyBorder(4, 4, 4, 4);
    stamp.setBorder(new CompoundBorder(border, margin));
  }

  @Override
  public Icon get(Object n) {
    if (!super.containsKey(n)) {
      cacheIconFor((V) n);
    }
    return super.get(n);
  }

  @Override
  public Icon apply(V in) {
    return get(in);
  }

  protected void cacheIconFor(V vertex) {
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
    graphics.setPaint(colorFunction.apply(vertex));
    graphics.setStroke(new BasicStroke(2.0f));
    graphics.drawRect(0, 0, stamp.getWidth() - 1, stamp.getHeight() - 1);
    graphics.dispose();
    Icon icon = new ImageIcon(bi);
    super.put(vertex, icon);
  }
}
