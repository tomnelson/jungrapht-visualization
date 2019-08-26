/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
/*
 * Created on Dec 4, 2003
 */
package org.jungrapht.visualization.layout.algorithms;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that positions vertices equally spaced on a regular circle.
 *
 * @author Masanori Harada
 * @author Tom Nelson - adapted to an algorithm
 */
public class CircleLayoutAlgorithm<V> implements LayoutAlgorithm<V> {

  private static final Logger log = LoggerFactory.getLogger(CircleLayoutAlgorithm.class);
  private double radius;
  private List<V> vertexOrderedList;

  public static class Builder<V, T extends CircleLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected int radius;

    B self() {
      return (B) this;
    }

    public B radius(int radius) {
      this.radius = radius;
      return self();
    }

    public T build() {
      return (T) new CircleLayoutAlgorithm(this);
    }
  }

  public static <V> Builder<V, ?, ?> builder() {
    return new Builder<>();
  }

  protected CircleLayoutAlgorithm(Builder<V, ?, ?> builder) {
    this(builder.radius);
  }

  private CircleLayoutAlgorithm(int radius) {
    this.radius = radius;
  }

  private CircleLayoutAlgorithm() {}

  /** @return the radius of the circle. */
  public double getRadius() {
    return radius;
  }

  /**
   * Sets the radius of the circle. Must be called before {@code initialize()} is called.
   *
   * @param radius the radius of the circle
   */
  public void setRadius(double radius) {
    this.radius = radius;
  }

  /**
   * Sets the order of the vertices in the layout according to the ordering specified by {@code
   * comparator}.
   *
   * @param comparator the comparator to use to order the vertices
   */
  public void setVertexOrder(LayoutModel<V> layoutModel, Comparator<V> comparator) {
    if (vertexOrderedList == null) {
      vertexOrderedList = new ArrayList<>(layoutModel.getGraph().vertexSet());
    }
    vertexOrderedList.sort(comparator);
  }

  /**
   * Sets the order of the vertices in the layout according to the ordering of {@code vertex_list}.
   *
   * @param vertex_list a list specifying the ordering of the vertices
   */
  public void setVertexOrder(LayoutModel<V> layoutModel, List<V> vertex_list) {
    Preconditions.checkArgument(
        vertex_list.containsAll(layoutModel.getGraph().vertexSet()),
        "Supplied list must include all vertices of the graph");
    this.vertexOrderedList = vertex_list;
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    if (layoutModel != null) {
      setVertexOrder(layoutModel, new ArrayList<>(layoutModel.getGraph().vertexSet()));

      double height = layoutModel.getHeight();
      double width = layoutModel.getWidth();

      if (radius <= 0) {
        radius = 0.45 * (Math.min(height, width));
      }

      int i = 0;
      for (V vertex : vertexOrderedList) {

        double angle = (2 * Math.PI * i) / vertexOrderedList.size();

        double posX = Math.cos(angle) * radius + width / 2;
        double posY = Math.sin(angle) * radius + height / 2;
        layoutModel.set(vertex, posX, posY);
        log.trace("set {} to {},{} ", vertex, posX, posY);

        i++;
      }
    }
  }
}
