/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.control;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPopupGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseListener {

  private static Logger log = LoggerFactory.getLogger(AbstractGraphMousePlugin.class);

  protected int modifiers;

  public AbstractPopupGraphMousePlugin() {
    this(MouseEvent.BUTTON3_DOWN_MASK);
  }

  public AbstractPopupGraphMousePlugin(int modifiers) {
    this.modifiers = modifiers;
  }

  public void mousePressed(MouseEvent e) {
    log.trace("mousePressed in {}", this.getClass().getName());
    if (e.isPopupTrigger()) {
      handlePopup(e);
      e.consume();
    }
  }

  /** if this is the popup trigger, process here, otherwise defer to the superclass */
  public void mouseReleased(MouseEvent e) {
    log.trace("mouseReleased in {}", this.getClass().getName());
    if (e.isPopupTrigger()) {
      handlePopup(e);
      e.consume();
    }
  }

  protected abstract void handlePopup(MouseEvent e);

  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}
}
