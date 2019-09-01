package org.jungrapht.visualization.renderers;

public interface ModalRenderer<V, E> extends Renderer<V, E> {

  /** the Modes for rendering */
  class Mode {}

  void setMode(Mode mode);

  void setVertexRenderer(Mode mode, Vertex<V, E> r);

  void setEdgeRenderer(Mode mode, Edge<V, E> r);

  void setVertexLabelRenderer(Mode mode, VertexLabel<V, E> r);

  void setEdgeLabelRenderer(Mode mode, EdgeLabel<V, E> r);

  VertexLabel<V, E> getVertexLabelRenderer(Mode mode);

  Vertex<V, E> getVertexRenderer(Mode mode);

  Edge<V, E> getEdgeRenderer(Mode mode);

  EdgeLabel<V, E> getEdgeLabelRenderer(Mode mode);
}
