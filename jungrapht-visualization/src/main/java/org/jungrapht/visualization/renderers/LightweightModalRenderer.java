package org.jungrapht.visualization.renderers;

public interface LightweightModalRenderer<V, E>
    extends ModalRenderer<V, E, LightweightModalRenderer.Mode> {

  enum Mode {
    DEFAULT,
    LIGHTWEIGHT
  }
}
