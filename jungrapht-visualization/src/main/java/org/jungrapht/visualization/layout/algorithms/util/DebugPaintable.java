package org.jungrapht.visualization.layout.algorithms.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.shape.ShapeTransformer;

public interface DebugPaintable {

  class LayoutBounds implements VisualizationServer.Paintable {
    private VisualizationModel visualizationModel;
    private ShapeTransformer transformer;
    Color color;

    public LayoutBounds(VisualizationModel visualizationModel, ShapeTransformer transformer) {
      this(visualizationModel, transformer, Color.cyan);
    }

    public LayoutBounds(
        VisualizationModel visualizationModel, ShapeTransformer transformer, Color color) {
      this.visualizationModel = visualizationModel;
      this.transformer = transformer;
      this.color = color;
    }

    @Override
    public void paint(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      // get the layout dimensions:
      g.setColor(color);
      Shape layoutRectangle =
          new Rectangle2D.Double(
              0,
              0,
              visualizationModel.getLayoutSize().width,
              visualizationModel.getLayoutSize().height);
      layoutRectangle = transformer.transform(layoutRectangle);
      g2d.draw(layoutRectangle);
    }

    @Override
    public boolean useTransform() {
      return false;
    }
  }

  class Grid implements VisualizationServer.Paintable {
    int cellWidth;
    int cellHeight;
    int count;
    Color color;

    public Grid() {
      this(100, 500, 20, Color.cyan);
    }

    public Grid(int cellWidth, int cellHeight, int count, Color color) {
      this.cellWidth = cellWidth;
      this.cellHeight = cellHeight;
      this.count = count;
      this.color = color;
    }

    @Override
    public void paint(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      for (int i = 0; i < count; i++) {
        Rectangle2D r = new Rectangle2D.Double(i * 100, 0, 100, 500);
        g2d.setPaint(color);
        g2d.draw(r);
      }
    }

    @Override
    public boolean useTransform() {
      return false;
    }
  }

  class TreeCells<V> implements VisualizationServer.Paintable {

    Map<V, Dimension> cellMap;
    LayoutModel<V> layoutModel;
    private ShapeTransformer transformer;
    Color color = Color.cyan;

    public TreeCells(
        LayoutModel<V> layoutModel, Map<V, Dimension> cellMap, ShapeTransformer transformer) {
      this(layoutModel, cellMap, transformer, Color.cyan);
    }

    public TreeCells(
        LayoutModel<V> layoutModel,
        Map<V, Dimension> cellMap,
        ShapeTransformer transformer,
        Color color) {
      this.layoutModel = layoutModel;
      this.cellMap = cellMap;
      this.transformer = transformer;
      this.color = color;
    }

    @Override
    public void paint(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      for (V vertex : layoutModel.getGraph().vertexSet()) {
        Dimension d = cellMap.get(vertex);
        Point p = layoutModel.apply(vertex);
        Shape r = new Rectangle2D.Double(p.x - d.width / 2, p.y, d.width, d.height);
        r = transformer.transform(r);
        g2d.setPaint(color);
        g2d.draw(r);
      }
    }

    @Override
    public boolean useTransform() {
      return false;
    }
  }
}
