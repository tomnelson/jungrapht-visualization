package org.jungrapht.visualization.selection;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationServer;

/**
 * Maintains the state of what edges have been 'selected' in the graph based on whether both
 * endpoints are selected
 *
 * @author Tom Nelson
 */
public class VertexEndpointsSelectedEdgeSelectedState<V, E>
    //        extends AbstractMutableSelectedState<E>
    implements SelectedState<E> {

  /** the 'selected' items */
  protected Set<E> selected = new LinkedHashSet<>();

  protected VisualizationServer<V, E> visualizationServer;

  public VertexEndpointsSelectedEdgeSelectedState(VisualizationServer<V, E> visualizationServer) {
    this.visualizationServer = visualizationServer;
    MutableSelectedState<V> selectedVertexState = visualizationServer.getSelectedVertexState();
    selectedVertexState.addItemListener(
        evt -> {
          // a vertex selection changed. Recompute the edge selections
          Graph<V, E> graph = visualizationServer.getVisualizationModel().getGraph();
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

  @Override
  public Set<E> getSelected() {
    return Collections.unmodifiableSet(selected);
  }

  @Override
  public boolean isSelected(E t) {
    return selected.contains(t);
  }
}
