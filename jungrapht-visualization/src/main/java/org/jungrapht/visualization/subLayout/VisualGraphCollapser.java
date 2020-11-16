package org.jungrapht.visualization.subLayout;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

/**
 * Extends {@code GraphCollapser} to add layout placement and painting for the collapsed graph
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class VisualGraphCollapser<V, E> extends GraphCollapser<V, E> {

  protected VisualizationServer<V, E> vv;

  public VisualGraphCollapser(VisualizationServer<V, E> vv, Supplier<V> vertexSupplier) {
    super(vv.getVisualizationModel().getGraph(), vertexSupplier);
    this.vv = vv;
  }

  @Override
  public V collapse(Collection<V> selected) {
    if (this.graph == null || this.vv.getVisualizationModel().getGraph() != this.graph) {
      setGraph(vv.getVisualizationModel().getGraph());
    }
    selected = new HashSet(selected);
    if (selected.size() > 1) {
      LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
      V clusterVertex = super.collapse(selected);

      double sumx = 0;
      double sumy = 0;
      for (V v : selected) {
        Point p = layoutModel.apply(v);
        sumx += p.x;
        sumy += p.y;
      }
      Point cp = Point.of(sumx / selected.size(), sumy / selected.size());
      layoutModel.getLayoutStateChangeSupport().fireLayoutStateChanged(layoutModel, true);
      layoutModel.lock(false);
      layoutModel.set(clusterVertex, cp);
      layoutModel.lock(clusterVertex, true);
      vv.getRenderContext().getParallelEdgeIndexFunction().reset();
      layoutModel.accept(vv.getVisualizationModel().getLayoutAlgorithm());
      //      vv.getSelectedVertexState().clear();
      //      vv.getSelectedVertexState().select(clusterVertex);
      vv.getSelectedVertexState().deselect(selected);
      vv.getSelectedVertexState().select(clusterVertex);
      vv.repaint();
      return clusterVertex;
    } else {
      return null;
    }
  }

  @Override
  public void expand(Collection<V> clusterVertices) {
    Collection<V> picked = new HashSet(clusterVertices);
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();

    super.expand(picked);

    layoutModel.lock(false);
    vv.getRenderContext().getParallelEdgeIndexFunction().reset();
    layoutModel.accept(vv.getVisualizationModel().getLayoutAlgorithm());
    vv.getSelectedVertexState().clear();
    vv.repaint();
  }
}
