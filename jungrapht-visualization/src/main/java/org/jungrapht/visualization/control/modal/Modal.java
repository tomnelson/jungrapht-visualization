package org.jungrapht.visualization.control.modal;

public interface Modal {

  void setMode(Mode mode);

  Mode getMode();

  /** */
  enum Mode {
    DEFAULT,
    TRANSFORMING,
    PICKING,
    ANNOTATING,
    EDITING
  }
}
