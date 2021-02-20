package org.jungrapht.visualization.selection;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationViewer;

public class VertexSelectedEndpointsEdgeSelectionListener<V, E> implements ItemListener {

  public VisualizationViewer<V, E> vv;

  VertexSelectedEndpointsEdgeSelectionListener(VisualizationViewer<V, E> vv) {
    this.vv = vv;
  }

  @Override
  public void itemStateChanged(ItemEvent evt) {
    // a vertex selection changed. Recompute the edge selections
    Graph<V, E> graph = vv.getVisualizationModel().getGraph();
    MutableSelectedState<V> selectedVertexState = vv.getSelectedVertexState();
    vv.getSelectedEdgeState()
        .select(
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
                .collect(Collectors.toSet()));
  }
}
