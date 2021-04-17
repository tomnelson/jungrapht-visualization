package org.jungrapht.visualization.control;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.util.PointUtils;

/**
 * a plugin that uses popup menus to create vertices, undirected edges, and directed edges.
 *
 * @author Tom Nelson
 * @param <V> vertex type
 * @param <E> edge type
 */
public class EditingPopupGraphMousePlugin<V, E> extends AbstractPopupGraphMousePlugin {

  protected Supplier<V> vertexFactory;
  protected Supplier<E> edgeFactory;

  public EditingPopupGraphMousePlugin(Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
    this.vertexFactory = vertexFactory;
    this.edgeFactory = edgeFactory;
  }

  protected void handlePopup(MouseEvent e) {
    final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    final LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();

    final Graph<V, E> graph = vv.getVisualizationModel().getGraph();
    final Point2D p = e.getPoint();
    final Point lp =
        PointUtils.convert(vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p));
    GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
    if (pickSupport != null) {

      final V vertex = pickSupport.getVertex(layoutModel, lp);
      final E edge = pickSupport.getEdge(layoutModel, lp);
      final MutableSelectedState<V> pickedVertexState = vv.getSelectedVertexState();
      final MutableSelectedState<E> pickedEdgeState = vv.getSelectedEdgeState();

      JPopupMenu popup = new JPopupMenu();
      if (vertex != null) {
        Set<V> picked = vv.getSelectedVertices();
        if (picked.size() > 0) {
          JMenu menu =
              new JMenu(
                  "Create " + (graph.getType().isDirected() ? "Directed" : "Undirected") + " Edge");
          popup.add(menu);
          for (final V other : picked) {
            menu.add(
                new AbstractAction("[" + other + "," + vertex + "]") {
                  public void actionPerformed(ActionEvent e) {
                    graph.addEdge(other, vertex, edgeFactory.get());
                    vv.repaint();
                  }
                });
          }
        }
        popup.add(
            new AbstractAction("Delete Vertex") {
              public void actionPerformed(ActionEvent e) {
                pickedVertexState.deselect(vertex);
                graph.removeVertex(vertex);
                vv.getVertexSpatial().recalculate();
                vv.repaint();
              }
            });
      } else if (edge != null) {
        popup.add(
            new AbstractAction("Delete Edge") {
              public void actionPerformed(ActionEvent e) {
                pickedEdgeState.deselect(edge);
                graph.removeEdge(edge);
                vv.getEdgeSpatial().recalculate();
                vv.repaint();
              }
            });
      } else {
        popup.add(
            new AbstractAction("Create Vertex") {
              public void actionPerformed(ActionEvent e) {
                V newVertex = vertexFactory.get();
                graph.addVertex(newVertex);
                Point2D p2d = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p);
                vv.getVisualizationModel().getLayoutModel().set(newVertex, p2d.getX(), p2d.getY());
                vv.repaint();
              }
            });
      }
      if (popup.getComponentCount() > 0) {
        popup.show(vv.getComponent(), e.getX(), e.getY());
      }
    }
  }
}
