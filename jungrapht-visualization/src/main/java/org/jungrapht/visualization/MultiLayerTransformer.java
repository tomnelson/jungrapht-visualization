package org.jungrapht.visualization;

import java.awt.Shape;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.transform.BidirectionalTransformer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.jungrapht.visualization.transform.shape.ShapeTransformer;
import org.jungrapht.visualization.util.ChangeEventSupport;

public interface MultiLayerTransformer
    extends BidirectionalTransformer, ShapeTransformer, ChangeEventSupport {

  enum Layer {
    LAYOUT,
    VIEW
  }

  void setTransformer(Layer layer, MutableTransformer Function);

  MutableTransformer getTransformer(Layer layer);

  Point2D inverseTransform(Layer layer, Point2D p);

  Point2D inverseTransform(Layer layer, double x, double y);

  Point2D transform(Layer layer, Point2D p);

  Point2D transform(Layer layer, double x, double y);

  Shape transform(Layer layer, Shape shape);

  Shape inverseTransform(Layer layer, Shape shape);

  void setToIdentity();
}
