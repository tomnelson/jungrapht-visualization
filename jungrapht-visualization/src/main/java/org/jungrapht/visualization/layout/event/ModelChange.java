package org.jungrapht.visualization.layout.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * For the most general change to a LayoutModel. There is no Event payload, only an indication that
 * there was a change in the model. A visualization would consume the event and re-draw itself.
 * Use-cases for firing this event are when the Graph or LayoutAlgorithm is changed in the
 * LayoutModel. The LayoutModel fires this event to the VisualizationModel and the
 * VisualizationModel fires this event to the VisualizationServer (the view)
 *
 * @author Tom Nelson
 */
public interface ModelChange {

  /** indicates support for this type of event dispatch */
  interface Producer {
    ModelChange.Support getModelChangeSupport();
  }

  /** method signatures to add/remove listeners and fire events */
  interface Support {

    static Support create() {
      return new SupportImpl();
    }

    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addModelChangeListener(ModelChange.Listener l);

    void removeModelChangeListener(ModelChange.Listener l);

    List<Listener> getModelChangeListeners();

    void fireModelChanged();
  }

  /** implementation of support. Manages a List of listeners */
  class SupportImpl implements Support {

    private SupportImpl() {}

    /** to fire or not to fire.... */
    protected boolean fireEvents = true;

    /** listeners for these changes */
    protected List<Listener> modelChangeListeners = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean isFireEvents() {
      return fireEvents;
    }

    @Override
    public void setFireEvents(boolean fireEvents) {
      this.fireEvents = fireEvents;
      // fire an event in case anything was missed while inactive
      if (fireEvents) {
        this.fireModelChanged();
      }
    }

    @Override
    public void addModelChangeListener(ModelChange.Listener l) {
      modelChangeListeners.add(l);
    }

    @Override
    public void removeModelChangeListener(ModelChange.Listener l) {
      modelChangeListeners.remove(l);
    }

    @Override
    public List<ModelChange.Listener> getModelChangeListeners() {
      return modelChangeListeners;
    }

    @Override
    public void fireModelChanged() {
      if (fireEvents) {
        for (int i = modelChangeListeners.size() - 1; i >= 0; i--) {
          modelChangeListeners.get(i).modelChanged();
        }
      }
    }
  }

  /** implemented by a consumer of this type of event */
  interface Listener {
    void modelChanged();
  }
}
