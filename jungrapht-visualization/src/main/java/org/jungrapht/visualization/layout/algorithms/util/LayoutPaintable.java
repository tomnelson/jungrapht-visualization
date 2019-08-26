package org.jungrapht.visualization.layout.algorithms.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayout;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.PolarPoint;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.jungrapht.visualization.transform.shape.ShapeTransformer;

public interface LayoutPaintable {

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

    Map<V, Rectangle> cellMap;
    LayoutModel<V> layoutModel;
    private ShapeTransformer transformer;
    Color color = Color.cyan;

    public TreeCells(
        LayoutModel<V> layoutModel, Map<V, Rectangle> cellMap, ShapeTransformer transformer) {
      this(layoutModel, cellMap, transformer, Color.cyan);
    }

    public TreeCells(
        LayoutModel<V> layoutModel,
        Map<V, Rectangle> cellMap,
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
        Rectangle d = cellMap.getOrDefault(vertex, Rectangle.IDENTITY);
        Point p = layoutModel.apply(vertex);
        Shape r = new Rectangle2D.Double(d.x, d.y, d.width, d.height);
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

  class BalloonRings<V, E> implements VisualizationServer.Paintable {
    BalloonLayoutAlgorithm<V> layoutAlgorithm;
    VisualizationServer<V, E> vv;

    public BalloonRings(VisualizationServer<V, E> vv, BalloonLayoutAlgorithm<V> layoutAlgorithm) {
      this.vv = vv;
      this.layoutAlgorithm = layoutAlgorithm;
    }

    public void paint(Graphics g) {
      g.setColor(Color.gray);

      Graphics2D g2d = (Graphics2D) g;

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (V v : vv.getVisualizationModel().getGraph().vertexSet()) {
        Double radius = layoutAlgorithm.getRadii().get(v);
        if (radius == null) {
          continue;
        }
        Point p = vv.getVisualizationModel().getLayoutModel().apply(v);
        ellipse.setFrame(-radius, -radius, 2 * radius, 2 * radius);
        AffineTransform at = AffineTransform.getTranslateInstance(p.x, p.y);
        Shape shape = at.createTransformedShape(ellipse);
        shape = vv.getTransformSupport().transform(vv, shape);
        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return false;
    }
  }

  class RadialRings<V> implements VisualizationServer.Paintable {

    Collection<Double> depths;
    LayoutModel<V> layoutModel;
    VisualizationServer<V, ?> vv;
    RadialTreeLayout<V> radialTreeLayoutAlgorithm;

    public RadialRings(
        VisualizationServer<V, ?> vv, RadialTreeLayout<V> radialTreeLayoutAlgorithm) {
      this.vv = vv;
      this.layoutModel = vv.getVisualizationModel().getLayoutModel();
      this.radialTreeLayoutAlgorithm = radialTreeLayoutAlgorithm;
      depths = getDepths();
    }

    private Collection<Double> getDepths() {
      Set<Double> depths = new HashSet<>();
      Map<V, PolarPoint> polarLocations = radialTreeLayoutAlgorithm.getPolarLocations();
      for (V v : vv.getVisualizationModel().getGraph().vertexSet()) {
        PolarPoint pp = polarLocations.get(v);
        depths.add(pp.radius);
      }
      return depths;
    }

    public void paint(Graphics g) {
      g.setColor(Color.lightGray);

      Graphics2D g2d = (Graphics2D) g;
      Point center = radialTreeLayoutAlgorithm.getCenter(layoutModel);

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (double d : depths) {
        ellipse.setFrameFromDiagonal(center.x - d, center.y - d, center.x + d, center.y + d);
        Shape shape =
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                .transform(ellipse);
        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return true;
    }
  }
}
