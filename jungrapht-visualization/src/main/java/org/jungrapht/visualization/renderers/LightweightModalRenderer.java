package org.jungrapht.visualization.renderers;

public interface LightweightModalRenderer<V, E> extends ModalRenderer<V, E, LightweightModalRenderer.Mode> {

    enum Mode {

        DEFAULT, LIGHTWEIGHT
    }

//    void setMode(M mode);
//
//    void setVertexRenderer(Vertex<V, E> r);
//
//    void setEdgeRenderer(Mode mode, Edge<V, E> r);
//
//    void setVertexLabelRenderer(Mode mode, VertexLabel<V, E> r);
//
//    void setEdgeLabelRenderer(Mode mode, EdgeLabel<V, E> r);
//
//    VertexLabel<V, E> getVertexLabelRenderer(Mode mode);
//
//    Vertex<V, E> getVertexRenderer(Mode mode);
//
//    Edge<V, E> getEdgeRenderer(Mode mode);
//
//    EdgeLabel<V, E> getEdgeLabelRenderer(Mode mode);

}
