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
 * A {@code Layout} implementation that positions nodes equally spaced on a regular circle.
 *
 * @author Masanori Harada
 * @author Tom Nelson - adapted to an algorithm
 */
public class CircleLayoutAlgorithm<N> implements LayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(CircleLayoutAlgorithm.class);
  private double radius;
  private List<N> nodeOrderedList;

  public static class Builder<N> {
    protected int radius;

    public Builder radius(int radius) {
      this.radius = radius;
      return this;
    }

    public CircleLayoutAlgorithm<N> build() {
      return new CircleLayoutAlgorithm(this);
    }
  }

  public static Builder builder() {
    return new Builder<>();
  }

  protected CircleLayoutAlgorithm(Builder<N> builder) {
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
   * Sets the order of the nodes in the layout according to the ordering specified by {@code
   * comparator}.
   *
   * @param comparator the comparator to use to order the nodes
   */
  public void setNodeOrder(LayoutModel<N> layoutModel, Comparator<N> comparator) {
    if (nodeOrderedList == null) {
      nodeOrderedList = new ArrayList<>(layoutModel.getGraph().vertexSet());
    }
    nodeOrderedList.sort(comparator);
  }

  /**
   * Sets the order of the nodes in the layout according to the ordering of {@code node_list}.
   *
   * @param node_list a list specifying the ordering of the nodes
   */
  public void setNodeOrder(LayoutModel<N> layoutModel, List<N> node_list) {
    Preconditions.checkArgument(
        node_list.containsAll(layoutModel.getGraph().vertexSet()),
        "Supplied list must include all nodes of the graph");
    this.nodeOrderedList = node_list;
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    if (layoutModel != null) {
      setNodeOrder(layoutModel, new ArrayList<>(layoutModel.getGraph().vertexSet()));

      double height = layoutModel.getHeight();
      double width = layoutModel.getWidth();

      if (radius <= 0) {
        radius = 0.45 * (height < width ? height : width);
      }

      int i = 0;
      for (N node : nodeOrderedList) {

        double angle = (2 * Math.PI * i) / nodeOrderedList.size();

        double posX = Math.cos(angle) * radius + width / 2;
        double posY = Math.sin(angle) * radius + height / 2;
        layoutModel.set(node, posX, posY);
        log.trace("set {} to {},{} ", node, posX, posY);

        i++;
      }
    }
  }
}
