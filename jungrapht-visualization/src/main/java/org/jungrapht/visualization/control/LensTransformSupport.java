package org.jungrapht.visualization.control;

import java.awt.*;
import java.awt.geom.Point2D;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LensTransformer;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.jungrapht.visualization.transform.MutableTransformerDecorator;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class LensTransformSupport<N, E> extends TransformSupport<N, E> {

  private static final Logger log = LoggerFactory.getLogger(LensTransformSupport.class);

  /**
   * Overridden to apply lens effects to the transformation from view to layout coordinates
   *
   * @param vv
   * @param p
   * @return
   */
  @Override
  public Point2D inverseTransform(VisualizationServer<N, E> vv, Point2D p) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW);
    MutableTransformer layoutTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.LAYOUT);

    if (viewTransformer instanceof LensTransformer) {
      LensTransformer lensTransformer = (LensTransformer) viewTransformer;
      MutableTransformer delegateTransformer = lensTransformer.getDelegate();

      if (viewTransformer instanceof MagnifyShapeTransformer) {
        MagnifyTransformer ht =
            new MagnifyTransformer(lensTransformer.getLens(), layoutTransformer);
        p = delegateTransformer.inverseTransform(p);
        p = ht.inverseTransform(p);
      } else if (viewTransformer instanceof HyperbolicShapeTransformer) {
        HyperbolicTransformer ht =
            new HyperbolicTransformer(lensTransformer.getLens(), layoutTransformer);
        p = delegateTransformer.inverseTransform(p);
        p = ht.inverseTransform(p);
      }

    } else {
      // the layoutTransformer may be a LensTransformer or not
      p = multiLayerTransformer.inverseTransform(p);
    }
    return p;
  }

  @Override
  public Shape transform(VisualizationServer<N, E> vv, Shape shape) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW);
    MutableTransformer layoutTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    VisualizationModel<N, E> model = vv.getModel();

    if (viewTransformer instanceof LensTransformer) {
      shape = multiLayerTransformer.transform(shape);
    } else if (layoutTransformer instanceof LensTransformer) {
      LayoutModel<N> layoutModel = model.getLayoutModel();
      Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
      HyperbolicShapeTransformer shapeChanger = new HyperbolicShapeTransformer(d, viewTransformer);
      LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
      shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
      MutableTransformer layoutDelegate =
          ((MutableTransformerDecorator) layoutTransformer).getDelegate();
      shape = shapeChanger.transform(layoutDelegate.transform(shape));
    } else {
      shape = multiLayerTransformer.transform(MultiLayerTransformer.Layer.LAYOUT, shape);
    }
    return shape;
  }

  @Override
  public Shape transform(
      VisualizationServer<N, E> vv, Shape shape, MultiLayerTransformer.Layer layer) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW);
    MutableTransformer layoutTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    VisualizationModel<N, E> model = vv.getModel();

    if (viewTransformer instanceof LensTransformer) {
      shape = multiLayerTransformer.transform(shape);
    } else if (layoutTransformer instanceof LensTransformer) {
      LayoutModel<N> layoutModel = model.getLayoutModel();
      Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
      HyperbolicShapeTransformer shapeChanger = new HyperbolicShapeTransformer(d, viewTransformer);
      LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
      shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
      MutableTransformer layoutDelegate =
          ((MutableTransformerDecorator) layoutTransformer).getDelegate();
      shape = shapeChanger.transform(layoutDelegate.transform(shape));
    } else {
      shape = super.transform(vv, shape, layer); //multiLayerTransformer.transform(layer, shape);
    }
    return shape;
  }

  @Override
  public Point2D transform(VisualizationServer<N, E> vv, Point2D p) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW);
    MutableTransformer layoutTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    VisualizationModel<N, E> model = vv.getModel();

    if (viewTransformer instanceof LensTransformer) {
      // use all layers
      p = multiLayerTransformer.transform(p);
    } else if (layoutTransformer instanceof LensTransformer) {
      // apply the shape changer
      LayoutModel<N> layoutModel = model.getLayoutModel();
      Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
      HyperbolicShapeTransformer shapeChanger = new HyperbolicShapeTransformer(d, viewTransformer);
      LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
      shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
      MutableTransformer layoutDelegate =
          ((MutableTransformerDecorator) layoutTransformer).getDelegate();
      p = shapeChanger.transform(layoutDelegate.transform(p));
    } else {
      // use the default
      p = multiLayerTransformer.transform(MultiLayerTransformer.Layer.LAYOUT, p);
    }
    return p;
  }

  @Override
  public Point2D transform(
      VisualizationServer<N, E> vv, Point2D p, MultiLayerTransformer.Layer layer) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW);
    MutableTransformer layoutTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.LAYOUT);
    VisualizationModel<N, E> model = vv.getModel();

    if (viewTransformer instanceof LensTransformer) {
      // use all layers
      p = multiLayerTransformer.transform(p);
    } else if (layoutTransformer instanceof LensTransformer) {
      // apply the shape changer
      LayoutModel<N> layoutModel = model.getLayoutModel();
      Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
      HyperbolicShapeTransformer shapeChanger = new HyperbolicShapeTransformer(d, viewTransformer);
      LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
      shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
      MutableTransformer layoutDelegate =
          ((MutableTransformerDecorator) layoutTransformer).getDelegate();
      p = shapeChanger.transform(layoutDelegate.transform(p));
    } else {
      // use the default
      p = multiLayerTransformer.transform(layer, p);
    }
    return p;
  }

  /**
   * Overridden to perform lens effects when inverse transforming from view to layout.
   *
   * @param vv
   * @param shape
   * @return
   */
  @Override
  public Shape inverseTransform(VisualizationServer<N, E> vv, Shape shape) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.VIEW);
    MutableTransformer layoutTransformer =
        multiLayerTransformer.getTransformer(MultiLayerTransformer.Layer.LAYOUT);

    if (layoutTransformer instanceof LensTransformer) {
      // apply the shape changer
      LayoutModel<N> layoutModel = vv.getModel().getLayoutModel();
      Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
      HyperbolicShapeTransformer shapeChanger = new HyperbolicShapeTransformer(d, viewTransformer);
      LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
      shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
      MutableTransformer layoutDelegate =
          ((MutableTransformerDecorator) layoutTransformer).getDelegate();
      shape = layoutDelegate.inverseTransform(shapeChanger.inverseTransform(shape));
    } else {
      // if the viewTransformer is either a LensTransformer or the default
      shape = multiLayerTransformer.inverseTransform(shape);
    }
    return shape;
  }
}
