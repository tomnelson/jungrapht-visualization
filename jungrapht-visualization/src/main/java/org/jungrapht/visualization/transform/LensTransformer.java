/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.visualization.transform;

import java.awt.*;
import java.awt.geom.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LensTransformer wraps a MutableAffineTransformer and modifies the transform and inverseTransform
 * methods so that they create a projection of the graph points within an elliptical lens.
 *
 * <p>LensTransformer uses an affine transform to cause translation, scaling, rotation, and shearing
 * while applying a possibly non-affine filter in its transform and inverseTransform methods.
 *
 * @author Tom Nelson
 */
public abstract class LensTransformer extends MutableTransformerDecorator
    implements MutableTransformer {

  public abstract static class Builder<T extends LensTransformer, B extends Builder<T, B>>
      extends MutableTransformerDecorator.Builder<T, B> {
    protected Lens lens;
    protected Dimension dimension = new Dimension(600, 600);

    protected Builder(Lens lens) {
      this.lens = lens;
    }

    protected Builder(Dimension dimension) {
      this.dimension = dimension;
    }

    protected B self() {
      return (B) this;
    }

    public B lens(Lens lens) {
      this.lens = lens;
      return self();
    }

    public B dimension(Dimension dimension) {
      this.dimension = dimension;
      return self();
    }

    protected abstract T build();
  }

  private static final Logger log = LoggerFactory.getLogger(LensTransformer.class);

  protected Lens lens;

  /** @param d the size used for the lens */
  protected LensTransformer(Dimension d) {
    this(new Lens(d));
  }

  /**
   * Create an instance with a possibly shared lens.
   *
   * @param lens
   */
  protected LensTransformer(Lens lens) {
    super(new MutableAffineTransformer());
    this.lens = lens;
  }

  /**
   * @param d the size used for the lens
   * @param delegate the layoutTransformer to use
   */
  protected LensTransformer(Dimension d, MutableTransformer delegate) {
    this(new Lens(d), delegate);
  }

  /**
   * @param lens
   * @param delegate the layoutTransformer to use
   */
  protected LensTransformer(Lens lens, MutableTransformer delegate) {
    super(delegate);
    this.lens = lens;
  }

  public Lens getLens() {
    return lens;
  }

  public void setToIdentity() {
    this.delegate.setToIdentity();
  }

  /** override base class transform to project the fisheye effect */
  public abstract Point2D transform(Point2D graphPoint);

  public Point2D transform(double x, double y) {
    return transform(new Point2D.Double(x, y));
  }

  /** override base class to un-project the fisheye effect */
  public abstract Point2D inverseTransform(Point2D viewPoint);

  public Point2D inverseTransform(double x, double y) {
    return inverseTransform(new Point2D.Double(x, y));
  }
}
