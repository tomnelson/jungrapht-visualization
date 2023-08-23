package org.jungrapht.visualization.util;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.jgrapht.Graph;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;

public class BoundingRectanglePaintable<V> implements VisualizationServer.Paintable {

  protected RenderContext rc;
  protected Graph<V, ?> graph;
  protected LayoutModel<V> layoutModel;
  protected List<Rectangle2D> rectangles;

  public BoundingRectanglePaintable(RenderContext rc, LayoutModel<V> layoutModel) {
    super();
    this.rc = rc;
    this.layoutModel = layoutModel;
    this.graph = layoutModel.getGraph();
    final BoundingRectangleCollector.Vertices<V> brc =
        new BoundingRectangleCollector.Vertices<>(rc.getVertexShapeFunction(), layoutModel);
    this.rectangles = brc.getRectangles();
    if (layoutModel instanceof ChangeEventSupport ces) {
          ces.addChangeListener(
              e -> {
                brc.compute();
                rectangles = brc.getRectangles();
              });
    }
  }

  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g.setColor(Color.cyan);

    for (Rectangle2D r : rectangles) {
      g2d.draw(rc.getMultiLayerTransformer().transform(MultiLayerTransformer.Layer.LAYOUT, r));
    }
  }

  public boolean useTransform() {
    return true;
  }
}
