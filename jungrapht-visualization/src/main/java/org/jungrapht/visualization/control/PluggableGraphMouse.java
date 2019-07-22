/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Jul 7, 2005
 */

package org.jungrapht.visualization.control;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jungrapht.visualization.VisualizationViewer;

/**
 * a GraphMouse that accepts plugins for various mouse events.
 *
 * @author Tom Nelson
 */
public class PluggableGraphMouse implements VisualizationViewer.GraphMouse {

  MouseListener[] mouseListeners;
  MouseMotionListener[] mouseMotionListeners;
  MouseWheelListener[] mouseWheelListeners;
  Set<GraphMousePlugin> mousePluginList = new LinkedHashSet<>();
  Set<MouseMotionListener> mouseMotionPluginList = new LinkedHashSet<>();
  Set<MouseWheelListener> mouseWheelPluginList = new LinkedHashSet<>();

  public void add(GraphMousePlugin plugin) {
    if (plugin instanceof MouseListener) {
      mousePluginList.add(plugin);
      mouseListeners = null;
    }
    if (plugin instanceof MouseMotionListener) {
      mouseMotionPluginList.add((MouseMotionListener) plugin);
      mouseMotionListeners = null;
    }
    if (plugin instanceof MouseWheelListener) {
      mouseWheelPluginList.add((MouseWheelListener) plugin);
      mouseWheelListeners = null;
    }
  }

  public void remove(GraphMousePlugin plugin) {
    if (plugin instanceof MouseListener) {
      boolean wasThere = mousePluginList.remove(plugin);
      if (wasThere) {
        mouseListeners = null;
      }
    }
    if (plugin instanceof MouseMotionListener) {
      boolean wasThere = mouseMotionPluginList.remove(plugin);
      if (wasThere) {
        mouseMotionListeners = null;
      }
    }
    if (plugin instanceof MouseWheelListener) {
      boolean wasThere = mouseWheelPluginList.remove(plugin);
      if (wasThere) {
        mouseWheelListeners = null;
      }
    }
  }

  private void checkMouseListeners() {
    if (mouseListeners == null) {
      mouseListeners = mousePluginList.toArray(new MouseListener[0]);
    }
  }

  private void checkMouseMotionListeners() {
    if (mouseMotionListeners == null) {
      mouseMotionListeners = mouseMotionPluginList.toArray(new MouseMotionListener[0]);
    }
  }

  private void checkMouseWheelListeners() {
    if (mouseWheelListeners == null) {
      mouseWheelListeners = mouseWheelPluginList.toArray(new MouseWheelListener[0]);
    }
  }

  public void mouseClicked(MouseEvent e) {
    checkMouseListeners();
    for (MouseListener mouseListener : mouseListeners) {
      mouseListener.mouseClicked(e);
      if (e.isConsumed()) {
        break;
      }
    }
  }

  public void mousePressed(MouseEvent e) {
    checkMouseListeners();
    for (MouseListener mouseListener : mouseListeners) {
      mouseListener.mousePressed(e);
      if (e.isConsumed()) {
        break;
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
    checkMouseListeners();
    for (MouseListener mouseListener : mouseListeners) {
      mouseListener.mouseReleased(e);
      if (e.isConsumed()) {
        break;
      }
    }
  }

  public void mouseEntered(MouseEvent e) {
    checkMouseListeners();
    for (MouseListener mouseListener : mouseListeners) {
      mouseListener.mouseEntered(e);
      if (e.isConsumed()) {
        break;
      }
    }
  }

  public void mouseExited(MouseEvent e) {
    checkMouseListeners();
    for (MouseListener mouseListener : mouseListeners) {
      mouseListener.mouseExited(e);
      if (e.isConsumed()) {
        break;
      }
    }
  }

  public void mouseDragged(MouseEvent e) {
    checkMouseMotionListeners();
    for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
      mouseMotionListener.mouseDragged(e);
      if (e.isConsumed()) {
        break;
      }
    }
  }

  public void mouseMoved(MouseEvent e) {
    checkMouseMotionListeners();
    for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
      mouseMotionListener.mouseMoved(e);
      if (e.isConsumed()) {
        break;
      }
    }
  }

  public void mouseWheelMoved(MouseWheelEvent e) {
    checkMouseWheelListeners();
    for (MouseWheelListener mouseWheelListener : mouseWheelListeners) {
      mouseWheelListener.mouseWheelMoved(e);
      if (e.isConsumed()) {
        break;
      }
    }
  }
}
