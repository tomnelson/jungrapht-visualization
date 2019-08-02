package org.jungrapht.visualization;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.*;

public interface VisualizationComponent {

  JComponent getComponent();

  /** @return the rectangular bounds */
  Rectangle getBounds();

  /** @return the background color */
  Color getBackground();

  /** @param backgroundColor the color to set */
  void setBackground(Color backgroundColor);

  /** @param foregroundColor the color to set */
  void setForeground(Color foregroundColor);
  /** @param cursor the cursor to set */
  void setCursor(Cursor cursor);

  int getWidth();

  int getHeight();

  Dimension getPreferredSize();

  void setPreferredSize(Dimension preferredSize);

  Dimension getSize();

  String getToolTipText();

  void setLayout(LayoutManager layout);

  void setFont(Font font);

  Font getFont();

  Component add(Component component);

  void add(Component component, Object layout);
}
