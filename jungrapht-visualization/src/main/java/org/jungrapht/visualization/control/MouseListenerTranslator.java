/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
/*
 * Created on Feb 17, 2004
 */
package org.jungrapht.visualization.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.NetworkElementAccessor;
import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * This class translates mouse clicks into node clicks
 *
 * @author danyelf
 */
public class MouseListenerTranslator<N, E> extends MouseAdapter {

  private VisualizationViewer<N, E> vv;
  private GraphMouseListener<N> gel;

  /**
   * @param gel listens for mouse events
   * @param vv the viewer used for visualization
   */
  public MouseListenerTranslator(GraphMouseListener<N> gel, VisualizationViewer<N, E> vv) {
    this.gel = gel;
    this.vv = vv;
  }

  /**
   * Transform the point to the coordinate system in the VisualizationViewer, then use either
   * PickSuuport (if available) or Layout to find a Node
   *
   * @param point
   * @return
   */
  private N getNode(Point2D point) {
    // adjust for scale and offset in the VisualizationViewer
    Point2D p = point;
    //vv.getRenderContext().getBasicTransformer().inverseViewTransform(point);
    NetworkElementAccessor<N, E> pickSupport = vv.getPickSupport();
    LayoutModel<N> layoutModel = vv.getModel().getLayoutModel();
    //        Layout<N> layout = vv.getGraphLayout();
    N v = null;
    if (pickSupport != null) {
      v = pickSupport.getNode(layoutModel, p.getX(), p.getY());
    }
    return v;
  }
  /** @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent) */
  public void mouseClicked(MouseEvent e) {
    N v = getNode(e.getPoint());
    if (v != null) {
      gel.graphClicked(v, e);
    }
  }

  /** @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent) */
  public void mousePressed(MouseEvent e) {
    N v = getNode(e.getPoint());
    if (v != null) {
      gel.graphPressed(v, e);
    }
  }

  /** @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent) */
  public void mouseReleased(MouseEvent e) {
    N v = getNode(e.getPoint());
    if (v != null) {
      gel.graphReleased(v, e);
    }
  }
}
