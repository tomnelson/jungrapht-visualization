package org.jungrapht.samples.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import org.jungrapht.visualization.VisualizationViewer;

public class TitlePaintable implements VisualizationViewer.Paintable {
  int x;
  int y;
  Font font;
  FontMetrics metrics;
  int swidth;
  int sheight;
  String title;
  Dimension overallSize;

  public TitlePaintable(String title, Dimension overallSize) {
    this.title = title;
    this.overallSize = overallSize;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void paint(Graphics g) {
    Dimension d = overallSize;
    if (font == null) {
      font = new Font(g.getFont().getName(), Font.BOLD, 30);
      metrics = g.getFontMetrics(font);
      swidth = metrics.stringWidth(title);
      sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
      x = (d.width - swidth) / 2;
      y = (int) (d.height - sheight * 1.5);
    }
    g.setFont(font);
    Color oldColor = g.getColor();
    g.setColor(Color.lightGray);

    x = overallSize.width / 4;
    y = 2 * overallSize.height / 3;
    drawString(g, title, x, y);
    g.setColor(oldColor);
  }

  void drawString(Graphics g, String text, int x, int y) {
    int lineHeight = g.getFontMetrics().getHeight();
    for (String line : text.split("\n")) g.drawString(line, x, y += lineHeight);
  }

  public boolean useTransform() {
    return false;
  }
}
