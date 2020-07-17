package org.jungrapht.visualization.util;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

public class VertexLocationAnimator {

  public static <V, E> void jumpPointToCenter(VisualizationServer<V, E> vv, Point newCenter) {
    Point2D lvc = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(vv.getCenter());
    final double dx = (lvc.getX() - newCenter.x);
    final double dy = (lvc.getY() - newCenter.y);

    vv.getRenderContext()
        .getMultiLayerTransformer()
        .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
        .translate(dx, dy);
  }

  public static <V, E> void scrollPointToCenter(VisualizationServer<V, E> vv, Point newCenter) {
    Point2D lvc = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(vv.getCenter());
    final double dx = (lvc.getX() - newCenter.x) / 10;
    final double dy = (lvc.getY() - newCenter.y) / 10;

    Runnable animator =
        () -> {
          for (int i = 0; i < 10; i++) {
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                .translate(dx, dy);
            try {
              Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
          }
        };
    Thread thread = new Thread(animator);
    thread.start();
  }

  public static <V, E> void scrollVertexToCenter(VisualizationServer<V, E> vv, V vertex) {
    Point newCenter;
    if (vertex != null) {
      // center the selected vertex
      LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
      newCenter = layoutModel.apply(vertex);
    } else {
      // they did not pick a vertex to center, so
      // just center the graph
      Point2D center = vv.getCenter();
      newCenter = Point.of(center.getX(), center.getY());
    }
    scrollPointToCenter(vv, newCenter);
  }

  private static <V, E> Point getPointToCenter(
      VisualizationServer<V, E> vv, Collection<V> vertices) {
    LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();

    Collection<Point> points = vertices.stream().map(layoutModel).collect(Collectors.toList());

    if (points.size() > 0) {
      // center the selected vertices
      return Point.centroidOf(points);
    }

    // they did not pick a vertex to center, so
    // just center the graph
    Point2D center = vv.getCenter();
    return Point.of(center.getX(), center.getY());
  }

  public static <V, E> void scrollVertexClusterToCenter(
      VisualizationServer<V, E> vv, Collection<V> vertices) {
    Point newCenter = getPointToCenter(vv, vertices);
    scrollPointToCenter(vv, newCenter);
  }
}
