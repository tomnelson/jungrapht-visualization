package org.jungrapht.visualization.layout.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event model to convey that the LayoutModel size has changed. Some LayoutAlgorithms enlarge the
 * LayoutModel area, notably the TreeLayouts. Consumers of this event can adjust transform sizes to
 * better fit the layout of the Graph. A use-case for a consumer of this event is the
 * VisualizationServer (the event is passed thru the VisualizationModel). When the LayoutModel size
 * is changed, the VisualizationServer can adjust its transforms to show the entire graph.
 */
public interface LayoutSizeChange {

  /** indicates that an implementor supports being a producer for this event model */
  interface Producer<V> {
    Support<V> getLayoutSizeChangeSupport();
  }

  /** required method signatures to be a producer for this event model */
  interface Support<V> {

    static <V> Support<V> create() {
      return new SupportImpl<>();
    }

    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addLayoutSizeChangeListener(LayoutSizeChange.Listener<V> listener);

    void removeLayoutSizeChangeListener(LayoutSizeChange.Listener<V> listener);

    List<LayoutSizeChange.Listener<V>> getLayoutSizeChangeListeners();

    /**
     * @param layoutModel the layoutModel
     * @param width the {@code width} of the {@link LayoutModel}
     * @param height the {@code height} of the {@link LayoutModel}
     */
    void fireLayoutSizeChanged(LayoutModel<V> layoutModel, int width, int height);
  }

  /** implementations for a producer of this event model */
  class SupportImpl<V> implements Support<V> {

    private static final Logger log = LoggerFactory.getLogger(LayoutSizeChange.SupportImpl.class);

    private SupportImpl() {}

    /** to fire or not to fire.... */
    protected boolean fireEvents = true;

    /** listeners for these changes */
    protected List<LayoutSizeChange.Listener<V>> changeListeners =
        Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean isFireEvents() {
      return fireEvents;
    }

    @Override
    public void setFireEvents(boolean fireEvents) {
      log.trace("setFireEvents({})", fireEvents);
      this.fireEvents = fireEvents;
    }

    @Override
    public void addLayoutSizeChangeListener(LayoutSizeChange.Listener<V> l) {
      changeListeners.add(l);
    }

    @Override
    public void removeLayoutSizeChangeListener(LayoutSizeChange.Listener<V> l) {
      changeListeners.remove(l);
    }

    @Override
    public List<LayoutSizeChange.Listener<V>> getLayoutSizeChangeListeners() {
      return changeListeners;
    }

    @Override
    public void fireLayoutSizeChanged(LayoutModel<V> layoutModel, int width, int height) {
      if (fireEvents && changeListeners.size() > 0) {
        log.trace("fireLayoutSizeChange width:{}, height:{}", width, height);
        // make an event and fire it
        LayoutSizeChange.Event<V> evt = new LayoutSizeChange.Event<>(layoutModel, width, height);
        for (int i = changeListeners.size() - 1; i >= 0; i--) {
          changeListeners.get(i).layoutSizeChanged(evt);
        }
      }
    }
  }

  /**
   * the event payload produced by this event model and consumed by its Listener consumers. Contains
   * a reference to the LayoutModel and a boolean flag indicating whether the LayoutModel is
   * currently active or not. The LayoutModel is considered active when a relaxer thread is applying
   * a LayoutAlgorithm to change Vertex positions
   */
  class Event<V> {
    public final LayoutModel<V> layoutModel;
    public final int width;
    public final int height;

    public Event(LayoutModel<V> layoutModel, int width, int height) {
      this.layoutModel = layoutModel;
      this.width = width;
      this.height = height;
    }

    @Override
    public String toString() {
      return "LayoutSizeChange.Event{"
          + "layoutModel="
          + layoutModel
          + ", "
          + width
          + ", "
          + height
          + '}';
    }
  }

  /** interface required for consumers of this event model */
  interface Listener<V> {
    void layoutSizeChanged(Event<V> evt);
  }
}
