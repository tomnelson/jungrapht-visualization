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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.layout.algorithms.util.CircleLayoutReduceEdgeCrossing;
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
  private boolean reduceEdgeCrossing;
  private List<V> vertexOrderedList;

  public static class Builder<V, T extends CircleLayoutAlgorithm<V>, B extends Builder<V, T, B>>
      implements LayoutAlgorithm.Builder<V, T, B> {
    protected int radius;
    protected boolean reduceEdgeCrossing = false;

    B self() {
      return (B) this;
    }

    public B radius(int radius) {
      this.radius = radius;
      return self();
    }

    public B reduceEdgeCrossing(boolean reduceEdgeCrossing) {
      this.reduceEdgeCrossing = reduceEdgeCrossing;
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
    this(builder.radius, builder.reduceEdgeCrossing);
  }

  private CircleLayoutAlgorithm(int radius, boolean reduceEdgeCrossing) {
    this.radius = radius;
    this.reduceEdgeCrossing = reduceEdgeCrossing;
  }

  public CircleLayoutAlgorithm() {
    this(CircleLayoutAlgorithm.builder());
  }

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

  //  /**
  //   * Sets the order of the vertices in the layout according to the ordering specified by {@code
  //   * comparator}.
  //   *
  //   * @param comparator the comparator to use to order the vertices
  //   */
  //  public void setVertexOrder(LayoutModel<V> layoutModel, Comparator<V> comparator) {
  //    if (vertexOrderedList == null) {
  //      vertexOrderedList = new ArrayList<>(layoutModel.getGraph().vertexSet());
  //    }
  //    vertexOrderedList.sort(comparator);
  //  }

  private void computeVertexOrder(Graph<V, Object> graph) {
    if (this.reduceEdgeCrossing) {
      // is this a multicomponent graph?
      ConnectivityInspector<V, ?> connectivityInspector = new ConnectivityInspector<>(graph);
      List<Set<V>> componentVertices = connectivityInspector.connectedSets();
      List<V> vertexOrderedList = new ArrayList<>();
      if (componentVertices.size() > 1) {
        for (Set<V> vertexSet : componentVertices) {
          // get back the graph for these vertices
          Graph<V, Object> subGraph = GraphTypeBuilder.forGraph(graph).buildGraph();
          vertexSet.forEach(subGraph::addVertex);
          for (V v : vertexSet) {
            // get neighbors
            Graphs.successorListOf(graph, v)
                .forEach(s -> subGraph.addEdge(v, s, graph.getEdge(v, s)));
            Graphs.predecessorListOf(graph, v)
                .forEach(p -> subGraph.addEdge(p, v, graph.getEdge(p, v)));
          }
          vertexOrderedList.addAll(
              new CircleLayoutReduceEdgeCrossing<>(subGraph).getVertexOrderedList());
        }
      } else {
        CircleLayoutReduceEdgeCrossing<V, ?> circleLayouts =
            new CircleLayoutReduceEdgeCrossing<>(graph);
        vertexOrderedList.addAll(circleLayouts.getVertexOrderedList());
      }
      this.vertexOrderedList = vertexOrderedList;
    } else {
      this.vertexOrderedList = new ArrayList<>(graph.vertexSet());
    }
    log.info(
        "crossing count {}",
        CircleLayoutReduceEdgeCrossing.countCrossings(graph, (V[]) vertexOrderedList.toArray()));
  }

  /**
   * Sets the order of the vertices in the layout according to the ordering of {@code vertex_list}.
   *
   * @param vertexList a list specifying the ordering of the vertices
   */
  public void setVertexOrder(LayoutModel<V> layoutModel, List<V> vertexList) {
    Objects.requireNonNull(
        vertexList.containsAll(layoutModel.getGraph().vertexSet()),
        "Supplied list must include all vertices of the graph");
    this.vertexOrderedList = vertexList;
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    if (layoutModel != null) {
      computeVertexOrder(layoutModel.getGraph());
      //       setVertexOrder(
      //          layoutModel,
      //          //              vertexOrderList);
      //          new ArrayList<>(layoutModel.getGraph().vertexSet()));

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
