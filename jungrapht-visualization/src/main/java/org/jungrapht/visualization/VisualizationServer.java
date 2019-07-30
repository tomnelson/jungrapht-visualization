/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.function.Predicate;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.control.TransformSupport;
import org.jungrapht.visualization.layout.GraphElementAccessor;
import org.jungrapht.visualization.layout.event.LayoutChange;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.spatial.Spatial;

/**
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public interface VisualizationServer<V, E>
    extends LayoutChange.Listener, ChangeListener, LayoutStateChange.Listener {

  /**
   * Specify whether this class uses its offscreen image or not.
   *
   * @param doubleBuffered if true, then doubleBuffering in the superclass is set to 'false'
   */
  void setDoubleBuffered(boolean doubleBuffered);

  /**
   * Returns whether this class uses double buffering. The superclass will be the opposite state.
   *
   * @return the double buffered state
   */
  boolean isDoubleBuffered();

  Shape viewOnLayout();

  Spatial<V> getVertexSpatial();

  void setVertexSpatial(Spatial<V> spatial);

  Spatial<E> getEdgeSpatial();

  void setEdgeSpatial(Spatial<E> spatial);

  TransformSupport<V, E> getTransformSupport();

  /** @return the model. */
  VisualizationModel<V, E> getModel();

  /** @param model the model for this class to use */
  void setModel(VisualizationModel<V, E> model);

  /**
   * In response to changes from the model, repaint the view, then fire an event to any listeners.
   * Examples of listeners are the GraphZoomScrollPane and the BirdsEyeVisualizationViewer
   *
   * @param e the change event
   */
  void stateChanged(ChangeEvent e);

  /**
   * Sets the showing Renderer to be the input Renderer. Also tells the Renderer to refer to this
   * instance as a PickedKey. (Because Renderers maintain a small amount of state, such as the
   * PickedKey, it is important to create a separate instance for each VV instance.)
   *
   * @param r the renderer to use
   */
  void setRenderer(Renderer<V, E> r);

  /**
   * Sets the lightweight Renderer
   *
   * @param r the renderer to use
   */
  void setLightweightRenderer(Renderer<V, E> r);

  /** @return the renderer used by this instance. */
  Renderer<V, E> getRenderer();

  /**
   * Makes the component visible if {@code aFlag} is true, or invisible if false.
   *
   * @param aFlag true iff the component should be visible
   * @see javax.swing.JComponent#setVisible(boolean)
   */
  void setVisible(boolean aFlag);

  /** @return the renderingHints */
  Map<Key, Object> getRenderingHints();

  /** @param renderingHints The renderingHints to set. */
  void setRenderingHints(Map<Key, Object> renderingHints);

  /** @param paintable The paintable to add. */
  void addPreRenderPaintable(Paintable paintable);

  /** @param paintable The paintable to remove. */
  void removePreRenderPaintable(Paintable paintable);

  /** @param paintable The paintable to add. */
  void addPostRenderPaintable(Paintable paintable);

  /** @param paintable The paintable to remove. */
  void removePostRenderPaintable(Paintable paintable);

  /**
   * Adds a <code>ChangeListener</code>.
   *
   * @param l the listener to be added
   */
  void addChangeListener(ChangeListener l);

  /**
   * Removes a ChangeListener.
   *
   * @param l the listener to be removed
   */
  void removeChangeListener(ChangeListener l);

  /**
   * Returns an array of all the <code>ChangeListener</code>s added with addChangeListener().
   *
   * @return all of the <code>ChangeListener</code>s added or an empty array if no listeners have
   *     been added
   */
  ChangeListener[] getChangeListeners();

  /**
   * Notifies all listeners that have registered interest for notification on this event type. The
   * event instance is lazily created.
   *
   * @see EventListenerList
   */
  void fireStateChanged();

  /** @return the vertex MutableSelectedState instance */
  MutableSelectedState<V> getSelectedVertexState();

  /** @return the edge MutableSelectedState instance */
  MutableSelectedState<E> getSelectedEdgeState();

  void setSelectedVertexState(MutableSelectedState<V> selectedVertexState);

  void setSelectedEdgeState(MutableSelectedState<E> selectedEdgeState);

  /** @return the GraphElementAccessor */
  GraphElementAccessor<V, E> getPickSupport();

  /** @param pickSupport The pickSupport to set. */
  void setPickSupport(GraphElementAccessor<V, E> pickSupport);

  Point2D getCenter();

  RenderContext<V, E> getRenderContext();

  void setRenderContext(RenderContext<V, E> renderContext);

  void repaint();

  /** an interface for the preRender and postRender */
  interface Paintable {
    void paint(Graphics g);

    boolean useTransform();
  }

  void simplifyRenderer(boolean simplify);

  void setSmallScaleOverridePredicate(Predicate<Double> smallScaleOverridePredicate);

  void scaleToLayout(ScalingControl scaler);

  void scaleToLayout();
}
