package org.jungrapht.visualization.util;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * a lazy-map backed {@code Function} that creates an image icon for each vertex
 *
 * @param <V> the vertex type
 */
public class IconCache<V> extends HashMap<V, Icon> implements Function<V, Icon> {

  /**
   * Builder for IconCache
   *
   * @param <V> vertex type
   */
  public static class Builder<V> {
    private Function<V, String> vertexLabelFunction;
    private Function<V, Shape> vertexShapeFunction = v -> new Ellipse2D.Double(-5, -5, 10, 10);
    private Function<V, Paint> paintFunction = v -> Color.black;
    private JLabel label = new JLabel();
    /** default simple renderer for vertex labels. */
    private Stylist<V> stylist =
        (label, vertex, colorFunction) -> {
          label.setFont(new Font("Serif", Font.BOLD, 20));
          label.setForeground(Color.black);
          label.setBackground(Color.white);
          label.setOpaque(true);
          Border lineBorder =
              BorderFactory.createLineBorder((Color) colorFunction.apply(vertex), 3);
          Border marginBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
          label.setBorder(new CompoundBorder(lineBorder, marginBorder));
        };

    private Decorator<V> decorator =
        (graphics, vertex, bounds, vertexShapeFunction, colorFunction) -> {};

    public Builder(Function<V, String> vertexLabelFunction) {
      this.vertexLabelFunction = vertexLabelFunction;
    }

    public Builder<V> vertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
      this.vertexShapeFunction = vertexShapeFunction;
      return this;
    }

    public Builder<V> label(JLabel label) {
      this.label = label;
      return this;
    }

    public Builder<V> colorFunction(Function<V, Paint> paintFunction) {
      this.paintFunction = paintFunction;
      return this;
    }

    public Builder<V> stylist(Stylist<V> stylist) {
      this.stylist = stylist;
      return this;
    }

    public Builder<V> decorator(Decorator<V> decorator) {
      this.decorator = decorator;
      return this;
    }

    public IconCache<V> build() {
      return new IconCache<>(this);
    }
  }

  /**
   * @param vertexLabelFunction
   * @param <V> vertex type
   * @return the Builder
   */
  public static <V> Builder<V> builder(Function<V, String> vertexLabelFunction) {
    return new Builder<>(vertexLabelFunction);
  }

  protected Function<V, String> vertexLabelFunction;
  protected Function<V, Shape> vertexShapeFunction;
  protected JLabel label;
  protected Map<RenderingHints.Key, Object> renderingHints = new HashMap<>();
  protected Function<V, Paint> colorFunction;
  protected Stylist<V> stylist;
  protected Decorator<V> decorator;

  /**
   * 
   * @param builder
   */
  protected IconCache(Builder<V> builder) {
    this(
        builder.label,
        builder.vertexLabelFunction,
        builder.vertexShapeFunction,
        builder.paintFunction,
        builder.stylist,
        builder.decorator);
  }

  private IconCache(
      JLabel label,
      Function<V, String> vertexLabelFunction,
      Function<V, Shape> vertexShapeFunction,
      Function<V, Paint> colorFunction,
      Stylist<V> stylist,
      Decorator<V> decorator) {
    this.label = label;
    this.vertexLabelFunction = vertexLabelFunction;
    this.vertexShapeFunction = vertexShapeFunction;
    this.colorFunction = colorFunction;
    this.stylist = stylist;
    this.decorator = decorator;
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  @Override
  public Icon get(Object n) {
    if (!super.containsKey(n)) {
      cacheIconFor(label, (V) n, vertexLabelFunction, colorFunction);
    }
    return super.get(n);
  }

  @Override
  public Icon apply(V in) {
    return get(in);
  }

  /**
   * allows for functional interface method
   *
   * @param <V> vertex type
   */
  public interface Stylist<V> {
    void style(JLabel label, V vertex, Function<V, Paint> colorFunction);
  }

  public interface Decorator<V> {
    void decorate(
        Graphics2D graphics,
        V vertex,
        Dimension labelBounds,
        Function<V, Shape> vertexShapeFunction,
        Function<V, Paint> colorFunction);
  }

  protected void cacheIconFor(
      JLabel label,
      V vertex,
      Function<V, String> vertexLabelFunction,
      Function<V, Paint> colorFunction) {
    label.setText(vertexLabelFunction.apply(vertex));
    stylist.style(label, vertex, colorFunction);
    Dimension size = label.getPreferredSize();
    label.setSize(size);

    BufferedImage bufferedImage =
        new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);

    // draw the JLabel onto the Image
    Graphics2D graphics = bufferedImage.createGraphics();
    graphics.setRenderingHints(renderingHints);
    label.paint(graphics);

    decorator.decorate(graphics, vertex, label.getSize(), vertexShapeFunction, colorFunction);
    // clean up and return the Icon
    graphics.dispose();
    super.put(vertex, new ImageIcon(bufferedImage));
  }
}
