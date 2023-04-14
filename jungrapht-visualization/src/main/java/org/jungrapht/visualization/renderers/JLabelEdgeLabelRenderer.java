/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Apr 14, 2005
 */

package org.jungrapht.visualization.renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * JLabelEdgeLabelRenderer is similar to the cell renderers used by the JTable and JTree jfc
 * classes.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class JLabelEdgeLabelRenderer extends JLabel implements EdgeLabelRenderer, Serializable {

  protected static final Border noFocusBorder = new EmptyBorder(0, 0, 0, 0);

  protected Color pickedEdgeLabelColor = Color.black;
  protected boolean rotateEdgeLabels;

  public JLabelEdgeLabelRenderer(Color pickedEdgeLabelColor) {
    this(pickedEdgeLabelColor, true);
  }

  /**
   * Creates an instance with the specified properties.
   *
   * @param pickedEdgeLabelColor the color to use for rendering the labels of selected edges
   * @param rotateEdgeLabels whether the
   */
  public JLabelEdgeLabelRenderer(Color pickedEdgeLabelColor, boolean rotateEdgeLabels) {
    super();
    this.pickedEdgeLabelColor = pickedEdgeLabelColor;
    this.rotateEdgeLabels = rotateEdgeLabels;
    setOpaque(true);
    setBorder(noFocusBorder);
  }

  /** @return Returns the rotateEdgeLabels. */
  public boolean isRotateEdgeLabels() {
    return rotateEdgeLabels;
  }
  /** @param rotateEdgeLabels The rotateEdgeLabels to set. */
  public void setRotateEdgeLabels(boolean rotateEdgeLabels) {
    this.rotateEdgeLabels = rotateEdgeLabels;
  }
  /**
   * Overrides <code>JComponent.setForeground</code> to assign the unselected-foreground color to
   * the specified color.
   *
   * @param c set the foreground color to this value
   */
  @Override
  public void setForeground(Color c) {
    super.setForeground(c);
  }

  /**
   * Overrides <code>JComponent.setBackground</code> to assign the unselected-background color to
   * the specified color.
   *
   * @param c set the background color to this value
   */
  @Override
  public void setBackground(Color c) {
    super.setBackground(c);
  }

  /**
   * Notification from the <code>UIManager</code> that the look and feel has changed. Replaces the
   * current UI object with the latest version from the <code>UIManager</code>.
   *
   * @see JComponent#updateUI
   */
  @Override
  public void updateUI() {
    super.updateUI();
    setForeground(null);
    setBackground(null);
  }

  /**
   * Returns the default label renderer for an Edge
   *
   * @param vv the <code>VisualizationViewer</code> to render on
   * @param value the value to assign to the label for <code>Edge</code>
   * @param edge the <code>Edge</code>
   * @return the default label renderer
   */
  public <E> Component getEdgeLabelRendererComponent(
      JComponent vv, Object value, Font font, boolean isSelected, E edge) {

    super.setForeground(vv.getForeground());
    if (isSelected) {
      setForeground(pickedEdgeLabelColor);
    }
    super.setBackground(vv.getBackground());

    if (font != null) {
      setFont(font);
    } else {
      setFont(vv.getFont());
    }
    setIcon(null);
    setBorder(noFocusBorder);
    setValue(value);
    return this;
  }

  /*
   * <bold id="override">Implementation Note</bold>
   * The following methods are overridden as a performance measure to
   * prune code-paths that are often called in the case of renders
   * but which we know are unnecessary.  Great care should be taken
   * when writing your own renderer to weigh the benefits and
   * drawbacks of overriding methods like these.
   */

  /**
   * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for
   * more information.
   */
  @Override
  public boolean isOpaque() {
    Color back = getBackground();
    Component p = getParent();
    if (p != null) {
      p = p.getParent();
    }
    boolean colorMatch =
        (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
    return !colorMatch && super.isOpaque();
  }

  /**
   * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for
   * more information.
   */
  @Override
  public void validate() {}

  /**
   * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for
   * more information.
   */
  @Override
  public void revalidate() {}

  /**
   * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for
   * more information.
   */
  @Override
  public void repaint(long tm, int x, int y, int width, int height) {}

  /**
   * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for
   * more information.
   */
  @Override
  public void repaint(Rectangle r) {}

  /**
   * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for
   * more information.
   */
  @Override
  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    // Strings get interned...
    if (Objects.equals(propertyName, "text")) {
      super.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  /**
   * Overridden for performance reasons. See the <a href="#override">Implementation Note</a> for
   * more information.
   */
  @Override
  public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}

  /**
   * Sets the <code>String</code> object for the cell being rendered to <code>value</code>.
   *
   * @param value the string value for this cell; if value is <code>null</code> it sets the text
   *     value to an empty string
   * @see JLabel#setText
   */
  protected void setValue(Object value) {
    setText((value == null) ? "" : value.toString());
  }
}
