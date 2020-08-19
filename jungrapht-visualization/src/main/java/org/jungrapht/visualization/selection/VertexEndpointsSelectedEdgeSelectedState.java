package org.jungrapht.visualization.selection;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.event.EventListenerList;
import org.jgrapht.Graph;

/**
 * Maintains the state of what edges have been 'selected' in the graph based on whether both
 * endpoints are selected
 *
 * @author Tom Nelson
 */
public class VertexEndpointsSelectedEdgeSelectedState<V, E> implements MutableSelectedState<E> {

  /** the 'selected' items */
  protected Set<E> selected = new LinkedHashSet<>();

  protected Supplier<Graph<V, E>> graphSupplier;

  public VertexEndpointsSelectedEdgeSelectedState(
      Supplier<Graph<V, E>> graphSupplier, MutableSelectedState<V> selectedVertexState) {
    this.graphSupplier = graphSupplier;
    selectedVertexState.addItemListener(
        evt -> {
          // a vertex selection changed. Recompute the edge selections
          Graph<V, E> graph = graphSupplier.get();
          selected =
              graph
                  .edgeSet()
                  .stream()
                  .filter(
                      e -> {
                        V source = graph.getEdgeSource(e);
                        V target = graph.getEdgeTarget(e);
                        return selectedVertexState.isSelected(source)
                            && selectedVertexState.isSelected(target);
                      })
                  .collect(Collectors.toSet());
        });
  }

  protected EventListenerList listenerList = new EventListenerList();

  @Override
  public void addItemListener(ItemListener l) {
    listenerList.add(ItemListener.class, l);
  }

  @Override
  public void removeItemListener(ItemListener l) {
    listenerList.remove(ItemListener.class, l);
  }

  protected void fireItemStateChanged(ItemEvent e) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ItemListener.class) {
        ((ItemListener) listeners[i + 1]).itemStateChanged(e);
      }
    }
  }

  @Override
  public Set<E> getSelected() {
    return Collections.unmodifiableSet(selected);
  }

  @Override
  public boolean isSelected(E t) {
    return selected.contains(t);
  }

  /**
   * select one element.
   *
   * @param element the element to select
   * @return true if the collection of selected elements was changed
   */
  @Override
  public boolean select(E element) {
    return false;
  }

  @Override
  public boolean select(E element, boolean fireEvents) {
    return false;
  }

  /**
   * deselect one element
   *
   * @param element the element to deselect
   * @return true is the collection of selected elements was changed
   */
  @Override
  public boolean deselect(E element) {
    return false;
  }

  @Override
  public boolean deselect(E element, boolean fireEvents) {
    return false;
  }

  /**
   * select a collection of elements to be the only selected elements
   *
   * @param elements
   * @return true if the collection of selected elements was changed
   */
  @Override
  public boolean select(Collection<E> elements) {
    return false;
  }

  @Override
  public boolean select(Collection<E> elements, boolean fireEvents) {
    return false;
  }

  /**
   * deselect a collection of elements
   *
   * @param elements the elements to deselect
   * @return true if the collection of selected elements was changed
   */
  @Override
  public boolean deselect(Collection<E> elements) {
    return false;
  }

  @Override
  public boolean deselect(Collection<E> elements, boolean fireEvents) {
    return false;
  }

  /** Clears the "selected" state from all elements. */
  @Override
  public void clear() {}

  @Override
  public void clear(boolean fireEvents) {}

  @Override
  public Object[] getSelectedObjects() {
    return new Object[0];
  }
}
