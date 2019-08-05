package org.jungrapht.visualization.renderers;

public interface ModalRenderer<V, E, M extends Enum<M>> extends Renderer<V, E> {

  void setMode(M mode);

  void setVertexRenderer(M mode, Vertex<V, E> r);

  void setEdgeRenderer(M mode, Edge<V, E> r);

  void setVertexLabelRenderer(M mode, VertexLabel<V, E> r);

  void setEdgeLabelRenderer(M mode, EdgeLabel<V, E> r);

  VertexLabel<V, E> getVertexLabelRenderer(M mode);

  Vertex<V, E> getVertexRenderer(M mode);

  Edge<V, E> getEdgeRenderer(M mode);

  EdgeLabel<V, E> getEdgeLabelRenderer(M mode);
}
