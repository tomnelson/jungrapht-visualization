/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package org.jungrapht.visualization.control;

import static org.jungrapht.visualization.layout.util.PropertyLoader.PREFIX;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jungrapht.visualization.PropertyLoader;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.selection.ShapePickSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScalingGraphMouse applies a scaling transformation to the graph layout. The Vertices get closer
 * or farther apart, but do not themselves change size. ScalingGraphMouse uses MouseWheelEvents to
 * apply the scaling.
 * <li>Using only the mouse wheel, both the X-axis and Y-axis are scaled equally.
 * <li>If the CTRL key is pressed while the mouse wheel is turned, only the X-axis is scaled
 * <li>If the ALT key is pressed while the mouse wheel is turned, only the Y-axis is scaled
 *
 * @author Tom Nelson
 */
public class ScalingGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseWheelListener, MouseListener {

  private static final Logger log = LoggerFactory.getLogger(ScalingGraphMousePlugin.class);

  private static final String PICK_AREA_SIZE = PREFIX + "pickAreaSize";
  protected int pickSize = Integer.getInteger(PICK_AREA_SIZE, 4);

  static {
    PropertyLoader.load();
  }

  public static class Builder {
    ScalingControl scalingControl = new CrossoverScalingControl();
    protected int xAxisScalingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "xAxisScalingMask", "MENU"));
    protected int yAxisScalingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "yAxisScalingMask", "ALT"));
    protected int scalingMask =
        Modifiers.masks.get(System.getProperty(PREFIX + "scalingMask", "NONE"));

    public Builder self() {
      return this;
    }

    public Builder scalingControl(ScalingControl scalingControl) {
      this.scalingControl = scalingControl;
      return self();
    }

    public Builder scalingMask(int scalingMask) {
      this.scalingMask = scalingMask;
      return self();
    }

    public Builder xAxisScalingMask(int xAxisScalingMask) {
      this.xAxisScalingMask = xAxisScalingMask;
      return self();
    }

    public Builder yAxisScalingMask(int yAxisScalingMask) {
      this.yAxisScalingMask = yAxisScalingMask;
      return self();
    }

    public ScalingGraphMousePlugin build() {
      return new ScalingGraphMousePlugin(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private static String ENABLE_MIDDLE_MOUSE_BUTTON_SCALE_RESET =
      PREFIX + "enableMiddleMouseButtonScaleReset";
  private static String ENABLE_DOUBLE_CLICK_SCALE_RESET = PREFIX + "enableDoubleClickScaleReset";

  /** the amount to zoom in by */
  protected double in = 1.1f;
  /** the amount to zoom out by */
  protected double out = 1 / 1.1f;

  protected int scalingMask;
  protected int xAxisScalingMask;
  protected int yAxisScalingMask;

  /** whether to center the zoom at the current mouse position */
  protected boolean zoomAtMouse = true;

  /** controls scaling operations */
  protected ScalingControl scaler;

  protected ScalingControl layoutScalingControl = new LayoutScalingControl();

  protected boolean enableMiddleMouseButtonScaleReset =
      Boolean.parseBoolean(System.getProperty(ENABLE_MIDDLE_MOUSE_BUTTON_SCALE_RESET, "true"));

  protected boolean enableDoubleClickScaleReset =
      Boolean.parseBoolean(System.getProperty(ENABLE_DOUBLE_CLICK_SCALE_RESET, "true"));

  public ScalingGraphMousePlugin() {
    this(ScalingGraphMousePlugin.builder());
  }

  public ScalingGraphMousePlugin(Builder builder) {
    this(
        builder.scalingControl,
        builder.scalingMask,
        builder.xAxisScalingMask,
        builder.yAxisScalingMask);
  }

  public ScalingGraphMousePlugin(
      ScalingControl scaler, int scalingMask, int xAxisScalingMask, int yAxisScalingMask) {
    this(scaler, scalingMask, xAxisScalingMask, yAxisScalingMask, 1.1f, 1 / 1.1f);
  }

  public ScalingGraphMousePlugin(
      ScalingControl scaler,
      int scalingMask,
      int xAxisScalingMask,
      int yAxisScalingMask,
      double in,
      double out) {
    this.scaler = scaler;
    this.scalingMask = scalingMask;
    this.xAxisScalingMask = xAxisScalingMask;
    this.yAxisScalingMask = yAxisScalingMask;
    this.in = in;
    this.out = out;
  }
  /** @param zoomAtMouse The zoomAtMouse to set. */
  public void setZoomAtMouse(boolean zoomAtMouse) {
    this.zoomAtMouse = zoomAtMouse;
  }

  public boolean checkModifiers(MouseEvent e) {
    return e.getModifiersEx() == scalingMask
        || e.getModifiersEx() == xAxisScalingMask
        || e.getModifiersEx() == yAxisScalingMask;
  }

  /** zoom the display in or out, depending on the direction of the mouse wheel motion. */
  public void mouseWheelMoved(MouseWheelEvent e) {
    boolean accepted = checkModifiers(e);
    if (accepted) {
      ScalingControl scalingControl = scaler;
      double xin = in;
      double yin = in;
      double xout = out;
      double yout = out;
      // check for single axis
      if (e.getModifiersEx() == xAxisScalingMask) {
        // only scale x axis,
        yin = yout = 1.0f;
        scalingControl = layoutScalingControl;
      }
      if (e.getModifiersEx() == yAxisScalingMask) {
        // only scroll y axis
        xin = xout = 1.0f;
        scalingControl = layoutScalingControl;
      }
      VisualizationViewer vv = (VisualizationViewer) e.getSource();
      Point2D mouse = e.getPoint();
      int amount = e.getWheelRotation();
      if (zoomAtMouse) {
        if (amount < 0) {
          scalingControl.scale(vv, xin, yin, mouse);
        } else if (amount > 0) {
          scalingControl.scale(vv, xout, yout, mouse);
        }
      } else {
        Point2D center = vv.getCenter();
        if (amount < 0) {
          scalingControl.scale(vv, xin, yin, center);
        } else if (amount > 0) {
          scalingControl.scale(vv, xout, yout, center);
        }
      }
      e.consume();
      vv.repaint();
    }
  }

  /** @return Returns the zoom in value. */
  public double getIn() {
    return in;
  }
  /** @param in The zoom in value to set. */
  public void setIn(double in) {
    this.in = in;
  }
  /** @return Returns the zoom out value. */
  public double getOut() {
    return out;
  }
  /** @param out The zoom out value to set. */
  public void setOut(double out) {
    this.out = out;
  }

  public ScalingControl getScaler() {
    return scaler;
  }

  public void setScaler(ScalingControl scaler) {
    this.scaler = scaler;
  }

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component.
   *
   * @param e the event to be processed
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    log.trace("mouseClicked {} in {}", e.getClickCount(), this.getClass().getName());

    if (enableDoubleClickScaleReset) {
      if (e.getClickCount() == 2 && scaler instanceof CrossoverScalingControl) {
        if (!singleVertexSelection(e) && !singleEdgeSelection(e)) {
          CrossoverScalingControl crossoverScalingControl = (CrossoverScalingControl) scaler;
          crossoverScalingControl.reset((VisualizationServer) e.getSource(), e.getPoint());
          ((VisualizationViewer) e.getSource()).scaleToLayout(scaler);
          e.consume();
        }
      }
    }
  }

  protected <V, E> boolean singleVertexSelection(MouseEvent e) {
    VisualizationServer<V, E> vv = (VisualizationServer<V, E>) e.getSource();
    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
    TransformSupport<V, E> transformSupport = vv.getTransformSupport();
    Point2D layoutPoint = transformSupport.inverseTransform(vv, e.getPoint());
    Rectangle2D footprintRectangle =
        new Rectangle2D.Double(
            e.getPoint().x - pickSize / 2, e.getPoint().y - pickSize / 2, pickSize, pickSize);

    V vertex;
    if (pickSupport instanceof ShapePickSupport) {
      ShapePickSupport<V, E> shapePickSupport = (ShapePickSupport<V, E>) pickSupport;
      vertex = shapePickSupport.getVertex(layoutModel, footprintRectangle);
    } else {
      vertex = pickSupport.getVertex(layoutModel, layoutPoint.getX(), layoutPoint.getY());
    }
    return vertex != null;
  }

  protected <V, E> boolean singleEdgeSelection(MouseEvent e) {
    VisualizationServer<V, E> vv = (VisualizationServer<V, E>) e.getSource();
    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
    TransformSupport<V, E> transformSupport = vv.getTransformSupport();
    Point2D layoutPoint = transformSupport.inverseTransform(vv, e.getPoint());
    Rectangle2D footprintRectangle =
        new Rectangle2D.Double(
            e.getPoint().x - pickSize / 2, e.getPoint().y - pickSize / 2, pickSize, pickSize);

    E edge;
    if (pickSupport instanceof ShapePickSupport) {
      ShapePickSupport<V, E> shapePickSupport = (ShapePickSupport<V, E>) pickSupport;
      edge = shapePickSupport.getEdge(layoutModel, footprintRectangle);
    } else {
      edge = pickSupport.getEdge(layoutModel, layoutPoint.getX(), layoutPoint.getY());
    }

    return edge != null;
  }

  /**
   * Invoked when a mouse button has been pressed on a component.
   *
   * @param e the event to be processed
   */
  @Override
  public void mousePressed(MouseEvent e) {
    log.trace("mousePressed in {}", this.getClass().getName());

    if (enableMiddleMouseButtonScaleReset) {
      // check for middle mouse button and reset transforms
      if (e.getModifiersEx() == InputEvent.BUTTON2_DOWN_MASK
          && scaler instanceof CrossoverScalingControl) {
        CrossoverScalingControl crossoverScalingControl = (CrossoverScalingControl) scaler;
        crossoverScalingControl.reset((VisualizationServer) e.getSource(), e.getPoint());
        ((VisualizationViewer) e.getSource()).scaleToLayout(scaler);
        e.consume();
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component.
   *
   * @param e the event to be processed
   */
  @Override
  public void mouseReleased(MouseEvent e) {}

  /**
   * Invoked when the mouse enters a component.
   *
   * @param e the event to be processed
   */
  @Override
  public void mouseEntered(MouseEvent e) {}

  /**
   * Invoked when the mouse exits a component.
   *
   * @param e the event to be processed
   */
  @Override
  public void mouseExited(MouseEvent e) {}
}
