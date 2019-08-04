package org.jungrapht.visualization.renderers;

public interface ModalRenderer<V, E> extends Renderer<V, E> {

  enum Mode {
    DEFAULT,
    LIGHTWEIGHT
  }

  void setMode(Mode mode);
}
