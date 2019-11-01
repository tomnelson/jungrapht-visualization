package org.jungrapht.visualization.layout.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jungrapht.visualization.RenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event model to convey that the RenderContext has changed. Consumers of this event can do
 * something in response to a change in the RenderContext. A use-case for a consumer of this event
 * is the spatial data structures. When the RenderContext vertexShapeFunction or edgeShapeFunction
 * changes, the bounding rectangles of the spatial data structures should be recalculated.
 */
public interface RenderContextStateChange {

  /** indicates that an implementor supports being a producer for this event model */
  interface Producer {
    Support getRenderContextStateChangeSupport();
  }

  /** required method signatures to be a producer for this event model */
  interface Support<V, E> {

    static Support create() {
      return new SupportImpl();
    }

    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addRenderContextStateChangeListener(RenderContextStateChange.Listener listener);

    void removeRenderContextStateChangeListener(RenderContextStateChange.Listener listener);

    List<RenderContextStateChange.Listener> getRenderContextStateChangeListeners();

    /**
     * @param renderContext the renderContext
     * @param state {@code true} unused so far
     */
    void fireRenderContextStateChanged(RenderContext<V, E> renderContext, boolean state);
  }

  /** implementations for a producer of this event model */
  class SupportImpl<V, E> implements Support<V, E> {

    private static final Logger log =
        LoggerFactory.getLogger(RenderContextStateChange.SupportImpl.class);

    private SupportImpl() {}

    /** to fire or not to fire.... */
    protected boolean fireEvents = true;

    /** listeners for these changes */
    protected List<RenderContextStateChange.Listener> changeListeners =
        Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean isFireEvents() {
      return fireEvents;
    }

    @Override
    public void setFireEvents(boolean fireEvents) {
      this.fireEvents = fireEvents;
    }

    @Override
    public void addRenderContextStateChangeListener(RenderContextStateChange.Listener l) {
      changeListeners.add(l);
    }

    @Override
    public void removeRenderContextStateChangeListener(RenderContextStateChange.Listener l) {
      changeListeners.remove(l);
    }

    @Override
    public List<RenderContextStateChange.Listener> getRenderContextStateChangeListeners() {
      return changeListeners;
    }

    @Override
    public void fireRenderContextStateChanged(RenderContext<V, E> renderContext, boolean state) {
      if (fireEvents && changeListeners.size() > 0) {
        // make an event and fire it
        RenderContextStateChange.Event evt =
            new RenderContextStateChange.Event(renderContext, state);
        for (int i = changeListeners.size() - 1; i >= 0; i--) {
          changeListeners.get(i).renderContextStateChanged(evt);
        }
      }
    }
  }

  /**
   * the event payload produced by this event model and consumed by its Listener consumers. Contains
   * a reference to the RenderContext and a boolean flag (unused)
   */
  class Event<V, E> {
    public final RenderContext<V, E> renderContext;
    public final boolean active;

    public Event(RenderContext<V, E> renderContext, boolean active) {
      this.renderContext = renderContext;
      this.active = active;
    }

    @Override
    public String toString() {
      return "RenderContextStateChange.Event{"
          + "renderContext="
          + renderContext
          + ", active="
          + active
          + '}';
    }
  }

  /** interface required for consumers of this event model */
  interface Listener<V, E> {
    void renderContextStateChanged(Event<V, E> evt);
  }
}
