/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 1, 2005
 */

package org.jungrapht.visualization.decorators;

import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jungrapht.visualization.util.ImageShapeUtils;

/**
 * A default implementation that stores images in a Map keyed on the input T. Also applies a shaping
 * function to images to extract the shape of the opaque part of a transparent image.
 *
 * @author Tom Nelson
 */
public class IconShapeFunction<T> implements Function<T, Shape> {
  protected Map<Image, Shape> shapeMap = new HashMap<>();
  protected Function<T, Icon> iconFunction;
  protected Function<Image, Shape> shapeFunction = shapeMap::get;
  protected Function<T, Shape> delegate;

  /**
   * Creates an instance with the specified delegate.
   *
   * @param delegate the function to use if no image is present for the input t
   */
  public IconShapeFunction(Function<T, Shape> delegate) {
    this.delegate = delegate;
  }

  /** @return Returns the delegate. */
  public Function<T, Shape> getDelegate() {
    return delegate;
  }

  /** @param delegate The delegate to set. */
  public void setDelegate(Function<T, Shape> delegate) {
    this.delegate = delegate;
  }

  /**
   * get the shape from the image. If not available, get the shape from the delegate ShapeFunction
   */
  public Shape apply(T t) {
    Icon icon = iconFunction.apply(t);
    if (icon instanceof ImageIcon) {
      Image image = ((ImageIcon) icon).getImage();
      Shape shape = shapeMap.get(image);
      if (shape == null) {
        shape = ImageShapeUtils.getShape(image, 500);
        if (shape.getBounds().getWidth() > 0 && shape.getBounds().getHeight() > 0) {
          // don't cache a zero-sized shape, wait for the image
          // to be ready
          int width = image.getWidth(null);
          int height = image.getHeight(null);
          AffineTransform transform = AffineTransform.getTranslateInstance(-width / 2, -height / 2);
          shape = transform.createTransformedShape(shape);
          shapeMap.put(image, shape);
        }
      }
      return shape;
    } else {
      return delegate.apply(t);
    }
  }

  /** @param iconFunction the iconFunction to set */
  public void setIconFunction(Function<T, Icon> iconFunction) {
    this.iconFunction = iconFunction;
  }

  /** @return the shapeFunction */
  public Function<Image, Shape> getShapeFunction() {
    return shapeFunction::apply;
  }

  /** @param shapeFunction the shapeFunction to set */
  public void setShapeFunction(Function<Image, Shape> shapeFunction) {
    this.shapeFunction = shapeFunction;
  }
}
