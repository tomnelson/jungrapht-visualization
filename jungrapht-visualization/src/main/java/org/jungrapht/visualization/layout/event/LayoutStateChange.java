package org.jungrapht.visualization.layout.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jungrapht.visualization.layout.model.LayoutModel;

/**
 * An event model to convey that the LayoutModel is either active (busy) or not. Consumers of this
 * event can modify their behavior based on the state of the LayoutModel. A use-case for a consumer
 * of this event is the spatial data structures. When the LayoutModel is active (busy), the spatial
 * data structures do not constantly rebuild and compete with the LayoutAlgorithm relax Thread by
 * doing unnecessary work. When the relax thread completes, this event will alert the spatial data
 * structures to rebuild themselves by firing with 'false' (not busy)
 */
public interface LayoutStateChange {

  /** indicates that an implementor supports being a producer for this event model */
  interface Producer {
    Support getLayoutStateChangeSupport();
  }

  /** required method signatures to be a producer for this event model */
  interface Support {

    static Support create() {
      return new SupportImpl();
    }

    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addLayoutStateChangeListener(LayoutStateChange.Listener listener);

    void removeLayoutStateChangeListener(LayoutStateChange.Listener listener);

    List<LayoutStateChange.Listener> getLayoutStateChangeListeners();

    /**
     * @param layoutModel the layoutModel
     * @param state {@code true} if the layoutModel is active, {@code false} otherwise
     */
    void fireLayoutStateChanged(LayoutModel layoutModel, boolean state);
  }

  /** implementations for a producer of this event model */
  class SupportImpl implements Support {

    private SupportImpl() {}

    /** to fire or not to fire.... */
    protected boolean fireEvents = true;

    /** listeners for these changes */
    protected List<LayoutStateChange.Listener> changeListeners =
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
    public void addLayoutStateChangeListener(LayoutStateChange.Listener l) {
      changeListeners.add(l);
    }

    @Override
    public void removeLayoutStateChangeListener(LayoutStateChange.Listener l) {
      changeListeners.remove(l);
    }

    @Override
    public List<LayoutStateChange.Listener> getLayoutStateChangeListeners() {
      return changeListeners;
    }

    @Override
    public void fireLayoutStateChanged(LayoutModel layoutModel, boolean state) {
      if (fireEvents && changeListeners.size() > 0) {
        // make an event and fire it
        LayoutStateChange.Event evt = new LayoutStateChange.Event(layoutModel, state);
        for (int i = changeListeners.size() - 1; i >= 0; i--) {
          changeListeners.get(i).layoutStateChanged(evt);
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
  class Event {
    public final LayoutModel layoutModel;
    public final boolean active;

    public Event(LayoutModel layoutModel, boolean active) {
      this.layoutModel = layoutModel;
      this.active = active;
    }

    @Override
    public String toString() {
      return "LayoutStateChange.Event{" + "layoutModel=" + layoutModel + ", active=" + active + '}';
    }
  }

  /** interface required for consumers of this event model */
  interface Listener {
    void layoutStateChanged(Event evt);
  }
}
