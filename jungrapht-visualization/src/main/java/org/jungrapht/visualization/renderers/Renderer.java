/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.renderers;

import java.awt.Dimension;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.spatial.Spatial;

/**
 * The interface for drawing vertices, edges, and their labels. Implementations of this class can
 * set specific renderers for each element, allowing custom control of each.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public interface Renderer<V, E> {

  class Builder<V, E, T extends Renderer<V, E>, B extends Builder<V, E, T, B>> {

    public T build() {
      return (T) new HeavyweightRenderer<>();
    }
  }

  static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder<>();
  }

  void render(
      RenderContext<V, E> renderContext,
      LayoutModel<V> layoutModel,
      Spatial<V, V> vertexSpatial,
      Spatial<E, V> edgeSpatial);

  void render(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel);

  void renderVertex(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v);

  void renderVertexLabel(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v);

  void renderEdge(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e);

  void renderEdgeLabel(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e);

  void setVertexRenderer(Vertex<V, E> r);

  void setEdgeRenderer(Renderer.Edge<V, E> r);

  void setVertexLabelRenderer(VertexLabel<V, E> r);

  void setEdgeLabelRenderer(Renderer.EdgeLabel<V, E> r);

  VertexLabel<V, E> getVertexLabelRenderer();

  Vertex<V, E> getVertexRenderer();

  Renderer.Edge<V, E> getEdgeRenderer();

  Renderer.EdgeLabel<V, E> getEdgeLabelRenderer();

  interface Vertex<V, E> {
    void paintVertex(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v);

    class NOOP<V, E> implements Vertex<V, E> {
      public void paintVertex(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v) {}
    }
  }

  interface Edge<V, E> {
    void paintEdge(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e);

    EdgeArrowRenderingSupport<V, E> getEdgeArrowRenderingSupport();

    void setEdgeArrowRenderingSupport(EdgeArrowRenderingSupport<V, E> edgeArrowRenderingSupport);

    @SuppressWarnings("rawtypes")
    class NOOP<V, E> implements Edge<V, E> {
      public void paintEdge(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e) {}

      public EdgeArrowRenderingSupport getEdgeArrowRenderingSupport() {
        return null;
      }

      public void setEdgeArrowRenderingSupport(
          EdgeArrowRenderingSupport edgeArrowRenderingSupport) {}
    }
  }

  interface VertexLabel<V, E> {
    void labelVertex(
        RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v, String label);

    void setPositioner(Positioner positioner);

    Positioner getPositioner();

    class NOOP<V, E> implements VertexLabel<V, E> {
      public void labelVertex(
          RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, V v, String label) {}

      public Position getPosition() {
        return Position.CNTR;
      }

      public void setPosition(Position position) {}

      public Positioner getPositioner() {
        return (x, y, d) -> Position.CNTR;
      }

      public void setPositioner(Positioner positioner) {}
    }

    enum Position {
      N,
      NE,
      E,
      SE,
      S,
      SW,
      W,
      NW,
      CNTR,
      AUTO
    }

    interface Positioner {
      Position getPosition(float x, float y, Dimension d);
    }
  }

  interface EdgeLabel<V, E> {
    void labelEdge(
        RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e, String label);

    class NOOP<V, E> implements EdgeLabel<V, E> {
      public void labelEdge(
          RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e, String label) {}
    }
  }
}
