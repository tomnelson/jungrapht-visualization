package org.jungrapht.visualization.layout.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.model.Point;

/**
 * Event support to indicate that a Vertex's position has changed. The jung-visualization spatial
 * data structures will consume this event and re-insert the vertex or edge. The event payload is a
 * single vertex and its (possibly new) Point location.
 *
 * @author Tom Nelson
 */
public interface LayoutVertexPositionChange {

  /**
   * indicates support for this event model
   *
   * @param <V>
   */
  interface Producer<V> {
    Support<V> getLayoutVertexPositionSupport();
  }

  /**
   * method signatures required for producers of this event model
   *
   * @param <V>
   */
  interface Support<V> {

    static Support create() {
      return new SupportImpl();
    }

    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addLayoutVertexPositionChangeListener(LayoutVertexPositionChange.Listener<V> l);

    void removeLayoutVertexPositionChangeListener(LayoutVertexPositionChange.Listener<V> l);

    List<LayoutVertexPositionChange.Listener<V>> getLayoutVertexPositionChangeListeners();

    void fireLayoutVertexPositionChanged(V vertex, Point location);
  }

  /**
   * implementations of support for this event model
   *
   * @param <V> the vertex type managed by the LayoutModel
   */
  class SupportImpl<V> implements Support<V> {

    private SupportImpl() {}

    /** to fire or not to fire.... */
    protected boolean fireEvents = true;

    /** listeners for these changes */
    protected List<Listener<V>> changeListeners = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean isFireEvents() {
      return fireEvents;
    }

    @Override
    public void setFireEvents(boolean fireEvents) {
      this.fireEvents = fireEvents;
    }

    @Override
    public void addLayoutVertexPositionChangeListener(LayoutVertexPositionChange.Listener l) {
      changeListeners.add(l);
    }

    @Override
    public void removeLayoutVertexPositionChangeListener(LayoutVertexPositionChange.Listener l) {
      changeListeners.remove(l);
    }

    @Override
    public List<LayoutVertexPositionChange.Listener<V>> getLayoutVertexPositionChangeListeners() {
      return changeListeners;
    }

    @Override
    public void fireLayoutVertexPositionChanged(V vertex, Point location) {
      if (fireEvents && changeListeners.size() > 0) {
        Event<V> layoutEvent = new Event(vertex, location);
        for (int i = changeListeners.size() - 1; i >= 0; i--) {
          changeListeners.get(i).layoutVertexPositionChanged(layoutEvent);
        }
      }
    }
  }

  /**
   * Event payload. Contains the Vertex and its location
   *
   * @param <V>
   */
  class Event<V> {
    public final V vertex;
    public final Point location;

    public Event(V vertex, Point location) {
      this.vertex = vertex;
      this.location = location;
    }
  }

  class GraphEvent<V> extends Event<V> {
    final Graph<V, ?> graph;

    public GraphEvent(V vertex, Graph<V, ?> graph, Point location) {
      super(vertex, location);
      this.graph = graph;
    }

    public GraphEvent(Event<V> layoutEvent, Graph<V, ?> graph) {
      super(layoutEvent.vertex, layoutEvent.location);
      this.graph = graph;
    }

    public Graph<V, ?> getGraph() {
      return this.graph;
    }
  }

  /**
   * implemented by consumers for this event model
   *
   * @param <V>
   */
  interface Listener<V> {
    void layoutVertexPositionChanged(Event<V> evt);

    void layoutVertexPositionChanged(GraphEvent<V> evt);
  }
}
