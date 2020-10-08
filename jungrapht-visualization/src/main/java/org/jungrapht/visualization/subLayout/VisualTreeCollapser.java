package org.jungrapht.visualization.subLayout;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayout;
import org.jungrapht.visualization.layout.model.LayoutModel;

public class VisualTreeCollapser<V, E> extends TreeCollapser<V, E> {

  protected VisualizationServer<V, E> vv;

  public VisualTreeCollapser(VisualizationServer<V, E> vv, Supplier<V> vertexSupplier) {
    super(vv.getVisualizationModel().getGraph(), vertexSupplier);
    this.vv = vv;
  }

  @Override
  public Collection<V> collapse(Collection<V> selected) {
    Set<V> set = new HashSet<>();
    for (V v : selected) {
      set.add(collapse(v));
    }
    return set;
  }

  @Override
  public V collapse(V root) {
      V collapsedVertex = super.collapse(root);
      LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
      layoutModel.set(collapsedVertex, layoutModel.apply(root));
      // the rootPredicate uses the graph which we have changed.
      // let the layoutAlgorithm create a rootPredicate based on the changed graph
      LayoutAlgorithm layoutAlgorithm = vv.getVisualizationModel().getLayoutAlgorithm();
      if (layoutAlgorithm instanceof TreeLayout) {
        ((TreeLayout) layoutAlgorithm).setRootPredicate(null);
      }
      vv.getVisualizationModel().setGraph(tree, true);
      vv.repaint();
      return root;
  }

  @Override
  public void expand(V clusterRoot) {
      super.expand(clusterRoot);
      LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
      layoutModel.set(clusterRoot, layoutModel.apply(clusterRoot));
      // the rootPredicate uses the graph which we have changed.
      // let the layoutAlgorithm create a rootPredicate based on the changed graph
      LayoutAlgorithm layoutAlgorithm = vv.getVisualizationModel().getLayoutAlgorithm();
      if (layoutAlgorithm instanceof TreeLayout) {
        ((TreeLayout) layoutAlgorithm).setRootPredicate(null);
      }
      vv.getVisualizationModel().setGraph(tree, true);
    vv.repaint();
  }
}
