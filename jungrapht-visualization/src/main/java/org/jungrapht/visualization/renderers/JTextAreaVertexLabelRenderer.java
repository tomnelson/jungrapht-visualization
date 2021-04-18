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
import java.io.Serializable;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * JLabelVertexLabelRenderer is similar to the cell renderers used by the JTable and JTree JFC
 * classes.
 *
 * @author Tom Nelson
 */
public class JTextAreaVertexLabelRenderer extends JTextArea
    implements VertexLabelRenderer, Serializable {

  protected static Border noFocusBorder = new EmptyBorder(0, 0, 0, 0);

  protected Color pickedVertexLabelColor = Color.black;

  /**
   * Creates a default table cell renderer.
   *
   * @param pickedVertexLabelColor the color to use for rendering the labels of selected vertices
   */
  public JTextAreaVertexLabelRenderer(Color pickedVertexLabelColor) {
    this.pickedVertexLabelColor = pickedVertexLabelColor;
    setEditable(false);
    setOpaque(true);
    setBorder(noFocusBorder);
    setLineWrap(true);
    setWrapStyleWord(true);
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
   * Returns the default label renderer for a Vertex
   *
   * @param vv the <code>VisualizationViewer</code> to render on
   * @param value the value to assign to the label for <code>Vertex</code>
   * @param vertex the <code>Vertex</code>
   * @return the default label renderer
   */
  public <V> Component getVertexLabelRendererComponent(
      JComponent vv, Object value, Font font, boolean isSelected, V vertex) {

    super.setForeground(vv.getForeground());
    if (isSelected) {
      setForeground(pickedVertexLabelColor);
    }
    super.setBackground(vv.getBackground());
    if (font != null) {
      setFont(font);
    } else {
      setFont(vv.getFont());
    }
    //    setIcon(null);
    setBorder(noFocusBorder);
    setValue(value);
    return this;
  }

  /*
   * The following methods are overridden as a performance measure to
   * to prune code-paths are often called in the case of renders
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
