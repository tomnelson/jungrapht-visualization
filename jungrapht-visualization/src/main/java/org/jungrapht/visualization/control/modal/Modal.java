package org.jungrapht.visualization.control.modal;

public interface Modal {

  void setMode(Mode mode);

  Mode getMode();

  /** */
  enum Mode {
    TRANSFORMING,
    PICKING,
    ANNOTATING,
    EDITING
  }
}
