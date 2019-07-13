package org.jungrapht.visualization.layout;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.jgrapht.Graph;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.util.ChangeEventSupport;

public class BoundingRectanglePaintable<N> implements VisualizationServer.Paintable {

  protected RenderContext rc;
  protected Graph<N, ?> graph;
  protected LayoutModel<N> layoutModel;
  protected List<Rectangle2D> rectangles;

  public BoundingRectanglePaintable(RenderContext rc, VisualizationModel<N, ?> visualizationModel) {
    super();
    this.rc = rc;
    this.layoutModel = visualizationModel.getLayoutModel();
    this.graph = visualizationModel.getNetwork();
    final BoundingRectangleCollector.Nodes<N> brc =
        new BoundingRectangleCollector.Nodes<>(rc, visualizationModel);
    this.rectangles = brc.getRectangles();
    if (layoutModel instanceof ChangeEventSupport) {
      ((ChangeEventSupport) layoutModel)
          .addChangeListener(
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
