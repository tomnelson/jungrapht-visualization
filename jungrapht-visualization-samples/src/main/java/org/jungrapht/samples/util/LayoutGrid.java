package org.jungrapht.samples.util;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.function.Function;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.jungrapht.visualization.transform.shape.ShapeTransformer;

public class LayoutGrid<V> implements VisualizationServer.Paintable {

  Rectangle rectangle;
  LayoutModel<V> layoutModel;
  int horizontalDivisions;

  int verticalDivisions;
  VisualizationViewer<V, ?> vv;

  public LayoutGrid(VisualizationViewer<V, ?> vv, int divisions) {
    this.vv = vv;
    this.horizontalDivisions = this.verticalDivisions = divisions;
    this.layoutModel = vv.getVisualizationModel().getLayoutModel();
    this.rectangle = Rectangle.of(layoutModel.getWidth(), layoutModel.getHeight());
  }

  public LayoutGrid(VisualizationViewer<V, ?> vv) {
    this.vv = vv;
    this.layoutModel = vv.getVisualizationModel().getLayoutModel();
    Function<V, Rectangle> vbf = vv.getRenderContext().getVertexBoundsFunction();
    //        layoutModel.getGraph().vertexSet()
    //                .stream().map(vbf::apply)
    int largestVertexWidth =
        (int)
            layoutModel
                .getGraph()
                .vertexSet()
                .stream()
                .map(vbf::apply)
                .mapToDouble(r -> r.width)
                .max()
                .orElse(20);
    int largestVertexHeight =
        (int)
            layoutModel
                .getGraph()
                .vertexSet()
                .stream()
                .map(v -> vbf.apply(v))
                .mapToDouble(r -> r.height)
                .max()
                .orElse(20);

    this.horizontalDivisions = layoutModel.getWidth() / largestVertexWidth;
    this.verticalDivisions = layoutModel.getHeight() / largestVertexHeight;

    this.rectangle = Rectangle.of(layoutModel.getWidth(), layoutModel.getHeight());
  }

  public void paint(Graphics g) {
    // get new rectangle, but first change divisions for resize
    horizontalDivisions *= layoutModel.getWidth() / rectangle.width;
    verticalDivisions *= layoutModel.getHeight() / rectangle.height;
    rectangle = Rectangle.of(layoutModel.getWidth(), layoutModel.getHeight());
    //        ShapeTransformer masterViewTransformer =
    //                vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.VIEW);
    //        ShapeTransformer masterLayoutTransformer =
    //                vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    ShapeTransformer vvLayoutTransformer =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.LAYOUT);

    vv.getBounds();
    // outside boundary
    GeneralPath path = new GeneralPath();
    path.moveTo(rectangle.x, rectangle.y);
    path.lineTo(rectangle.width, rectangle.y);
    path.lineTo(rectangle.width, rectangle.height);
    path.lineTo(rectangle.x, rectangle.height);
    path.lineTo(rectangle.x, rectangle.y);

    for (int i = 0; i <= rectangle.width; i += rectangle.width / horizontalDivisions) {
      path.moveTo(rectangle.x + i, rectangle.y);
      path.lineTo(rectangle.x + i, rectangle.height);
    }
    for (int i = 0; i <= rectangle.height; i += rectangle.height / verticalDivisions) {
      path.moveTo(rectangle.x, rectangle.y + i);
      path.lineTo(rectangle.width, rectangle.y + i);
    }
    Shape lens = path;
    lens = vvLayoutTransformer.transform(lens);
    Graphics2D g2d = (Graphics2D) g;
    Color old = g.getColor();
    g.setColor(Color.red);
    g2d.draw(lens);

    path = new GeneralPath();
    path.moveTo((float) rectangle.x, (float) rectangle.getCenterY());
    path.lineTo((float) rectangle.maxX, (float) rectangle.getCenterY());
    path.moveTo((float) rectangle.getCenterX(), (float) rectangle.y);
    path.lineTo((float) rectangle.getCenterX(), (float) rectangle.maxY);
    g.setColor(old);
  }

  public boolean useTransform() {
    return true;
  }
}
