package org.jungrapht.visualization.layout.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For the most general change to a LayoutModel. There is no Event payload, only an indication that
 * there was a change. A visualization would consume the event and re-draw itself. Use cases are
 * whenever vertex positions change and a view should re-draw itself
 *
 * @author Tom Nelson
 */
public interface ViewChange {

  /** indicates support for this type of event dispatch */
  interface Producer {
    ViewChange.Support getViewChangeSupport();
  }

  /** method signatures to add/remove listeners and fire events */
  interface Support {

    static Support create() {
      return new SupportImpl();
    }

    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addViewChangeListener(ViewChange.Listener l);

    void removeViewChangeListener(ViewChange.Listener l);

    List<Listener> getViewChangeListeners();

    void fireViewChanged();
  }

  /** implementation of support. Manages a List of listeners */
  class SupportImpl implements Support {

    private static final Logger log = LoggerFactory.getLogger(ViewChange.SupportImpl.class);

    private SupportImpl() {}

    /** to fire or not to fire.... */
    protected boolean fireEvents = true;

    /** listeners for these changes */
    protected List<Listener> viewChangeListeners = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean isFireEvents() {
      return fireEvents;
    }

    @Override
    public void setFireEvents(boolean fireEvents) {
      this.fireEvents = fireEvents;
      // fire an event in case anything was missed while inactive
      if (fireEvents) {
        log.trace("fireViewChanged");
        this.fireViewChanged();
      }
    }

    @Override
    public void addViewChangeListener(ViewChange.Listener l) {
      viewChangeListeners.add(l);
    }

    @Override
    public void removeViewChangeListener(ViewChange.Listener l) {
      viewChangeListeners.remove(l);
    }

    @Override
    public List<ViewChange.Listener> getViewChangeListeners() {
      return viewChangeListeners;
    }

    @Override
    public void fireViewChanged() {
      if (fireEvents) {
        for (int i = viewChangeListeners.size() - 1; i >= 0; i--) {
          viewChangeListeners.get(i).viewChanged();
        }
      }
    }
  }

  /** implemented by a consumer of this type of event */
  interface Listener {
    void viewChanged();
  }
}
